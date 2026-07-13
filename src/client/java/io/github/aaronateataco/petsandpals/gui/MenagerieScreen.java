package io.github.aaronateataco.petsandpals.gui;

import io.github.aaronateataco.petsandpals.Central;
import io.github.aaronateataco.petsandpals.PetsConfig;
import io.github.aaronateataco.petsandpals.PetsConfigScreen;
import io.github.aaronateataco.petsandpals.PetsInitializer;
import io.github.aaronateataco.petsandpals.cloud.PetsCloudClient;
import io.github.aaronateataco.petsandpals.cloud.PlayerKeystore;
import io.github.aaronateataco.petsandpals.cloud.dto.AdoptAdditionalResponse;
import io.github.aaronateataco.petsandpals.enums.PetList;
import io.github.aaronateataco.petsandpals.mob.AbstractPet;
import io.github.aaronateataco.petsandpals.ui.PreviewTileButton;
import io.github.aaronateataco.petsandpals.ui.Theme;
import io.github.aaronateataco.petsandpals.ui.ThemedButton;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.util.Util;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.function.DoubleConsumer;

import static io.github.aaronateataco.petsandpals.Central.CONFIG;

/**
 * The Menagerie: searchable catalog of every pet with a live preview and the core
 * settings. Names/skins still live in the old YACL screen behind "Advanced settings".
 */
public class MenagerieScreen extends Screen {

    private static final int PANEL_WIDTH = 132;
    private static final int PREVIEW_HEIGHT = 90;

    /** species enum name -> live pet instance (Central pre-constructs one of each on summon). */
    private static Map<String, AbstractPet> previewPets;
    // square picture-only tiles now, not wide text-label buttons - matches the
    // proportions AdoptionScreen's own SIDE_CELL/CENTER_CELL preview tiles use
    private static final int GRID_CELL = 56;
    private static final int GRID_GAP = 6;
    private static final int GRID_TOP = 52;

    private final Screen parent;
    private Button raftWoodButton;
    private Button cushionButton;
    private io.github.aaronateataco.petsandpals.mob.PetRaft raftPreview;
    private final List<PetList> allSpecies;
    private List<PetList> filtered;
    private final List<Button> gridWidgets = new ArrayList<>();
    private EditBox searchBox;
    private Button prevButton;
    private Button nextButton;
    private PetList selected;
    private Button summonButton;
    private Button skinButton;
    private int previewX;
    private int previewY;
    private int previewW;
    private int previewH;
    private int gridTop = GRID_TOP;
    private int gridLeft = 12;
    private int contentLeft = 12;
    private int page = 0;
    private int columns = 3;
    private int rows = 6;
    private String query = "";
    private Element element = Element.ALL;
    private enum OwnerFilter { ALL, OWNED, NEW }
    private OwnerFilter ownerFilter = OwnerFilter.ALL;
    private Button filterButton;

    // anvil naming page: type a name, then drag the tag onto the pet to apply it
    private final List<net.minecraft.client.gui.components.AbstractWidget> catalogWidgets = new ArrayList<>();
    private boolean naming = false;
    private EditBox nameBox;
    private Button nameTagButton;
    private Button namingCancelButton;
    private int tagRestX, tagRestY;
    private boolean draggingTag = false;
    private double dragMouseX, dragMouseY;
    private float dangleSeconds = -1.0F;
    private String dangleName;
    private float closeFade = -1.0F;
    private Button babyToggleButton;
    private int dandelionRestX, dandelionRestY;
    private boolean draggingDandelion = false;
    private float lockFlashSeconds = -1.0F;

    // --- adopt-a-new-species prompt (3-day cooldown, currency-skippable - see
    // cloudflare-worker/worker.js's /v1/pets/adopt-additional) ---
    private static final int SKIP_COOLDOWN_COST = 500; // mirrors worker.js's SKIP_COOLDOWN_COST
    private PetList adoptPromptSpecies;
    private String adoptStatusMessage;
    private boolean adoptInFlight = false;
    private Button adoptFreeButton;
    private Button adoptSkipButton;
    private Button buyCoinsButton;
    private Button adoptCancelButton;
    private int adoptPanelLeft, adoptPanelTop, adoptPanelW, adoptPanelH;
    private float bondRewardFlashSeconds = -1.0F;
    private String bondRewardMessage;
    private Button resetProgressButton;
    private boolean resetArmed = false;
    private int leftPanelLeft, leftPanelTop, leftPanelRight, leftPanelBottom;

    // element categories
    private enum Element { ALL, LAND, SKY, SEA }

    // each tab's icon/text Y position, filled in during init()'s tab-building loop -
    // tabs run down the left edge as a vertical strip (shop-style side rail), so the
    // icon/text X is shared across all of them but Y varies per tab
    private final Map<Element, Integer> tabIconY = new java.util.EnumMap<>(Element.class);
    private int tabIconX;
    private int tabTextX;
    private static final Map<Element, Identifier> CATEGORY_ICONS = new java.util.EnumMap<>(Element.class);
    static {
        CATEGORY_ICONS.put(Element.ALL, Identifier.fromNamespaceAndPath(PetsInitializer.MOD_ID, "theme/category/all"));
        CATEGORY_ICONS.put(Element.LAND, Identifier.fromNamespaceAndPath(PetsInitializer.MOD_ID, "theme/category/land"));
        CATEGORY_ICONS.put(Element.SKY, Identifier.fromNamespaceAndPath(PetsInitializer.MOD_ID, "theme/category/sky"));
        CATEGORY_ICONS.put(Element.SEA, Identifier.fromNamespaceAndPath(PetsInitializer.MOD_ID, "theme/category/sea"));
    }
    // color-coded per category, like the concept art - kept square (not rounded)
    // per an explicit decision to match the rest of this theme's chrome rather than
    // the concept's rounded pills
    private static final Map<Element, Integer> CATEGORY_COLOR = new java.util.EnumMap<>(Element.class);
    static {
        CATEGORY_COLOR.put(Element.ALL, 0xFF585C68);
        CATEGORY_COLOR.put(Element.LAND, 0xFF8A5A2E);
        CATEGORY_COLOR.put(Element.SKY, 0xFF4A90C4);
        CATEGORY_COLOR.put(Element.SEA, 0xFF2E9B8F);
    }

    // public builds only unlock the polished pets for now; a .pnp_testing file in
    // the game dir (dev instances have one) opens the whole catalog
    private static final java.util.Set<String> PUBLIC_PETS = java.util.Set.of(
            "copper_golem", "fox", "cat", "bee");
    private final boolean testingCatalog;

    private static final java.util.Set<String> SKY_PETS = java.util.Set.of(
            "allay", "bat", "bee", "blaze", "breeze", "ghast", "happy_ghast",
            "parrot", "phantom", "vex", "wither", "ender_dragon");
    private static final java.util.Set<String> SEA_PETS = java.util.Set.of(
            "squid", "cod", "salmon", "tropical_fish", "pufferfish", "tadpole", "axolotl",
            "dolphin", "nautilus", "guardian", "elder_guardian", "turtle");

    // vanilla species from newer game versions, backported here as pets with the mod's
    // own model/texture since this branch's registry doesn't have the real asset -
    // still shown as Vanilla origin, since that's genuinely what they are
    private static final java.util.Set<String> BACKPORTED_VANILLA = java.util.Set.of("sulfur_cube");

    /** Where a species comes from: vanilla if the base game registers the same id (or it's
     *  a newer-version vanilla species backported here - see {@link #BACKPORTED_VANILLA}). */
    private static String originOf(PetList species) {
        String name = species.name().toLowerCase(Locale.ROOT);
        boolean vanilla = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE
                .containsKey(net.minecraft.resources.Identifier.withDefaultNamespace(name))
                || BACKPORTED_VANILLA.contains(name);
        return vanilla ? "Vanilla" : "Pets&Pals";
    }

    private static Element elementOf(PetList species) {
        String name = species.name().toLowerCase(Locale.ROOT);
        if (SEA_PETS.contains(name)) return Element.SEA;
        if (SKY_PETS.contains(name)) return Element.SKY;
        return Element.LAND;
    }

    public MenagerieScreen(Screen parent) {
        super(Component.literal("Shop"));
        this.parent = parent;
        this.testingCatalog = java.nio.file.Files.exists(
                net.minecraft.client.Minecraft.getInstance().gameDirectory.toPath().resolve(".pnp_testing"));
        // catalog carries the vanilla-backed roster only; the old mod's custom
        // species (racoon, potato crew, nerd creeper...) are shelved for now
        this.allSpecies = new ArrayList<>(List.of(PetList.values()));
        this.allSpecies.removeIf(p -> !"Vanilla".equals(originOf(p)));
        this.allSpecies.sort(Comparator.comparing(p -> p.getDisplayName().getString()));
        this.filtered = this.allSpecies;
        try {
            this.selected = PetList.valueOf(CONFIG.activePet.replaceAll(" ", "_"));
        } catch (Exception e) {
            this.selected = PetList.values()[0];
        }
    }

    @Override
    public boolean isPauseScreen() {
        // an overlay on the live game, not a menu blocking it - same fix already
        // applied to AdoptionScreen this session: Screen's default (true) also stops
        // GameRenderer from rendering the level scene at all while this screen is
        // open, not just freezing a visible frame - so with the default, the world
        // was never actually visible behind the dim at all, just solid black
        return false;
    }

    @Override
    protected void init() {
        // category tabs run down the left edge as a wide, color-coded strip (icon +
        // visible text, not icon-only) - each category gets its own flat color
        // (see CATEGORY_COLOR) instead of blending into the shared grey chrome, the
        // way the concept art color-codes land/sky/sea. Everything else in the left
        // column anchors off contentLeft instead of a bare screen margin, so it
        // shifts right to make room for the strip.
        int tabStripX = 6;
        int tabWidth = 120;
        int tabHeight = 38;
        int contentLeft = tabStripX + tabWidth + 10;
        this.contentLeft = contentLeft;
        int leftWidth = this.width - PANEL_WIDTH - 24 - contentLeft;

        int tabTop = 22;
        this.tabIconY.clear();
        int ty = tabTop;
        for (Element el : Element.values()) {
            Element tabElement = el;
            ThemedButton tab = ThemedButton.of(tabStripX, ty, tabWidth, tabHeight, Component.empty(), b -> {
                this.element = tabElement;
                this.page = 0;
                this.applyFilter();
                this.rebuildGrid();
            }).withBackgroundColor(CATEGORY_COLOR.get(el));
            String label = switch (el) {
                case ALL -> "All"; case LAND -> "Land"; case SKY -> "Sky"; case SEA -> "Sea";
            };
            // sky/sea catalogs open up alongside the rest of the roster
            if ((el == Element.SKY || el == Element.SEA) && !this.testingCatalog) {
                tab.active = false;
                tab.setTooltip(net.minecraft.client.gui.components.Tooltip.create(
                        Component.literal(label + " - Coming soon")));
            }
            this.tabIconY.put(el, ty + tabHeight / 2 - 5);
            ty += tabHeight + 6;
            this.track(tab);
        }
        this.tabIconX = tabStripX + 8;
        this.tabTextX = this.tabIconX + 14;

        // the pet takes center stage: big preview up top, arrows to flip through
        this.previewW = Math.min(210, leftWidth - 64);
        this.previewH = Math.max(96, Math.min(120, this.height / 4));
        this.previewX = contentLeft + (leftWidth - this.previewW) / 2;
        this.previewY = 22;
        int arrowY = this.previewY + this.previewH / 2 - 10;
        this.track(ThemedButton.of(this.previewX - 26, arrowY, 20, 20,
                Component.literal("<"), b -> this.cycleSelected(-1)));
        this.track(ThemedButton.of(this.previewX + this.previewW + 6, arrowY, 20, 20,
                Component.literal(">"), b -> this.cycleSelected(1)));

        // search box + a small "Find" button that focuses it, + a filter button
        // that cycles All/Owned/New (not-yet-adopted) on top of the existing
        // element/text filters - grouped and centered as one row. Labels stay
        // plain ASCII rather than icon glyphs on purpose: a Unicode alembic
        // character used elsewhere in this file for a similar small marker
        // rendered as a broken fallback glyph in Minecraft's default font (found
        // via an actual screenshot this session), not worth risking again here.
        int searchY = this.previewY + this.previewH + 24;
        int searchWidth = Math.min(180, leftWidth - 56);
        int searchGroupWidth = searchWidth + 4 + 20 + 4 + 20;
        int searchGroupX = contentLeft + (leftWidth - searchGroupWidth) / 2;
        this.searchBox = new EditBox(this.font, searchGroupX, searchY, searchWidth, 18, Component.literal("Search"));
        this.searchBox.setValue(this.query);
        this.searchBox.setResponder(text -> {
            this.query = text;
            this.page = 0;
            this.applyFilter();
            this.rebuildGrid();
        });
        this.track(this.searchBox);
        this.track(ThemedButton.of(searchGroupX + searchWidth + 4, searchY - 1, 20, 20,
                Component.literal("Find"), b -> this.setFocused(this.searchBox)));
        this.filterButton = this.track(ThemedButton.of(searchGroupX + searchWidth + 28, searchY - 1, 20, 20,
                this.filterLabel(), b -> {
                    this.ownerFilter = OwnerFilter.values()[(this.ownerFilter.ordinal() + 1) % OwnerFilter.values().length];
                    b.setMessage(this.filterLabel());
                    this.page = 0;
                    this.applyFilter();
                    this.rebuildGrid();
                }));
        this.filterButton.setTooltip(net.minecraft.client.gui.components.Tooltip.create(
                Component.literal("Filter: cycles All / Owned / New")));

        this.gridTop = searchY + 24;
        this.columns = Math.max(2, leftWidth / (GRID_CELL + GRID_GAP));
        this.rows = Math.max(2, (this.height - this.gridTop - 34) / (GRID_CELL + GRID_GAP));
        this.gridLeft = contentLeft + (leftWidth - (this.columns * (GRID_CELL + GRID_GAP) - GRID_GAP)) / 2;

        int pageY = this.height - 28;
        this.prevButton = this.track(ThemedButton.of(contentLeft, pageY, 20, 20, Component.literal("<"), b -> {
            if (this.page > 0) this.page--;
            this.rebuildGrid();
        }));
        this.nextButton = this.track(ThemedButton.of(contentLeft + 24, pageY, 20, 20, Component.literal(">"), b -> {
            if ((this.page + 1) * this.pageSize() < this.filtered.size()) this.page++;
            this.rebuildGrid();
        }));

        int panelX = this.width - PANEL_WIDTH - 6;
        int y = GRID_TOP + PREVIEW_HEIGHT + 4;
        this.summonButton = this.track(ThemedButton.of(panelX, y, PANEL_WIDTH, 20,
                Component.literal("Summon"), b -> this.summonSelected()));
        this.updateSummonState();
        y += 24;
        this.track(ThemedButton.of(panelX, y, PANEL_WIDTH, 20, this.petToggleLabel(), b -> {
            CONFIG.petOn = !Boolean.TRUE.equals(CONFIG.petOn);
            if (CONFIG.petOn) Central.summonPet();
            else Central.despawnPet();
            b.setMessage(this.petToggleLabel());
        }));
        y += 24;
        this.nameTagButton = this.track(ThemedButton.of(panelX, y, PANEL_WIDTH, 20,
                Component.literal("Name Tag..."), b -> this.enterNaming()));
        y += 24;
        this.skinButton = this.track(ThemedButton.of(panelX, y, PANEL_WIDTH, 20, this.skinLabel(), b -> {
            this.cycleSkin();
            b.setMessage(this.skinLabel());
        }));
        y += 24;
        this.babyToggleButton = this.track(ThemedButton.of(panelX, y, PANEL_WIDTH, 20, this.babyToggleLabel(), b -> {
            Central.setPetBaby(!CONFIG.isBaby);
            b.setMessage(this.babyToggleLabel());
        }));
        y += 24;
        this.track(new PercentSlider(panelX, y, PANEL_WIDTH, 20, "Speed", 0.25, 3.0,
                CONFIG.petSpeed, value -> CONFIG.petSpeed = (float) value));
        y += 24;
        this.track(new PercentSlider(panelX, y, PANEL_WIDTH, 20, "Volume", 0.0, 1.0,
                CONFIG.petVolume == null ? 1.0f : CONFIG.petVolume, value -> CONFIG.petVolume = (float) value));
        y += 24;
        this.raftWoodButton = this.track(ThemedButton.of(panelX, y, PANEL_WIDTH, 20, this.raftWoodLabel(), b -> {
            String[] woods = io.github.aaronateataco.petsandpals.mob.PetRaftBlock.WOODS;
            int i = java.util.Arrays.asList(woods).indexOf(CONFIG.raftWood);
            CONFIG.raftWood = woods[(i + 1) % woods.length];
            this.raftPreview = null;
            b.setMessage(this.raftWoodLabel());
        }));
        y += 24;
        this.cushionButton = this.track(ThemedButton.of(panelX, y, PANEL_WIDTH, 20, this.cushionLabel(), b -> {
            // cycles through the 16 dyes plus a bare deck
            List<String> options = new ArrayList<>(List.of(io.github.aaronateataco.petsandpals.mob.PetRaftBlock.DYES));
            options.add("none");
            int i = options.indexOf(CONFIG.cushionColor);
            CONFIG.cushionColor = options.get((i + 1) % options.size());
            this.raftPreview = null;
            b.setMessage(this.cushionLabel());
        }));
        y += 28;
        this.track(ThemedButton.of(panelX, y, PANEL_WIDTH, 20, Component.literal("Advanced settings..."), b -> {
            if (this.minecraft == null) return;
            try {
                this.minecraft.setScreen(PetsConfigScreen.getInstance().getAdvancedConfigScreenFactory().create(this));
            } catch (Exception e) {
                // building the YACL screen can throw if the active pet's skin enum is
                // mismatched (see PetsConfigScreen's enumClass) - log instead of silently
                // doing nothing, which just looked like a dead button
                io.github.aaronateataco.petsandpals.PetsInitializer.LOGGER.error(
                        "[Pets&Pals] Advanced settings screen failed to open for active pet '{}'", CONFIG.activePet, e);
            }
        }));

        if (this.testingCatalog) {
            y += 24;
            // destructive + server-side, so this only ever exists in dev/test
            // instances (same .pnp_testing gate as the rest of the unlocked test
            // catalog) - never reachable by a real player. Two clicks required:
            // first arms it (and relabels to make that obvious), second actually
            // wipes the account. resetArmed resets itself if you navigate away
            // from the button by clicking anything else in this screen that
            // rebuilds the grid/panel, since init() runs again on any resize but
            // NOT on ordinary clicks - acceptable given this is a testing tool,
            // not something that needs airtight anti-misclick guarantees.
            this.resetProgressButton = this.track(ThemedButton.of(panelX, y, PANEL_WIDTH, 20,
                    Component.literal("Reset Progress"), b -> this.resetAccountProgress()));
        }

        this.addRenderableWidget(ThemedButton.of(panelX, this.height - 28, PANEL_WIDTH, 20,
                Component.literal("Done"), b -> this.onClose()));

        // adopt-a-new-species prompt: a small centered modal, buttons built once here
        // and shown/hidden via closeAdoptPrompt()/refreshAdoptPromptState() rather
        // than rebuilt each time, same pattern the naming page already uses for
        // nameBox/namingCancelButton. When open, extractRenderState() skips drawing
        // the preview stage/pet entirely (see the early return there) specifically
        // so nothing drawn after super.extractRenderState() can paint over these
        // buttons - the preview stage's own box can genuinely overlap screen-center
        // on wide layouts, which would otherwise repeat the exact z-order bug this
        // session already fixed once for AdoptionScreen's Continue button.
        int adoptButtonW = 200;
        int adoptCenterX = this.width / 2 - adoptButtonW / 2;
        int adoptTop = this.height / 2 - 40;
        int adoptButtonY = adoptTop + 40;
        this.adoptPanelLeft = adoptCenterX - 8;
        this.adoptPanelTop = adoptTop - 8;
        this.adoptPanelW = adoptButtonW + 16;
        this.adoptPanelH = (adoptButtonY + 48 + 20) - adoptTop + 16;
        this.adoptFreeButton = this.addRenderableWidget(ThemedButton.of(adoptCenterX, adoptButtonY, adoptButtonW, 20,
                Component.literal("Adopt"), b -> this.confirmAdopt(false)));
        this.adoptSkipButton = this.addRenderableWidget(ThemedButton.of(adoptCenterX, adoptButtonY, adoptButtonW, 20,
                Component.literal("Skip wait"), b -> this.confirmAdopt(true)));
        this.buyCoinsButton = this.addRenderableWidget(ThemedButton.of(adoptCenterX, adoptButtonY + 24, adoptButtonW, 20,
                Component.literal("Buy Paw Coins..."), b -> this.buyCurrency()));
        this.adoptCancelButton = this.addRenderableWidget(ThemedButton.of(adoptCenterX, adoptButtonY + 48, adoptButtonW, 20,
                Component.literal("Cancel"), b -> this.closeAdoptPrompt()));
        this.closeAdoptPrompt();

        // anvil naming page: a name tag icon the player drags onto the pet stage
        this.tagRestX = contentLeft + leftWidth / 2 - 80;
        this.tagRestY = searchY;
        this.dandelionRestX = this.tagRestX;
        this.dandelionRestY = this.tagRestY + 36;
        this.nameBox = new EditBox(this.font, this.tagRestX + 26, searchY, Math.min(160, leftWidth - 100), 18,
                Component.literal("Name"));
        this.nameBox.setMaxLength(32);
        this.nameBox.visible = false;
        this.addRenderableWidget(this.nameBox);
        // parked below the dandelion row so it never sits under the dandelion icon -
        // the old fixed offset (searchY + 28) landed right in the middle of where the
        // dandelion icon and its hint text render for baby pets, so the icon covered
        // half the button. Kept at this fixed spot regardless of baby/adult so it
        // doesn't jump around if the age toggle changes without reopening this page.
        this.namingCancelButton = ThemedButton.of(this.tagRestX, searchY + 80, 106, 20,
                Component.literal("Cancel"), b -> this.exitNaming());
        this.namingCancelButton.visible = false;
        this.addRenderableWidget(this.namingCancelButton);

        // group all the existing (already-positioned) content into one bounded
        // panel rather than leaving every button floating loose over a full-screen
        // dim - bounds computed from geometry already in scope above, not
        // re-derived, so none of the ~40 already-tuned button positions move.
        // One panel, not two: the live gap between the grid and the right-side
        // button column is only ~20px to begin with (baked into leftWidth's own
        // formula), well under the inset sprite's 12px nine-slice border on each
        // side - two separate panels that close would have their borders visibly
        // collide rather than reading as a clean gap.
        this.leftPanelLeft = 2;
        this.leftPanelTop = Math.min(tabTop - 10, GRID_TOP + PREVIEW_HEIGHT - 34);
        this.leftPanelRight = this.width - 2;
        this.leftPanelBottom = this.height - 4;

        this.applyFilter();
        this.rebuildGrid();
        this.updateContentVisibility();
        this.updateSummonState();
        this.refreshOwnedPets();
        this.refreshCurrencyBalance();
        this.claimBondReward();
    }

    /** Claims passive bond-time Paw Coins once per Shop open - free, no payment or
     *  ad content, just a reward for playing (see worker.js's handleBondClaim). */
    private void claimBondReward() {
        if (this.minecraft == null || this.minecraft.player == null) return;
        UUID uuid = this.minecraft.player.getUUID();
        String secret = PlayerKeystore.loadSecret(uuid);
        if (secret == null) return; // never adopted online yet - nothing to claim against
        PetsCloudClient.claimBondReward(uuid, secret).thenAccept(resp -> Minecraft.getInstance().execute(() -> {
            if (resp == null || resp.error != null || resp.balance == null) return;
            CONFIG.currencyBalanceCache = resp.balance;
            this.refreshAdoptPromptState();
            if (resp.creditedCoins != null && resp.creditedCoins > 0) {
                this.bondRewardMessage = "+" + resp.creditedCoins + " Paw Coins earned!";
                this.bondRewardFlashSeconds = 0.0F;
            }
        }));
    }

    /** Testing-only: wipes this UUID's cloud adoptions/currency/bond-claim clock so
     *  the whole flow can be run through again from scratch. Two clicks required -
     *  see the button's construction comment in init() for why. */
    private void resetAccountProgress() {
        if (this.minecraft == null || this.minecraft.player == null || this.resetProgressButton == null) return;
        if (!this.resetArmed) {
            this.resetArmed = true;
            this.resetProgressButton.setMessage(Component.literal("Click again to confirm"));
            return;
        }
        UUID uuid = this.minecraft.player.getUUID();
        String secret = PlayerKeystore.loadSecret(uuid);
        if (secret == null) {
            this.resetProgressButton.setMessage(Component.literal("No account to reset"));
            return;
        }
        this.resetProgressButton.active = false;
        this.resetProgressButton.setMessage(Component.literal("Resetting..."));
        PetsCloudClient.resetAccount(uuid, secret).thenAccept(resp -> Minecraft.getInstance().execute(() -> {
            this.resetArmed = false;
            if (this.resetProgressButton != null) this.resetProgressButton.active = true;
            if (resp == null || resp.error != null) {
                if (this.resetProgressButton != null) {
                    this.resetProgressButton.setMessage(Component.literal("Failed - try again"));
                }
                return;
            }
            // mirror the wipe locally so the UI reflects it immediately instead of
            // waiting on the next refreshOwnedPets()/refreshCurrencyBalance() poll
            CONFIG.ownedSpecies.clear();
            CONFIG.currencyBalanceCache = 0;
            CONFIG.lastNonStarterAdoptionAt = 0L;
            CONFIG.hasAdoptedStarterPet = false;
            this.saveConfig();
            if (this.resetProgressButton != null) {
                this.resetProgressButton.setMessage(Component.literal("Reset Progress"));
            }
            this.rebuildGrid();
            this.updateSummonState();
        }));
    }

    /** Registers a widget as catalog-only: hidden while the naming page is open. */
    private <T extends net.minecraft.client.gui.components.AbstractWidget> T track(T widget) {
        this.catalogWidgets.add(widget);
        return this.addRenderableWidget(widget);
    }

    private void enterNaming() {
        if (this.selected == null || !this.selected.name().equals(CONFIG.activePet)) return;
        this.naming = true;
        AbstractPet pet = this.selectedPreview();
        this.nameBox.setValue(pet != null ? pet.getPlainTextName() : "");
        this.setFocused(this.nameBox);
        this.updateContentVisibility();
    }

    private void exitNaming() {
        this.naming = false;
        this.draggingTag = false;
        this.draggingDandelion = false;
        this.updateContentVisibility();
    }

    /** Hides the catalog (tabs/search/grid/equip panel) behind whichever single
     *  overlay page is active - naming or the adopt prompt - so it actually behaves
     *  like a modal instead of leaving every button underneath it live and
     *  clickable. Previously only gated on naming; the adopt prompt never blocked
     *  anything behind it at all, so the whole species grid stayed fully
     *  interactive while it was open - the main cause of menus reading as stacked
     *  on top of each other rather than one screen at a time.
     *
     *  Null-checks nameBox/namingCancelButton: closeAdoptPrompt() (which calls this)
     *  is itself called once from inside init(), right after the adopt-prompt
     *  buttons are constructed, to set their initial hidden state - but nameBox and
     *  namingCancelButton aren't constructed until the naming-page section further
     *  down the same init() method, so at that one call site both are still null.
     *  Crashed every launch until this was guarded (found via an actual crash
     *  report, not caught in review - the ordering dependency was missed). */
    private void updateContentVisibility() {
        boolean blocked = this.naming || this.adoptPromptSpecies != null;
        for (net.minecraft.client.gui.components.AbstractWidget w : this.catalogWidgets) {
            w.visible = !blocked;
        }
        for (Button w : this.gridWidgets) {
            w.visible = !blocked;
        }
        if (this.nameBox != null) {
            this.nameBox.visible = this.naming;
        }
        if (this.namingCancelButton != null) {
            this.namingCancelButton.visible = this.naming;
        }
    }

    // --- drag-the-nametag-onto-the-pet interaction ---

    private static final int TAG_ICON_SIZE = 16;

    private int tagDrawX() {
        return this.draggingTag ? (int) (this.dragMouseX - TAG_ICON_SIZE / 2.0) : this.tagRestX;
    }

    private int tagDrawY() {
        return this.draggingTag ? (int) (this.dragMouseY - TAG_ICON_SIZE / 2.0) : this.tagRestY;
    }

    private boolean overTagIcon(double mouseX, double mouseY) {
        int x = this.tagRestX;
        int y = this.tagRestY;
        return mouseX >= x && mouseX < x + TAG_ICON_SIZE && mouseY >= y && mouseY < y + TAG_ICON_SIZE;
    }

    private boolean overPetStage(double mouseX, double mouseY) {
        return mouseX >= this.previewX && mouseX < this.previewX + this.previewW
                && mouseY >= this.previewY && mouseY < this.previewY + this.previewH;
    }

    // --- drag-a-golden-dandelion-onto-the-pet interaction (baby lock) ---

    private int dandelionDrawX() {
        return this.draggingDandelion ? (int) (this.dragMouseX - TAG_ICON_SIZE / 2.0) : this.dandelionRestX;
    }

    private int dandelionDrawY() {
        return this.draggingDandelion ? (int) (this.dragMouseY - TAG_ICON_SIZE / 2.0) : this.dandelionRestY;
    }

    private boolean overDandelionIcon(double mouseX, double mouseY) {
        int x = this.dandelionRestX;
        int y = this.dandelionRestY;
        return mouseX >= x && mouseX < x + TAG_ICON_SIZE && mouseY >= y && mouseY < y + TAG_ICON_SIZE;
    }

    /** Locks the baby pet's age and kicks off a small confirmation flash. */
    private void applyGoldenDandelion() {
        if (!CONFIG.isBaby || CONFIG.babyLocked) return;
        Central.applyGoldenDandelion();
        this.lockFlashSeconds = 0.0F;
    }

    @Override
    public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event, boolean doubleClick) {
        if (this.naming && event.button() == 0 && this.overTagIcon(event.x(), event.y())) {
            this.draggingTag = true;
            this.dragMouseX = event.x();
            this.dragMouseY = event.y();
            return true;
        }
        if (this.naming && CONFIG.isBaby && !CONFIG.babyLocked && event.button() == 0
                && this.overDandelionIcon(event.x(), event.y())) {
            this.draggingDandelion = true;
            this.dragMouseX = event.x();
            this.dragMouseY = event.y();
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(net.minecraft.client.input.MouseButtonEvent event, double dragX, double dragY) {
        if (this.draggingTag || this.draggingDandelion) {
            this.dragMouseX = event.x();
            this.dragMouseY = event.y();
            return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(net.minecraft.client.input.MouseButtonEvent event) {
        if (this.draggingTag) {
            this.draggingTag = false;
            if (this.overPetStage(event.x(), event.y())) {
                this.applyNameTag();
            }
            return true;
        }
        if (this.draggingDandelion) {
            this.draggingDandelion = false;
            if (this.overPetStage(event.x(), event.y())) {
                this.applyGoldenDandelion();
            }
            return true;
        }
        return super.mouseReleased(event);
    }

    /** Writes the typed name and kicks off the settling dangle animation. */
    private void applyNameTag() {
        String name = this.nameBox.getValue().trim();
        // naming only ever targets the pet currently out, matching the "Name Tag..."
        // button's own enable check - this can't silently switch the active pet
        if (name.isEmpty() || this.selected == null || !this.selected.name().equals(CONFIG.activePet)) return;
        Central.setActivePetName(name);
        Central.refreshPetNames();
        this.dangleName = name;
        this.dangleSeconds = 0.0F;
        this.exitNaming();
    }

    /** Maps species ids to Central's pre-built pet instances for the preview. */
    private static Map<String, AbstractPet> previewPets() {
        if (previewPets == null || previewPets.isEmpty()) {
            previewPets = new HashMap<>();
            for (Field field : Central.class.getFields()) {
                if (AbstractPet.class.isAssignableFrom(field.getType())) {
                    String snake = field.getName().replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase(Locale.ROOT);
                    try {
                        Object value = field.get(null);
                        if (value != null) {
                            previewPets.put(snake, (AbstractPet) value);
                        }
                    } catch (IllegalAccessException ignored) {
                    }
                }
            }
        }
        return previewPets;
    }

    // Central's shared static instances (above) are only safe for ONE preview on
    // screen at a time - confirmed before building this: they default to entity
    // id=0 (ClientLevel never implements getNextEntityId(), same root cause
    // AdoptionScreen already had to work around for its own preview tiles), AND
    // whichever one matches the currently-active species is the literal live
    // in-world entity (invisible mid-spawn-animation, replaced wholesale on every
    // summon). A whole grid of them at once needs its own dedicated, disposable,
    // uniquely-IDed pool instead - built reflectively off each species' own
    // Central instance (borrowing its class + real EntityType) rather than a
    // hand-written 77-case switch, since every Client<Species> class was verified
    // to share the same (EntityType, Level) constructor.
    private static final java.util.concurrent.atomic.AtomicInteger GRID_PREVIEW_IDS =
            new java.util.concurrent.atomic.AtomicInteger(-200_000);
    private final Map<PetList, AbstractPet> gridPreviewCache = new HashMap<>();

    private AbstractPet gridPreview(PetList species) {
        return this.gridPreviewCache.computeIfAbsent(species, this::freshGridPreview);
    }

    private AbstractPet freshGridPreview(PetList species) {
        if (this.minecraft == null || this.minecraft.level == null) return null;
        AbstractPet template = previewPets().get(species.name().toLowerCase(Locale.ROOT));
        if (template == null) return null;
        try {
            EntityType<?> type = template.getType();
            Constructor<?> ctor = template.getClass().getConstructor(EntityType.class, Level.class);
            AbstractPet fresh = (AbstractPet) ctor.newInstance(type, this.minecraft.level);
            fresh.setId(GRID_PREVIEW_IDS.decrementAndGet());
            return fresh;
        } catch (ReflectiveOperationException | ClassCastException e) {
            io.github.aaronateataco.petsandpals.PetsInitializer.LOGGER.error(
                    "[Pets&Pals] couldn't build a grid preview for '{}'", species.name(), e);
            return null;
        }
    }

    private AbstractPet selectedPreview() {
        if (this.selected == null) return null;
        return previewPets().get(this.selected.name().toLowerCase(Locale.ROOT));
    }

    private boolean unlocked(PetList species) {
        return this.testingCatalog || PUBLIC_PETS.contains(species.name());
    }

    /** Whether this UUID has ever adopted this species (starter pet included) - the
     *  currently-active species always counts even before the first cloud refresh
     *  completes, so a legacy/offline player isn't gated on their own starter pet. */
    private boolean owned(PetList species) {
        String name = species.name();
        return name.equals(CONFIG.activePet) || (CONFIG.ownedSpecies != null && CONFIG.ownedSpecies.contains(name));
    }

    private void refreshOwnedPets() {
        if (this.minecraft == null || this.minecraft.player == null) return;
        UUID uuid = this.minecraft.player.getUUID();
        PetsCloudClient.getOwnedPets(uuid).thenAccept(resp -> Minecraft.getInstance().execute(() -> {
            if (resp == null || resp.error != null || resp.species == null) return; // offline - keep local cache
            CONFIG.ownedSpecies = new java.util.HashSet<>(resp.species);
            if (resp.lastNonStarterAdoptionAt != null) {
                CONFIG.lastNonStarterAdoptionAt = resp.lastNonStarterAdoptionAt;
            }
            this.rebuildGrid();
        }));
    }

    private void refreshCurrencyBalance() {
        if (this.minecraft == null || this.minecraft.player == null) return;
        UUID uuid = this.minecraft.player.getUUID();
        String secret = PlayerKeystore.loadSecret(uuid);
        if (secret == null) return; // never adopted online yet - nothing to check
        PetsCloudClient.getCurrencyBalance(uuid, secret).thenAccept(resp -> Minecraft.getInstance().execute(() -> {
            if (resp == null || resp.error != null || resp.balance == null) return;
            CONFIG.currencyBalanceCache = resp.balance;
            this.refreshAdoptPromptState();
        }));
    }

    private boolean cooldownActive() {
        return System.currentTimeMillis() < CONFIG.lastNonStarterAdoptionAt + 3L * 24 * 60 * 60 * 1000;
    }

    private void openAdoptPrompt(PetList species) {
        this.adoptPromptSpecies = species;
        this.adoptStatusMessage = null;
        this.adoptInFlight = false;
        this.refreshAdoptPromptState();
        this.refreshCurrencyBalance();
        this.updateContentVisibility();
    }

    private void closeAdoptPrompt() {
        this.adoptPromptSpecies = null;
        this.adoptStatusMessage = null;
        this.adoptInFlight = false;
        this.adoptFreeButton.visible = false;
        this.adoptSkipButton.visible = false;
        this.buyCoinsButton.visible = false;
        this.adoptCancelButton.visible = false;
        this.updateContentVisibility();
    }

    private void refreshAdoptPromptState() {
        if (this.adoptPromptSpecies == null) return;
        boolean interactable = !this.adoptInFlight;
        boolean cooldown = this.cooldownActive();
        this.adoptFreeButton.visible = !cooldown;
        this.adoptFreeButton.active = interactable && !cooldown;
        this.adoptSkipButton.visible = cooldown;
        this.adoptSkipButton.active = interactable && cooldown && CONFIG.currencyBalanceCache >= SKIP_COOLDOWN_COST;
        this.adoptSkipButton.setMessage(Component.literal(
                "Skip wait (" + SKIP_COOLDOWN_COST + " coins, have " + CONFIG.currencyBalanceCache + ")"));
        this.buyCoinsButton.visible = true;
        this.buyCoinsButton.active = interactable;
        this.adoptCancelButton.visible = true;
        this.adoptCancelButton.active = interactable;
    }

    private void confirmAdopt(boolean skipCooldown) {
        if (this.adoptPromptSpecies == null || this.minecraft == null || this.minecraft.player == null || this.adoptInFlight) return;
        UUID uuid = this.minecraft.player.getUUID();
        String secret = PlayerKeystore.loadSecret(uuid);
        if (secret == null) {
            this.adoptStatusMessage = "Adopt your starter pet online first";
            return;
        }
        PetList species = this.adoptPromptSpecies;
        String modVersion = net.fabricmc.loader.api.FabricLoader.getInstance()
                .getModContainer(PetsInitializer.MOD_ID)
                .map(c -> c.getMetadata().getVersion().getFriendlyString())
                .orElse("unknown");
        this.adoptInFlight = true;
        this.adoptStatusMessage = "Adopting...";
        this.refreshAdoptPromptState();
        PetsCloudClient.adoptAdditional(uuid, secret, species.name().toLowerCase(Locale.ROOT), skipCooldown, modVersion)
                .thenAccept(resp -> Minecraft.getInstance().execute(() -> this.handleAdoptAdditionalResponse(species, skipCooldown, resp)));
    }

    private void handleAdoptAdditionalResponse(PetList species, boolean skipCooldown, AdoptAdditionalResponse resp) {
        this.adoptInFlight = false;
        if (resp == null) {
            this.adoptStatusMessage = "No connection - try again later";
            this.refreshAdoptPromptState();
            return;
        }
        if (resp.error == null) {
            CONFIG.ownedSpecies.add(species.name());
            if (skipCooldown) {
                CONFIG.currencyBalanceCache = Math.max(0, CONFIG.currencyBalanceCache - SKIP_COOLDOWN_COST);
            }
            if (resp.adoptedAt != null) {
                CONFIG.lastNonStarterAdoptionAt = resp.adoptedAt;
            }
            this.saveConfig();
            this.selected = species;
            this.applySelected();
            this.closeAdoptPrompt();
            this.rebuildGrid();
            this.updateSummonState();
            return;
        }
        switch (resp.error) {
            case "cooldown_active" -> {
                if (resp.nextEligibleAt != null) {
                    CONFIG.lastNonStarterAdoptionAt = resp.nextEligibleAt - 3L * 24 * 60 * 60 * 1000;
                }
                this.adoptStatusMessage = "Still on cooldown";
            }
            case "insufficient_currency" -> this.adoptStatusMessage = "Not enough Paw Coins";
            case "invalid_credentials" -> this.adoptStatusMessage = "Adopt your starter pet online first";
            default -> this.adoptStatusMessage = "Something went wrong - try again later";
        }
        this.refreshAdoptPromptState();
    }

    private void buyCurrency() {
        if (this.minecraft == null || this.minecraft.player == null) return;
        UUID uuid = this.minecraft.player.getUUID();
        String secret = PlayerKeystore.loadSecret(uuid);
        if (secret == null) {
            this.adoptStatusMessage = "Adopt your starter pet online first";
            return;
        }
        this.adoptStatusMessage = "Opening checkout in your browser...";
        // one fixed pack for now (see worker.js's CURRENCY_PACKS) - a pack picker is
        // a natural follow-up, not essential for the mechanic to work end to end
        PetsCloudClient.createCurrencyCheckout(uuid, secret, "medium").thenAccept(resp -> Minecraft.getInstance().execute(() -> {
            if (resp == null || resp.checkoutUrl == null) {
                this.adoptStatusMessage = "Couldn't start checkout - try again later";
                return;
            }
            Util.getPlatform().openUri(resp.checkoutUrl);
            this.adoptStatusMessage = "Check your browser - balance updates shortly after payment";
        }));
    }

    /** Arrow buttons: browse the roster without summoning anything. */
    private void cycleSelected(int direction) {
        if (this.filtered.isEmpty()) return;
        int i = this.filtered.indexOf(this.selected);
        this.selected = this.filtered.get(Math.floorMod(i + direction, this.filtered.size()));
        this.page = Math.max(0, this.filtered.indexOf(this.selected)) / this.pageSize();
        this.rebuildGrid();
        this.updateSummonState();
    }

    private void updateSummonState() {
        if (this.summonButton != null) {
            this.summonButton.active = this.selected != null && this.unlocked(this.selected);
        }
        boolean isActivePet = this.selected != null && this.selected.name().equals(CONFIG.activePet);
        if (this.nameTagButton != null) {
            // naming only applies to the pet you actually have out
            this.nameTagButton.active = isActivePet;
        }
        if (this.skinButton != null) {
            if (this.minecraft != null) {
                // keeps /petskin's own chat suggestions in sync too - they used to go
                // stale after switching species from this screen instead of the command
                Central.updateSuggestions(this.minecraft);
            }
            this.skinButton.active = isActivePet && !this.availableSkins().isEmpty();
            this.skinButton.setMessage(this.skinLabel());
        }
    }

    private int pageSize() {
        return this.columns * this.rows;
    }

    private Component raftWoodLabel() {
        String wood = CONFIG.raftWood == null ? "spruce" : CONFIG.raftWood;
        String pretty = wood.replace('_', ' ');
        return Component.literal("Raft: " + Character.toUpperCase(pretty.charAt(0)) + pretty.substring(1));
    }

    private Component cushionLabel() {
        String dye = CONFIG.cushionColor == null ? "none" : CONFIG.cushionColor;
        String pretty = dye.equals("none") ? "none" : dye.replace('_', ' ');
        return Component.literal("Cushion: " + Character.toUpperCase(pretty.charAt(0)) + pretty.substring(1));
    }

    // --- pet skin cycling ---
    //
    // Species skins live in per-species CONFIG fields (catSkin, foxSkin, ...) that
    // only /petskin's giant per-species switch statement knows how to write safely -
    // that command is flagged in its own comment as fragile ("re-created from
    // bytecode"), so this reuses it wholesale via the same public path the chat
    // input takes (ClientPacketListener#sendCommand, which Fabric's client-command
    // mixin intercepts before it'd ever reach the server) instead of duplicating or
    // touching that logic. Reading the *current* skin uses reflection against the
    // matching CONFIG field, mirroring the same pattern already used by
    // previewPets() above for a config-field lookup by convention rather than a
    // hardcoded switch.

    private static String skinFieldName(String species) {
        StringBuilder sb = new StringBuilder();
        boolean upperNext = false;
        for (char c : species.toCharArray()) {
            if (c == '_') {
                upperNext = true;
                continue;
            }
            sb.append(upperNext ? Character.toUpperCase(c) : c);
            upperNext = false;
        }
        sb.append("Skin");
        return sb.toString();
    }

    /** Every valid skin name for the active pet, or empty if it doesn't have any. */
    private List<String> availableSkins() {
        return Central.currentSuggestions.stream()
                .filter(s -> !s.equals("baby") && !s.equals("adult"))
                .toList();
    }

    /** The active pet's current skin, normalized to match the suggestion list's spacing. */
    private String currentSkin() {
        try {
            Field field = PetsConfig.class.getField(skinFieldName(CONFIG.activePet));
            Object value = field.get(CONFIG);
            return value == null ? null : value.toString().replace('_', ' ');
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }

    private void cycleSkin() {
        if (this.minecraft == null || this.minecraft.player == null) return;
        List<String> skins = this.availableSkins();
        if (skins.isEmpty()) return;
        int i = skins.indexOf(this.currentSkin());
        String next = skins.get((i + 1) % skins.size());
        this.minecraft.player.connection.sendCommand("petskin " + next);
    }

    private Component skinLabel() {
        String skin = this.currentSkin();
        if (skin == null || skin.isEmpty()) return Component.literal("Skin: default");
        return Component.literal("Skin: " + Character.toUpperCase(skin.charAt(0)) + skin.substring(1));
    }

    /** Throwaway raft entity used only to extract a render state for the preview. */
    private io.github.aaronateataco.petsandpals.mob.PetRaft raftPreview() {
        if (this.raftPreview == null && this.minecraft != null && this.minecraft.level != null) {
            var raft = new io.github.aaronateataco.petsandpals.mob.PetRaft(
                    io.github.aaronateataco.petsandpals.PetsInitializer.PET_RAFT, this.minecraft.level);
            String[] woods = io.github.aaronateataco.petsandpals.mob.PetRaftBlock.WOODS;
            int style = Math.max(0, java.util.Arrays.asList(woods).indexOf(CONFIG.raftWood));
            int cushion = "none".equals(CONFIG.cushionColor)
                    ? io.github.aaronateataco.petsandpals.mob.PetRaftBlock.NO_CUSHION
                    : Math.max(0, java.util.Arrays.asList(io.github.aaronateataco.petsandpals.mob.PetRaftBlock.DYES)
                            .indexOf(CONFIG.cushionColor));
            raft.blockState = io.github.aaronateataco.petsandpals.PetsInitializer.PET_RAFT_BLOCK.defaultBlockState()
                    .setValue(io.github.aaronateataco.petsandpals.mob.PetRaftBlock.STYLE, style)
                    .setValue(io.github.aaronateataco.petsandpals.mob.PetRaftBlock.CUSHION, cushion);
            this.raftPreview = raft;
        }
        return this.raftPreview;
    }

    private Component petToggleLabel() {
        return Component.literal(Boolean.TRUE.equals(CONFIG.petOn) ? "Pet: ON" : "Pet: OFF");
    }

    private Component babyToggleLabel() {
        return Component.literal(CONFIG.isBaby ? "Baby" : "Adult");
    }

    private Component filterLabel() {
        return Component.literal(switch (this.ownerFilter) {
            case ALL -> "All"; case OWNED -> "Owned"; case NEW -> "New";
        });
    }

    private void applyFilter() {
        String q = this.query.trim().toLowerCase(Locale.ROOT);
        this.filtered = this.allSpecies.stream()
                .filter(p -> this.element == Element.ALL || elementOf(p) == this.element)
                .filter(p -> q.isEmpty() || p.getDisplayName().getString().toLowerCase(Locale.ROOT).contains(q))
                .filter(p -> switch (this.ownerFilter) {
                    case ALL -> true;
                    case OWNED -> this.owned(p);
                    case NEW -> !this.owned(p);
                })
                .toList();
    }

    private void rebuildGrid() {
        this.gridWidgets.forEach(this::removeWidget);
        this.gridWidgets.clear();

        int start = this.page * this.pageSize();
        for (int i = 0; i < this.pageSize() && start + i < this.filtered.size(); i++) {
            PetList species = this.filtered.get(start + i);
            int col = i % this.columns;
            int row = i / this.columns;
            int x = this.gridLeft + col * (GRID_CELL + GRID_GAP);
            int y = this.gridTop + row * (GRID_CELL + GRID_GAP);
            boolean isActive = species.name().equals(CONFIG.activePet);
            boolean isPublic = PUBLIC_PETS.contains(species.name());
            boolean unlocked = isPublic || this.testingCatalog;
            boolean isOwned = this.owned(species);

            // picture-only tile instead of a text-labeled button - hover tooltip
            // still carries the species name, a corner badge carries owned/needs-
            // adopting status instead of the old "✔ "/"$ "/"T " text prefixes
            PreviewTileButton.Badge badge = (isActive || isOwned) ? PreviewTileButton.Badge.OWNED
                    : unlocked ? PreviewTileButton.Badge.NEEDS_ADOPTING : PreviewTileButton.Badge.NONE;
            Button cell = PreviewTileButton.of(x, y, GRID_CELL, this.gridPreview(species),
                    species == this.selected, badge, b -> {
                this.selected = species;
                if (this.owned(species)) {
                    this.applySelected();
                    this.rebuildGrid();
                    this.updateSummonState();
                } else {
                    this.openAdoptPrompt(species);
                }
            });
            cell.active = unlocked && species != this.selected;
            String tooltip = species.getDisplayName().getString()
                    + (this.testingCatalog && !isPublic ? " (testing)" : !unlocked ? " - Coming soon" : "");
            cell.setTooltip(net.minecraft.client.gui.components.Tooltip.create(Component.literal(tooltip)));
            this.gridWidgets.add(this.addRenderableWidget(cell));
        }

        int maxPage = Math.max(0, (this.filtered.size() - 1) / this.pageSize());
        this.page = Mth.clamp(this.page, 0, maxPage);
        this.prevButton.active = this.page > 0;
        this.nextButton.active = this.page < maxPage;
    }

    // clicking a species applies it right away
    private void applySelected() {
        if (this.selected == null || !this.unlocked(this.selected)) return;
        CONFIG.activePet = this.selected.name();
        this.saveConfig();
        if (this.minecraft != null && this.minecraft.level != null && Boolean.TRUE.equals(CONFIG.petOn)) {
            Central.despawnPet();
            Central.summonPet();
            Central.refreshChatSuggestor(this.minecraft);
        }
    }

    private void summonSelected() {
        if (this.selected == null || !this.unlocked(this.selected)) return;
        CONFIG.activePet = this.selected.name();
        CONFIG.petOn = true;
        this.saveConfig();
        Central.despawnPet();
        Central.summonPet();
        if (this.minecraft != null) {
            Central.refreshChatSuggestor(this.minecraft);
        }
        this.rebuildGrid();
        this.updateSummonState();
    }

    private void saveConfig() {
        AutoConfig.getConfigHolder(PetsConfig.class).save();
    }

    @Override
    public void extractRenderState(@NotNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        // background/panels first, widgets (super) after: render commands paint in
        // queue order, so queuing widgets before this dim would let it paint
        // directly over them - same fix as AdoptionScreen's Continue button needed.
        //
        // a light dim (not the old near-opaque tint) plus isPauseScreen() above -
        // the live world is meant to actually show through now, overlay-style,
        // rather than the screen reading as a solid dark wall with the game
        // nowhere in evidence behind it. The panel below gives all the buttons a
        // visible boundary without needing to touch any of their existing,
        // already-tuned positions.
        graphics.fill(0, 0, this.width, this.height, 0x400B0B0D);
        Theme.drawInset(graphics, this.leftPanelLeft, this.leftPanelTop,
                this.leftPanelRight - this.leftPanelLeft, this.leftPanelBottom - this.leftPanelTop);
        // the adopt prompt no longer draws its own separate bordered box here - now
        // that it actually hides the catalog behind it (see updateContentVisibility),
        // a second nested border on top of the panel above just looked like two
        // menus stacked on each other instead of one dialog on one panel
        if (this.searchBox != null && this.searchBox.visible) {
            // vanilla EditBox's own border reads as near-invisible against this
            // theme's dark backdrop, making it look like a stray black rectangle -
            // give it an explicit themed background so it reads as an intentional
            // input field instead
            Theme.drawInset(graphics, this.searchBox.getX() - 2, this.searchBox.getY() - 2,
                    this.searchBox.getWidth() + 4, this.searchBox.getHeight() + 4);
        }
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        graphics.text(this.font, this.title, this.width / 2 - this.font.width(this.title) / 2, 10, Theme.TEXT_HEADER);
        if (this.naming) {
            this.renderNamingPage(graphics, mouseX, mouseY);
        } else {
            graphics.text(this.font, Component.literal("Page " + (this.page + 1) + "/"
                            + (Math.max(0, (this.filtered.size() - 1) / this.pageSize()) + 1)
                            + "  (" + this.filtered.size() + " pets)"),
                    this.contentLeft + 50, this.height - 22, Theme.TEXT_SECONDARY);
        }

        for (Element el : Element.values()) {
            Identifier icon = CATEGORY_ICONS.get(el);
            Integer y = this.tabIconY.get(el);
            if (icon != null && y != null) {
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, icon, this.tabIconX, y, 10, 10);
                String label = switch (el) {
                    case ALL -> "All"; case LAND -> "Land"; case SKY -> "Sky"; case SEA -> "Sea";
                };
                graphics.text(this.font, Component.literal(label), this.tabTextX, y, 0xFFFFFFFF);
            }
        }

        // equip panel header + persistent currency readout - the right-side buttons
        // (Summon/Skin/Baby/Speed/...) are the "equipping" section; this just labels
        // it as one and shows the Paw Coin balance the adopt prompt already spends
        int equipPanelX = this.width - PANEL_WIDTH - 6;
        int equipHeaderY = GRID_TOP + PREVIEW_HEIGHT - 24;
        graphics.text(this.font, Component.literal("Equip"), equipPanelX, equipHeaderY, Theme.TEXT_HEADER);
        String coins = "Paw Coins: " + CONFIG.currencyBalanceCache;
        graphics.text(this.font, Component.literal(coins),
                equipPanelX + PANEL_WIDTH - this.font.width(coins), equipHeaderY, Theme.TEXT_SECONDARY);
        if (this.bondRewardFlashSeconds >= 0.0F && this.bondRewardMessage != null) {
            float alpha = Mth.clamp(1.0F - this.bondRewardFlashSeconds / 3.0F, 0.0F, 1.0F);
            int color = 0xFF6CD242 | ((int) (alpha * 255.0F) << 24);
            graphics.text(this.font, Component.literal(this.bondRewardMessage),
                    equipPanelX + PANEL_WIDTH - this.font.width(this.bondRewardMessage),
                    equipHeaderY + 10, color);
        }

        if (this.adoptPromptSpecies != null) {
            // skip the preview stage/pet render entirely while the adopt modal is
            // open - its box can genuinely overlap screen-center on wide layouts,
            // and everything below this point draws after (therefore on top of) the
            // adoptFreeButton/adoptSkipButton/buyCoinsButton/adoptCancelButton
            // widgets super.extractRenderState() already drew above
            this.renderAdoptPrompt(graphics);
            return;
        }

        int boxLeft = this.previewX;
        int boxTop = this.previewY;
        int boxRight = this.previewX + this.previewW;
        int boxBottom = this.previewY + this.previewH;
        Theme.drawTile(graphics, boxLeft - 2, boxTop - 2, this.previewW + 4, this.previewH + 4, false);

        // name + origin centered under the stage
        if (this.selected != null) {
            String name = this.selected.getDisplayName().getString();
            String origin = originOf(this.selected);
            int nameWidth = this.font.width(name);
            int cx = boxLeft + this.previewW / 2;
            graphics.text(this.font, Component.literal(name), cx - nameWidth / 2, boxBottom + 4, Theme.TEXT_HEADER);
            graphics.text(this.font, Component.literal(origin),
                    cx - this.font.width(origin) / 2, boxBottom + 14, Theme.TEXT_SECONDARY);
        }

        // hovering the raft/cushion buttons swaps the stage to the raft itself
        boolean raftHover = (this.raftWoodButton != null && this.raftWoodButton.isHovered())
                || (this.cushionButton != null && this.cushionButton.isHovered());
        if (raftHover) {
            var raft = this.raftPreview();
            if (raft != null && this.minecraft != null) {
                @SuppressWarnings({"rawtypes", "unchecked"})
                net.minecraft.client.renderer.entity.EntityRenderer renderer =
                        this.minecraft.getEntityRenderDispatcher().getRenderer(raft);
                @SuppressWarnings("unchecked")
                net.minecraft.client.renderer.entity.state.EntityRenderState state =
                        renderer.createRenderState(raft, 1.0f);
                state.shadowPieces.clear();
                state.outlineColor = 0;
                // slow turntable spin with a slight top-down tilt
                float spin = (System.currentTimeMillis() % 8000L) / 8000.0F * ((float) Math.PI * 2.0F);
                org.joml.Quaternionf tilt = new org.joml.Quaternionf().rotateX(-0.5F);
                org.joml.Quaternionf pose = new org.joml.Quaternionf().rotateZ((float) Math.PI)
                        .mul(tilt).rotateY(spin);
                graphics.entity(state, this.previewH * 0.62F,
                        new org.joml.Vector3f(0.0F, 0.35F, 0.0F), pose, tilt,
                        boxLeft, boxTop, boxRight, boxBottom);
                graphics.text(this.font, this.raftWoodLabel(), boxLeft + 4, boxBottom - 10, 0xFFAAAAAA);
                this.renderOverlays(graphics, partialTick);
                return;
            }
            graphics.text(this.font, Component.literal("Raft preview needs a loaded world"),
                    boxLeft + 4, boxTop + this.previewH / 2, 0xFF888888);
            this.renderOverlays(graphics, partialTick);
            return;
        }

        // center stage: the pet big, the player alongside at the same scale for size
        AbstractPet pet = this.selectedPreview();
        LivingEntity player = this.minecraft != null ? this.minecraft.player : null;
        if (pet != null && player != null && !pet.isRemoved()) {
            float tallest = Math.max(player.getBbHeight(), pet.getBbHeight());
            int scale = Math.max(10, (int) ((this.previewH - 16) / tallest));
            int petPane = boxLeft + (int) (this.previewW * 0.62);
            InventoryScreen.extractEntityInInventoryFollowsMouse(graphics,
                    boxLeft, boxTop, petPane, boxBottom,
                    scale, 0.0625F, mouseX, mouseY, pet);
            InventoryScreen.extractEntityInInventoryFollowsMouse(graphics,
                    petPane + 4, boxTop, boxRight, boxBottom,
                    scale, 0.0625F, mouseX, mouseY, player);
            graphics.text(this.font, Component.literal(String.format(Locale.ROOT, "%.1fm", pet.getBbHeight())),
                    boxLeft + 4, boxBottom - 10, 0xFFAAAAAA);
            graphics.text(this.font, Component.literal(String.format(Locale.ROOT, "%.1fm", player.getBbHeight())),
                    petPane + 8, boxBottom - 10, 0xFFAAAAAA);
        } else {
            graphics.text(this.font, Component.literal("Previews need a loaded world"),
                    boxLeft + 4, boxTop + this.previewH / 2, 0xFF888888);
        }
        this.renderOverlays(graphics, partialTick);
    }

    /** Anvil naming page: type a name, then drag the tag icon onto the pet stage. */
    private void renderNamingPage(@NotNull GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        int panelLeft = this.tagRestX - 6;
        int panelTop = this.tagRestY - 6;
        // no separate bordered box here anymore - it's already sitting on the one
        // big panel the whole screen draws now, and a second nested border just
        // looked like two menus stacked on top of each other
        graphics.text(this.font, Component.literal("Name your pet"), panelLeft + 4, panelTop - 10, Theme.TEXT_HEADER);

        // the tag icon itself is drawn last (see renderOverlays) so it stays on
        // top while being dragged across the rest of the screen
        boolean overStage = this.overPetStage(mouseX, mouseY);
        graphics.text(this.font,
                Component.literal(this.draggingTag
                        ? (overStage ? "Release to attach!" : "Drag onto your pet")
                        : "Drag the tag onto your pet to apply the name"),
                panelLeft + 4, this.tagRestY + TAG_ICON_SIZE + 6,
                overStage && this.draggingTag ? 0xFF55FF55 : 0xFFAAAAAA);

        if (this.draggingTag && this.overPetStage(this.dragMouseX, this.dragMouseY)) {
            graphics.outline(this.previewX, this.previewY,
                    this.previewX + this.previewW, this.previewY + this.previewH, 0xFF55FF55);
        }

        if (CONFIG.isBaby) {
            String hint;
            if (CONFIG.babyLocked) {
                hint = "Golden dandelion applied - stays a baby";
            } else if (this.draggingDandelion) {
                hint = overStage ? "Release to lock!" : "Drag onto your pet";
            } else {
                hint = "Drag the golden dandelion onto your pet to keep it a baby";
            }
            graphics.text(this.font, Component.literal(hint), panelLeft + 4, this.dandelionRestY + TAG_ICON_SIZE + 6,
                    overStage && this.draggingDandelion ? 0xFF55FF55 : 0xFFAAAAAA);
            if (this.draggingDandelion && this.overPetStage(this.dragMouseX, this.dragMouseY)) {
                graphics.outline(this.previewX, this.previewY,
                        this.previewX + this.previewW, this.previewY + this.previewH, 0xFF55FF55);
            }
        }
    }

    /** Text inside the adopt-prompt panel (the panel background itself is drawn
     *  earlier, before super.extractRenderState(), so the buttons land on top of it -
     *  see the comment on that call). */
    private void renderAdoptPrompt(@NotNull GuiGraphicsExtractor graphics) {
        if (this.adoptPromptSpecies == null) return;
        int textLeft = this.adoptPanelLeft + 8;
        int cx = this.adoptPanelLeft + this.adoptPanelW / 2;
        String name = this.adoptPromptSpecies.getDisplayName().getString();
        String title = "Adopt " + name + "?";
        graphics.text(this.font, Component.literal(title), cx - this.font.width(title) / 2,
                this.adoptPanelTop + 6, Theme.TEXT_HEADER);
        String status = this.adoptStatusMessage != null ? this.adoptStatusMessage
                : this.cooldownActive() ? "On cooldown - pay to adopt now, or wait it out"
                : "Ready to adopt - free!";
        graphics.text(this.font, Component.literal(status), textLeft, this.adoptPanelTop + 20, Theme.TEXT_SECONDARY);
    }

    /** Draws whatever floats above the normal layout: the dragged tag, the settling
     *  dangle animation, and the close fade - always last so nothing else covers them. */
    private void renderOverlays(@NotNull GuiGraphicsExtractor graphics, float partialTick) {
        if (this.naming) {
            graphics.item(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.NAME_TAG),
                    this.tagDrawX(), this.tagDrawY());
            if (CONFIG.isBaby && !CONFIG.babyLocked) {
                graphics.item(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.GOLDEN_DANDELION),
                        this.dandelionDrawX(), this.dandelionDrawY());
            }
        }

        if (this.lockFlashSeconds >= 0.0F) {
            float alpha = Mth.clamp(1.0F - this.lockFlashSeconds / 1.5F, 0.0F, 1.0F);
            int color = 0xFFFFFF55 | ((int) (alpha * 255.0F) << 24);
            String msg = "Locked in as a baby!";
            int cx = this.previewX + this.previewW / 2;
            int ty = this.previewY + this.previewH - 4;
            graphics.text(this.font, Component.literal(msg), cx - this.font.width(msg) / 2, ty, color);
        }

        if (this.dangleSeconds >= 0.0F && this.dangleName != null) {
            // a light spring-damper swing settling under the pet's head
            float t = this.dangleSeconds;
            float decay = (float) Math.exp(-t * 3.0);
            float swing = (float) Math.sin(t * 14.0) * 6.0F * decay;
            int cx = this.previewX + (int) (this.previewW * 0.31);
            int ty = this.previewY + this.previewH - 4;
            graphics.pose().pushMatrix();
            graphics.pose().rotateAbout((float) Math.toRadians(swing), cx, ty);
            String tag = this.dangleName;
            graphics.text(this.font, Component.literal(tag), cx - this.font.width(tag) / 2, ty, 0xFFFFFF55);
            graphics.pose().popMatrix();
        }

        if (this.closeFade >= 0.0F) {
            int alpha = (int) (Mth.clamp(this.closeFade, 0.0F, 1.0F) * 255.0F) << 24;
            graphics.fill(0, 0, this.width, this.height, alpha);
        }
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
        if (this.lockFlashSeconds >= 0.0F) {
            this.lockFlashSeconds += 1.0F / 20.0F;
            if (this.lockFlashSeconds > 1.5F) {
                this.lockFlashSeconds = -1.0F;
            }
        }
        if (this.closeFade >= 0.0F) {
            this.closeFade += 1.0F / 8.0F;
            if (this.closeFade >= 1.0F) {
                this.finishClose();
            }
        }
        if (this.bondRewardFlashSeconds >= 0.0F) {
            this.bondRewardFlashSeconds += 1.0F / 20.0F;
            if (this.bondRewardFlashSeconds > 3.0F) {
                this.bondRewardFlashSeconds = -1.0F;
                this.bondRewardMessage = null;
            }
        }
    }

    @Override
    public void onClose() {
        // fade to black first, then actually swap screens - see finishClose()
        if (this.closeFade < 0.0F) {
            this.closeFade = 0.0F;
        }
    }

    private void finishClose() {
        this.saveConfig();
        AbstractPet.speedMultiplier = () -> CONFIG.petSpeed;
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }

    /** Simple labeled value slider mapping 0..1 to [min, max]. */
    private static class PercentSlider extends AbstractSliderButton {
        private final String label;
        private final double min;
        private final double max;
        private final DoubleConsumer setter;

        PercentSlider(int x, int y, int width, int height, String label,
                      double min, double max, double current, DoubleConsumer setter) {
            super(x, y, width, height, Component.empty(), (current - min) / (max - min));
            this.label = label;
            this.min = min;
            this.max = max;
            this.setter = setter;
            this.updateMessage();
        }

        private double current() {
            return this.min + this.value * (this.max - this.min);
        }

        @Override
        protected void updateMessage() {
            this.setMessage(Component.literal(String.format(Locale.ROOT, "%s: %.2fx", this.label, this.current())));
        }

        @Override
        protected void applyValue() {
            this.setter.accept(this.current());
        }
    }
}
