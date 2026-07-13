package io.github.aaronateataco.petsandpals.gui;

import io.github.aaronateataco.petsandpals.Central;
import io.github.aaronateataco.petsandpals.PetsInitializer;
import io.github.aaronateataco.petsandpals.cloud.PetsCloudClient;
import io.github.aaronateataco.petsandpals.cloud.PlayerKeystore;
import io.github.aaronateataco.petsandpals.cloud.dto.AdoptResponse;
import io.github.aaronateataco.petsandpals.enums.PetList;
import io.github.aaronateataco.petsandpals.mob.AbstractPet;
import io.github.aaronateataco.petsandpals.mob.vanilla.neutral.ClientBee;
import io.github.aaronateataco.petsandpals.mob.vanilla.neutral.ClientFox;
import io.github.aaronateataco.petsandpals.mob.vanilla.passive.ClientCat;
import io.github.aaronateataco.petsandpals.mob.vanilla.passive.ClientCopperGolem;
import io.github.aaronateataco.petsandpals.ui.ScrollRow;
import io.github.aaronateataco.petsandpals.ui.Theme;
import io.github.aaronateataco.petsandpals.ui.ThemedButton;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static io.github.aaronateataco.petsandpals.Central.CONFIG;

/**
 * First-launch "adopt your starter pet" screen: a translucent glass-style overlay
 * with a carousel - the centered species rendered big with its variant swatches
 * underneath, its neighbors peeking in smaller and option-less at each side, and
 * arrows to cycle. The world stays visible (dimmed) behind it rather than being
 * replaced by an opaque background. Phase 1's cloud backend is fox-only (see
 * {@code worker.js}'s {@code VALID_SPECIES}), so the roster carries a handful of the
 * Menagerie's already-public pets for the character-select feel, but only fox is
 * actually adoptable - cycling to anything else swaps the Continue button for a
 * "Coming soon" hint, the same pattern {@link MenagerieScreen} already uses for its
 * locked sky/sea tabs. The variant gallery is always populated with every fox skin,
 * rendered via the same "preview a species without it being spawned/active/in-world"
 * trick {@link MenagerieScreen#previewPets()} uses.
 *
 * <p>Requires a connection to complete a real adoption; offers an explicit
 * play-offline-for-now fallback that does NOT mark the adoption as done, so
 * {@code Central.createJoinHandler()} offers this screen again on a later join once
 * the player's actually online.
 */
public class AdoptionScreen extends Screen {

    private enum State { CONNECTING, ADOPTED, OFFLINE, ERROR }

    private static final String ADOPTABLE_SPECIES = "fox";
    private static final Set<String> ROSTER = Set.of("fox", "cat", "bee", "copper_golem");

    private static final int GLASS_H = 150;
    private static final int CENTER_CELL = 74;
    private static final int SIDE_CELL = 36;
    private static final int SIDE_GAP = 16;
    private static final int ARROW_SIZE = 18;
    private static final int VARIANT_CELL = 30;
    private static final int VARIANT_GAP = 4;
    private static final int VARIANT_ROW_HEIGHT = VARIANT_CELL + 3 + 8;

    // fraction of a preview box's height the entity's own bounding-box height maps to -
    // scaling to fill the whole box crops ears/tails/wide poses at the edges, so this
    // leaves generous headroom on every side (character-select screens keep the model
    // well clear of the frame, not filling it edge to edge)
    private static final float PREVIEW_FILL_RATIO = 0.45F;

    private final Screen parent;
    private final List<PetList> tiles;
    private int centerIndex = 0;
    private volatile State state = State.CONNECTING;
    private boolean adoptionStarted = false;
    private ThemedButton continueButton;
    private ThemedButton babyToggleButton;
    private ScrollRow<String> variantRow;
    private Map<String, AbstractPet> previewPets;

    private int glassX, glassY, glassW;
    private int stageX, stageY;
    private int sideCellY;
    private int nameY, statusY;
    private int arrowY, leftArrowX, rightArrowX;

    // drag-the-nametag-onto-your-pet, ported from MenagerieScreen's naming page -
    // only meaningful for the actually-adoptable centered species (fox in Phase 1),
    // same gate the variant row / Continue button already use
    private static final int TAG_ICON_SIZE = 16;
    private EditBox nameBox;
    private int tagRestX, tagRestY;
    private boolean draggingTag = false;
    private double dragMouseX, dragMouseY;
    private float dangleSeconds = -1.0F;
    private String dangleName;

    public AdoptionScreen(Screen parent) {
        super(Component.literal("Choose your first pet"));
        this.parent = parent;
        this.tiles = new ArrayList<>(List.of(PetList.values()));
        this.tiles.removeIf(p -> !ROSTER.contains(p.name()));
        this.tiles.sort((a, b) -> {
            if (a.name().equals(ADOPTABLE_SPECIES)) return -1;
            if (b.name().equals(ADOPTABLE_SPECIES)) return 1;
            return a.getDisplayName().getString().compareTo(b.getDisplayName().getString());
        });
    }

    private Component babyToggleLabel() {
        return Component.literal(CONFIG.isBaby ? "Baby" : "Adult");
    }

    @Override
    public boolean isPauseScreen() {
        // a glass overlay on the live game, not a menu blocking it - Screen's default
        // (true) also stops GameRenderer from rendering the level scene at all while
        // this screen is open (shouldRenderLevel is gated on advanceGameTime, which
        // pausing disables), not just freezing a visible frame like the vanilla pause
        // menu's blur effect - so with the default, nothing was ever behind the
        // translucent overlay to show through in the first place
        return false;
    }

    private PetList centered() {
        return this.tiles.get(this.centerIndex);
    }

    private void cycleCentered(int direction) {
        this.centerIndex = Math.floorMod(this.centerIndex + direction, this.tiles.size());
        this.variantRow.setSelected(this.currentFoxSkin());
    }

    @Override
    protected void init() {
        this.glassW = Math.min(400, this.width - 40);
        this.glassX = (this.width - this.glassW) / 2;
        this.glassY = (this.height - GLASS_H) / 2 - 6;

        this.stageX = this.glassX + this.glassW / 2 - CENTER_CELL / 2;
        this.nameY = this.glassY + 6;
        this.stageY = this.nameY + 10;
        this.sideCellY = this.stageY + (CENTER_CELL - SIDE_CELL) / 2;

        int variantRowW = 160;
        int variantRowX = this.glassX + this.glassW / 2 - variantRowW / 2;
        int variantRowTop = this.stageY + CENTER_CELL + 4;
        this.variantRow = new ScrollRow<>(variantRowX, variantRowTop, variantRowW, VARIANT_ROW_HEIGHT,
                VARIANT_CELL, VARIANT_GAP, this::renderVariantCell, this::pickVariant);
        this.variantRow.setItems(Central.FOX_SKINS);
        this.variantRow.setSelected(this.currentFoxSkin());

        this.arrowY = this.stageY + CENTER_CELL / 2 - ARROW_SIZE / 2;
        this.leftArrowX = this.glassX + 6;
        this.rightArrowX = this.glassX + this.glassW - 6 - ARROW_SIZE;

        this.statusY = this.glassY + GLASS_H + 10;

        this.continueButton = this.addRenderableWidget(ThemedButton.of(
                this.width / 2 - 50, this.statusY + 18, 100, 18,
                Component.literal("Continue"), b -> this.finish()));
        // derived from state, not hardcoded false: if Minecraft re-invokes init() (a
        // resize/layout pass) after the adoption callback already fired once, this
        // replaces the button with a fresh instance - hardcoding false here would
        // silently make it invisible again with nothing left to flip it back, since
        // beginAdoption() (and its one-shot callback) never fires a second time
        this.continueButton.visible = this.state != State.CONNECTING;

        // baby/adult is a single global cosmetic flag (CONFIG.isBaby), not
        // per-species, same as MenagerieScreen's own toggle - flipping it here
        // immediately affects every preview render (stage/side-peeks/variant cells)
        // since they all read the live CONFIG value through the same renderers
        this.babyToggleButton = this.addRenderableWidget(ThemedButton.of(
                this.width / 2 - 50 - 8 - 56, this.statusY + 18, 56, 18,
                this.babyToggleLabel(), b -> {
                    Central.setPetBaby(!CONFIG.isBaby);
                    b.setMessage(this.babyToggleLabel());
                }));

        // naming row: type a name, then drag the tag onto the pet stage above it -
        // centered below the baby/continue row rather than beside the glass panel,
        // so it doesn't get cramped on narrower windows
        int namingY = this.statusY + 44;
        this.nameBox = this.addRenderableWidget(new EditBox(this.font, this.width / 2 - 60, namingY, 90, 16,
                Component.literal("Name")));
        this.nameBox.setMaxLength(32);
        this.tagRestX = this.width / 2 + 38;
        this.tagRestY = namingY - 2;

        if (!this.adoptionStarted) {
            this.adoptionStarted = true;
            this.beginAdoption();
        }
    }

    private void beginAdoption() {
        if (this.minecraft == null || this.minecraft.player == null) {
            this.state = State.ERROR;
            return;
        }
        UUID uuid = this.minecraft.player.getUUID();
        String modVersion = FabricLoader.getInstance().getModContainer(PetsInitializer.MOD_ID)
                .map(c -> c.getMetadata().getVersion().getFriendlyString())
                .orElse("unknown");

        PetsCloudClient.adopt(uuid, ADOPTABLE_SPECIES, modVersion).thenAccept(response ->
                Minecraft.getInstance().execute(() -> this.handleAdoptResponse(uuid, response)));
    }

    private void handleAdoptResponse(UUID uuid, AdoptResponse response) {
        if (response == null) {
            // network failure/timeout - not a server rejection, see PetsCloudClient.send()
            this.state = State.OFFLINE;
            if (this.continueButton != null) this.continueButton.visible = true;
            return;
        }
        if (response.error != null && !"already_adopted".equals(response.error)) {
            this.state = State.ERROR;
            PetsInitializer.LOGGER.error("[Pets&Pals] adoption failed: {}", response.error);
            if (this.continueButton != null) this.continueButton.visible = true;
            return;
        }

        // fresh adopt (201) carries a secret to persist; already_adopted (409) means
        // this UUID has a confirmed adoption from before (a previous install, or a
        // race with another client) - either way, the account is adopted, so treat
        // both as success. Only persist a secret when one was actually issued.
        if (response.playerSecret != null) {
            PlayerKeystore.saveSecret(uuid, response.playerSecret);
        }
        CONFIG.activePet = response.species != null ? response.species : ADOPTABLE_SPECIES;
        CONFIG.hasAdoptedStarterPet = true;
        AutoConfig.getConfigHolder(io.github.aaronateataco.petsandpals.PetsConfig.class).save();
        this.state = State.ADOPTED;
        if (this.continueButton != null) this.continueButton.visible = true;
    }

    /** Offline fallback: play with a fox now, retry the real adoption on a later join. */
    private void playOffline() {
        CONFIG.activePet = ADOPTABLE_SPECIES;
        // deliberately NOT hasAdoptedStarterPet = true - see class javadoc
        AutoConfig.getConfigHolder(io.github.aaronateataco.petsandpals.PetsConfig.class).save();
        this.finish();
    }

    private void finish() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }

    // --- variant gallery ---

    private String currentFoxSkin() {
        return CONFIG.foxSkin != null ? CONFIG.foxSkin : "red";
    }

    private void pickVariant(String skin) {
        CONFIG.foxSkin = skin;
        if (this.variantRow != null) this.variantRow.setSelected(skin);
    }

    private void renderVariantCell(GuiGraphicsExtractor graphics, String skin, int x, int y, int w, int h,
                                    boolean hovered, boolean selected) {
        Theme.drawTile(graphics, x, y, w, h, selected);
        AbstractPet pet = this.previewPets().get(ADOPTABLE_SPECIES);
        if (pet != null && !pet.isRemoved()) {
            String real = CONFIG.foxSkin;
            CONFIG.foxSkin = skin;
            int scale = Math.max(8, (int) (h * PREVIEW_FILL_RATIO / Math.max(0.3F, pet.getBbHeight())));
            InventoryScreen.extractEntityInInventoryFollowsMouse(graphics, x, y - 2, x + w, y + h - 2,
                    scale, 0.0625F, x + w / 2, y + h, pet);
            CONFIG.foxSkin = real;
        }
    }

    /** Maps species ids to preview pet instances, each one this screen's own and never
     *  Central's. Central's static instances aren't safe to borrow here: the moment
     *  adoption completes, the tick watcher auto-summons the real fox via
     *  {@code Utils.summonPet()}, which sets the entity invisible for its "emerges
     *  from a sweet berry bush" dwelling spawn animation - {@code AbstractPet.spawnDwellingBlock()}
     *  maps {@code clientfox} to {@code SWEET_BERRY_BUSH}. Rendering that exact same
     *  entity in this screen's picture-in-picture preview would make the fox tile/stage
     *  go invisible right along with it for the animation's duration. Every roster tile
     *  here is a completely independent, never-spawned instance instead, so nothing the
     *  real pet does in the world can affect what this screen shows. */
    private Map<String, AbstractPet> previewPets() {
        if (this.previewPets == null) {
            this.previewPets = new HashMap<>();
        }
        if (this.minecraft != null && this.minecraft.level != null) {
            for (String species : ROSTER) {
                this.previewPets.computeIfAbsent(species, this::freshPreview);
            }
        }
        return this.previewPets;
    }

    // Entity.<init> gives every locally-constructed client entity id 0 (ClientLevel never
    // overrides Level.getNextEntityId()) - harmless for a single preview pet like
    // MenagerieScreen's (only ever one on screen per frame), but this screen renders all
    // four roster species at once, and something in the GUI picture-in-picture render path
    // keys/caches per entity id, so four id-0 entities in the same frame collide and only
    // one actually renders. Never added to the level (so Utils.spawnClientEntity's own
    // negative range doesn't apply here), just needs ids distinct from each other and from
    // 0; a separate negative range keeps this obviously unrelated to real spawned entities.
    private static final java.util.concurrent.atomic.AtomicInteger PREVIEW_ENTITY_IDS =
            new java.util.concurrent.atomic.AtomicInteger(-100_000);

    private AbstractPet freshPreview(String species) {
        ClientLevel level = this.minecraft.level;
        AbstractPet pet = switch (species) {
            case "fox" -> new ClientFox(PetsInitializer.FOX, level);
            case "cat" -> new ClientCat(PetsInitializer.CAT, level);
            case "bee" -> new ClientBee(PetsInitializer.BEE, level);
            case "copper_golem" -> new ClientCopperGolem(PetsInitializer.COPPER_GOLEM, level);
            default -> null;
        };
        if (pet != null) {
            pet.setId(PREVIEW_ENTITY_IDS.decrementAndGet());
        }
        return pet;
    }

    // --- rendering ---

    @Override
    public void extractRenderState(@NotNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        // background/content first, widgets (super) last: render commands paint in
        // queue order, so queuing the Continue button before this content would let
        // the content paint directly over it. Draw order must put widgets last so
        // they land on top.

        // just the world, dimmed - no collective panel/box bounding the whole
        // carousel, only the individual pet tiles and the Continue button get their
        // own boxes (see renderStage/renderSidePeek/ThemedButton)
        graphics.fill(0, 0, this.width, this.height, 0x400B0B0D);

        PetList center = this.centered();
        boolean adoptable = center.name().equals(ADOPTABLE_SPECIES);

        String title = "Choose your first pet";
        graphics.text(this.font, Component.literal(title),
                this.width / 2 - this.font.width(title) / 2, this.glassY - 12, Theme.TEXT_HEADER);

        this.renderSidePeek(graphics, this.tiles.get(Math.floorMod(this.centerIndex - 1, this.tiles.size())),
                this.stageX - SIDE_GAP - SIDE_CELL, false);
        this.renderSidePeek(graphics, this.tiles.get(Math.floorMod(this.centerIndex + 1, this.tiles.size())),
                this.stageX + CENTER_CELL + SIDE_GAP, true);

        // arrows: plain glyphs, no button box - just the carousel's pets and their
        // individual tile boxes should read as "boxed", not the arrows
        boolean hoverLeft = mouseX >= this.leftArrowX && mouseX < this.leftArrowX + ARROW_SIZE
                && mouseY >= this.arrowY && mouseY < this.arrowY + ARROW_SIZE;
        boolean hoverRight = mouseX >= this.rightArrowX && mouseX < this.rightArrowX + ARROW_SIZE
                && mouseY >= this.arrowY && mouseY < this.arrowY + ARROW_SIZE;
        graphics.text(this.font, Component.literal("<"), this.leftArrowX + ARROW_SIZE / 2 - 2,
                this.arrowY + ARROW_SIZE / 2 - 4, hoverLeft ? Theme.TEXT_HEADER : Theme.TEXT_SECONDARY);
        graphics.text(this.font, Component.literal(">"), this.rightArrowX + ARROW_SIZE / 2 - 2,
                this.arrowY + ARROW_SIZE / 2 - 4, hoverRight ? Theme.TEXT_HEADER : Theme.TEXT_SECONDARY);

        String name = center.getDisplayName().getString();
        graphics.text(this.font, Component.literal(name),
                this.stageX + CENTER_CELL / 2 - this.font.width(name) / 2, this.nameY,
                adoptable ? Theme.TEXT_HEADER : Theme.TEXT_SECONDARY);

        AbstractPet pet = this.previewPets().get(center.name());
        this.renderStage(graphics, mouseX, mouseY, pet);

        if (adoptable) {
            this.variantRow.render(graphics, mouseX, mouseY);
        } else {
            String soon = "Coming soon";
            graphics.text(this.font, Component.literal(soon),
                    this.glassX + this.glassW / 2 - this.font.width(soon) / 2,
                    this.stageY + CENTER_CELL + 6, Theme.TEXT_DISABLED);
        }

        String message = switch (this.state) {
            case CONNECTING -> "Adopting your first pet...";
            case ADOPTED -> "Adopted! A fox will be with you shortly.";
            case OFFLINE -> "No connection right now - you can play offline for now,\nand adopt properly next time you're online.";
            case ERROR -> "Something went wrong adopting your pet. You can still\nplay offline for now.";
        };
        int y = this.statusY;
        for (String line : message.split("\n")) {
            graphics.text(this.font, Component.literal(line),
                    this.width / 2 - this.font.width(line) / 2, y, Theme.TEXT_SECONDARY);
            y += 10;
        }

        // only fox is actually adoptable in Phase 1 - swap the confirm button for the
        // same "Coming soon" treatment while browsing anything else, so confirming
        // always matches what's actually centered
        this.continueButton.visible = adoptable && this.state != State.CONNECTING;

        // naming row: only meaningful once you're actually naming the species you're
        // adopting, same gate the variant row already uses
        this.nameBox.visible = adoptable;
        if (adoptable) {
            boolean overStage = this.overStage(mouseX, mouseY);
            String hint = this.draggingTag
                    ? (overStage ? "Release to name your pet!" : "Drag onto your pet")
                    : "Type a name, then drag the tag onto your pet";
            graphics.text(this.font, Component.literal(hint),
                    this.width / 2 - this.font.width(hint) / 2, this.tagRestY + TAG_ICON_SIZE + 4,
                    overStage && this.draggingTag ? 0xFF55FF55 : Theme.TEXT_SECONDARY);
            if (this.draggingTag && this.overStage(this.dragMouseX, this.dragMouseY)) {
                graphics.outline(this.stageX, this.stageY,
                        this.stageX + CENTER_CELL, this.stageY + CENTER_CELL, 0xFF55FF55);
            }
        }

        super.extractRenderState(graphics, mouseX, mouseY, partialTick);

        // drawn last (after the widgets super() just queued) so the dragged icon
        // stays visibly on top while crossing over the nameBox/buttons, same
        // reasoning already documented above for why super() itself runs last here
        if (adoptable) {
            graphics.item(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.NAME_TAG),
                    this.tagDrawX(), this.tagDrawY());
        }
        if (this.dangleSeconds >= 0.0F && this.dangleName != null) {
            // a light spring-damper swing settling under the pet's head, same
            // physics as MenagerieScreen's own dangle animation
            float t = this.dangleSeconds;
            float decay = (float) Math.exp(-t * 3.0);
            float swing = (float) Math.sin(t * 14.0) * 6.0F * decay;
            int cx = this.stageX + CENTER_CELL / 2;
            int ty = this.stageY + CENTER_CELL - 4;
            graphics.pose().pushMatrix();
            graphics.pose().rotateAbout((float) Math.toRadians(swing), cx, ty);
            String tag = this.dangleName;
            graphics.text(this.font, Component.literal(tag), cx - this.font.width(tag) / 2, ty, 0xFFFFFF55);
            graphics.pose().popMatrix();
        }
    }

    /** A smaller, option-less neighbor peeking in from one side of the carousel;
     *  clicking it (or the arrow on that side) brings it to the center. */
    private void renderSidePeek(GuiGraphicsExtractor graphics, PetList species, int x, boolean rightSide) {
        Theme.drawTile(graphics, x, this.sideCellY, SIDE_CELL, SIDE_CELL, false);
        AbstractPet pet = this.previewPets().get(species.name());
        if (pet != null && !pet.isRemoved()) {
            int scale = Math.max(6, (int) (SIDE_CELL * PREVIEW_FILL_RATIO / Math.max(0.3F, pet.getBbHeight())));
            InventoryScreen.extractEntityInInventoryFollowsMouse(graphics,
                    x, this.sideCellY, x + SIDE_CELL, this.sideCellY + SIDE_CELL,
                    scale, 0.0625F, x + SIDE_CELL / 2, this.sideCellY + SIDE_CELL, pet);
        }
    }

    private void renderStage(GuiGraphicsExtractor graphics, int mouseX, int mouseY, AbstractPet pet) {
        Theme.drawTile(graphics, this.stageX, this.stageY, CENTER_CELL, CENTER_CELL, true);
        if (pet == null || pet.isRemoved()) {
            String hint = "Preview unavailable";
            graphics.text(this.font, Component.literal(hint),
                    this.stageX + CENTER_CELL / 2 - this.font.width(hint) / 2,
                    this.stageY + CENTER_CELL / 2, Theme.TEXT_SECONDARY);
            return;
        }
        int scale = Math.max(10, (int) (CENTER_CELL * PREVIEW_FILL_RATIO / Math.max(0.3F, pet.getBbHeight())));
        InventoryScreen.extractEntityInInventoryFollowsMouse(graphics,
                this.stageX, this.stageY, this.stageX + CENTER_CELL, this.stageY + CENTER_CELL,
                scale, 0.0625F, mouseX, mouseY, pet);
    }

    private boolean overSidePeek(double mouseX, double mouseY, boolean rightSide) {
        int x = rightSide ? this.stageX + CENTER_CELL + SIDE_GAP : this.stageX - SIDE_GAP - SIDE_CELL;
        return mouseX >= x && mouseX < x + SIDE_CELL && mouseY >= this.sideCellY && mouseY < this.sideCellY + SIDE_CELL;
    }

    private boolean overArrow(double mouseX, double mouseY, int arrowX) {
        return mouseX >= arrowX && mouseX < arrowX + ARROW_SIZE
                && mouseY >= this.arrowY && mouseY < this.arrowY + ARROW_SIZE;
    }

    // --- drag-the-nametag-onto-the-pet interaction ---

    private boolean overStage(double mouseX, double mouseY) {
        return mouseX >= this.stageX && mouseX < this.stageX + CENTER_CELL
                && mouseY >= this.stageY && mouseY < this.stageY + CENTER_CELL;
    }

    private int tagDrawX() {
        return this.draggingTag ? (int) (this.dragMouseX - TAG_ICON_SIZE / 2.0) : this.tagRestX;
    }

    private int tagDrawY() {
        return this.draggingTag ? (int) (this.dragMouseY - TAG_ICON_SIZE / 2.0) : this.tagRestY;
    }

    private boolean overTagIcon(double mouseX, double mouseY) {
        return mouseX >= this.tagRestX && mouseX < this.tagRestX + TAG_ICON_SIZE
                && mouseY >= this.tagRestY && mouseY < this.tagRestY + TAG_ICON_SIZE;
    }

    /** Writes the typed name straight into the fox's own config field - this screen
     *  only ever adopts a fox (see ADOPTABLE_SPECIES), and CONFIG.activePet isn't
     *  guaranteed to already be "fox" yet at naming time (it's set once adoption
     *  actually completes), so this can't go through Central.setActivePetName's
     *  CONFIG.activePet-keyed switch the way MenagerieScreen's naming page does. */
    private void applyNameTag() {
        String name = this.nameBox.getValue().trim();
        if (name.isEmpty()) return;
        CONFIG.foxName = name;
        Central.refreshPetNames();
        this.dangleName = name;
        this.dangleSeconds = 0.0F;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0) {
            if (this.overSidePeek(event.x(), event.y(), false) || this.overArrow(event.x(), event.y(), this.leftArrowX)) {
                this.cycleCentered(-1);
                return true;
            }
            if (this.overSidePeek(event.x(), event.y(), true) || this.overArrow(event.x(), event.y(), this.rightArrowX)) {
                this.cycleCentered(1);
                return true;
            }
            if (this.centered().name().equals(ADOPTABLE_SPECIES) && this.overTagIcon(event.x(), event.y())) {
                this.draggingTag = true;
                this.dragMouseX = event.x();
                this.dragMouseY = event.y();
                return true;
            }
        }
        if (this.variantRow != null && this.variantRow.mouseClicked(event.x(), event.y(), event.button())) {
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (this.draggingTag) {
            this.dragMouseX = event.x();
            this.dragMouseY = event.y();
            return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (this.draggingTag) {
            this.draggingTag = false;
            if (this.overStage(event.x(), event.y())) {
                this.applyNameTag();
            }
            return true;
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (this.variantRow != null && this.variantRow.mouseScrolled(mouseX, mouseY, scrollY)) {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.dangleSeconds >= 0.0F) {
            this.dangleSeconds += 1.0F / 20.0F;
            if (this.dangleSeconds > 1.5F) {
                this.dangleSeconds = -1.0F;
                this.dangleName = null;
            }
        }
    }

    @Override
    public void onClose() {
        // closing this screen any way other than the Continue button (Esc, etc.)
        // still needs to leave the player with a playable pet - route it through the
        // same offline fallback as the explicit button. If an in-flight adopt() call
        // completes afterward, its callback still runs (Minecraft.execute doesn't
        // care whether this screen is still open) and upgrades activePet/
        // hasAdoptedStarterPet to the real adopted state at that point.
        this.playOffline();
    }
}
