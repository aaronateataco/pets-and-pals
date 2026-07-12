package io.github.aaronateataco.petsandpals.gui;

import io.github.aaronateataco.petsandpals.Central;
import io.github.aaronateataco.petsandpals.PetsConfig;
import io.github.aaronateataco.petsandpals.PetsConfigScreen;
import io.github.aaronateataco.petsandpals.enums.PetList;
import io.github.aaronateataco.petsandpals.mob.AbstractPet;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.entity.LivingEntity;

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
    private static final int CELL_WIDTH = 96;
    private static final int CELL_HEIGHT = 20;
    private static final int CELL_GAP = 3;
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
    private int previewX;
    private int previewY;
    private int previewW;
    private int previewH;
    private int gridTop = GRID_TOP;
    private int gridLeft = 12;
    private int page = 0;
    private int columns = 3;
    private int rows = 6;
    private String query = "";
    private Element element = Element.ALL;

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

    // element categories
    private enum Element { ALL, LAND, SKY, SEA }

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

    /** Where a species comes from: vanilla if the base game registers the same id. */
    private static String originOf(PetList species) {
        String name = species.name().toLowerCase(Locale.ROOT);
        return net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE
                .containsKey(net.minecraft.resources.Identifier.withDefaultNamespace(name))
                ? "Vanilla" : "Pets&Pals";
    }

    private static Element elementOf(PetList species) {
        String name = species.name().toLowerCase(Locale.ROOT);
        if (SEA_PETS.contains(name)) return Element.SEA;
        if (SKY_PETS.contains(name)) return Element.SKY;
        return Element.LAND;
    }

    public MenagerieScreen(Screen parent) {
        super(Component.literal("Menagerie"));
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
    protected void init() {
        int leftWidth = this.width - PANEL_WIDTH - 24;

        // the pet takes center stage: big preview up top, arrows to flip through
        this.previewW = Math.min(210, leftWidth - 64);
        this.previewH = Math.max(96, Math.min(120, this.height / 4));
        this.previewX = 12 + (leftWidth - this.previewW) / 2;
        this.previewY = 22;
        int arrowY = this.previewY + this.previewH / 2 - 10;
        this.addRenderableWidget(Button.builder(Component.literal("<"), b -> this.cycleSelected(-1))
                .bounds(this.previewX - 26, arrowY, 20, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal(">"), b -> this.cycleSelected(1))
                .bounds(this.previewX + this.previewW + 6, arrowY, 20, 20).build());

        // element toggles sit under the preview, search under those
        int tabY = this.previewY + this.previewH + 24;
        int tabX = 12 + (leftWidth - 4 * 36 + 2) / 2;
        for (Element el : Element.values()) {
            Element tabElement = el;
            Button tab = Button.builder(Component.literal(switch (el) {
                case ALL -> "All"; case LAND -> "Land"; case SKY -> "Sky"; case SEA -> "Sea";
            }), b -> {
                this.element = tabElement;
                this.page = 0;
                this.applyFilter();
                this.rebuildGrid();
            }).bounds(tabX, tabY, 34, 18).build();
            // sky/sea catalogs open up alongside the rest of the roster
            if ((el == Element.SKY || el == Element.SEA) && !this.testingCatalog) {
                tab.active = false;
                tab.setTooltip(net.minecraft.client.gui.components.Tooltip.create(
                        Component.literal("Coming soon")));
            }
            tabX += 36;
            this.track(tab);
        }

        int searchWidth = Math.min(220, leftWidth - 8);
        this.searchBox = new EditBox(this.font, 12 + (leftWidth - searchWidth) / 2, tabY + 22,
                searchWidth, 18, Component.literal("Search"));
        this.searchBox.setValue(this.query);
        this.searchBox.setResponder(text -> {
            this.query = text;
            this.page = 0;
            this.applyFilter();
            this.rebuildGrid();
        });
        this.track(this.searchBox);

        this.gridTop = tabY + 46;
        this.columns = Math.max(2, leftWidth / (CELL_WIDTH + CELL_GAP));
        this.rows = Math.max(2, (this.height - this.gridTop - 34) / (CELL_HEIGHT + CELL_GAP));
        this.gridLeft = 12 + (leftWidth - (this.columns * (CELL_WIDTH + CELL_GAP) - CELL_GAP)) / 2;

        int pageY = this.height - 28;
        this.prevButton = this.track(Button.builder(Component.literal("<"), b -> {
            if (this.page > 0) this.page--;
            this.rebuildGrid();
        }).bounds(12, pageY, 20, 20).build());
        this.nextButton = this.track(Button.builder(Component.literal(">"), b -> {
            if ((this.page + 1) * this.pageSize() < this.filtered.size()) this.page++;
            this.rebuildGrid();
        }).bounds(36, pageY, 20, 20).build());

        int panelX = this.width - PANEL_WIDTH - 6;
        int y = GRID_TOP + PREVIEW_HEIGHT + 4;
        this.summonButton = this.track(Button.builder(Component.literal("Summon"), b -> this.summonSelected())
                .bounds(panelX, y, PANEL_WIDTH, 20).build());
        this.updateSummonState();
        y += 24;
        this.track(Button.builder(this.petToggleLabel(), b -> {
            CONFIG.petOn = !Boolean.TRUE.equals(CONFIG.petOn);
            if (CONFIG.petOn) Central.summonPet();
            else Central.despawnPet();
            b.setMessage(this.petToggleLabel());
        }).bounds(panelX, y, PANEL_WIDTH, 20).build());
        y += 24;
        this.nameTagButton = this.track(Button.builder(Component.literal("Name Tag..."), b -> this.enterNaming())
                .bounds(panelX, y, PANEL_WIDTH, 20).build());
        y += 24;
        this.babyToggleButton = this.track(Button.builder(this.babyToggleLabel(), b -> {
            Central.setPetBaby(!CONFIG.isBaby);
            b.setMessage(this.babyToggleLabel());
        }).bounds(panelX, y, PANEL_WIDTH, 20).build());
        y += 24;
        this.track(new PercentSlider(panelX, y, PANEL_WIDTH, 20, "Speed", 0.25, 3.0,
                CONFIG.petSpeed, value -> CONFIG.petSpeed = (float) value));
        y += 24;
        this.track(new PercentSlider(panelX, y, PANEL_WIDTH, 20, "Volume", 0.0, 1.0,
                CONFIG.petVolume == null ? 1.0f : CONFIG.petVolume, value -> CONFIG.petVolume = (float) value));
        y += 24;
        this.raftWoodButton = this.track(Button.builder(this.raftWoodLabel(), b -> {
            String[] woods = io.github.aaronateataco.petsandpals.mob.PetRaftBlock.WOODS;
            int i = java.util.Arrays.asList(woods).indexOf(CONFIG.raftWood);
            CONFIG.raftWood = woods[(i + 1) % woods.length];
            this.raftPreview = null;
            b.setMessage(this.raftWoodLabel());
        }).bounds(panelX, y, PANEL_WIDTH, 20).build());
        y += 24;
        this.cushionButton = this.track(Button.builder(this.cushionLabel(), b -> {
            // cycles through the 16 dyes plus a bare deck
            List<String> options = new ArrayList<>(List.of(io.github.aaronateataco.petsandpals.mob.PetRaftBlock.DYES));
            options.add("none");
            int i = options.indexOf(CONFIG.cushionColor);
            CONFIG.cushionColor = options.get((i + 1) % options.size());
            this.raftPreview = null;
            b.setMessage(this.cushionLabel());
        }).bounds(panelX, y, PANEL_WIDTH, 20).build());
        y += 28;
        this.track(Button.builder(Component.literal("Advanced settings..."), b -> {
            if (this.minecraft != null) {
                this.minecraft.gui.setScreen(PetsConfigScreen.getInstance().getAdvancedConfigScreenFactory().create(this));
            }
        }).bounds(panelX, y, PANEL_WIDTH, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Done"), b -> this.onClose())
                .bounds(panelX, this.height - 28, PANEL_WIDTH, 20).build());

        // anvil naming page: a name tag icon the player drags onto the pet stage
        this.tagRestX = 12 + leftWidth / 2 - 80;
        this.tagRestY = tabY;
        this.dandelionRestX = this.tagRestX;
        this.dandelionRestY = this.tagRestY + 40;
        this.nameBox = new EditBox(this.font, this.tagRestX + 26, tabY, Math.min(160, leftWidth - 100), 18,
                Component.literal("Name"));
        this.nameBox.setMaxLength(32);
        this.nameBox.visible = false;
        this.addRenderableWidget(this.nameBox);
        this.namingCancelButton = Button.builder(Component.literal("Cancel"), b -> this.exitNaming())
                .bounds(this.tagRestX, tabY + 28, 106, 20).build();
        this.namingCancelButton.visible = false;
        this.addRenderableWidget(this.namingCancelButton);

        this.applyFilter();
        this.rebuildGrid();
        this.applyNamingVisibility();
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
        this.applyNamingVisibility();
    }

    private void exitNaming() {
        this.naming = false;
        this.draggingTag = false;
        this.draggingDandelion = false;
        this.applyNamingVisibility();
    }

    private void applyNamingVisibility() {
        for (net.minecraft.client.gui.components.AbstractWidget w : this.catalogWidgets) {
            w.visible = !this.naming;
        }
        for (Button w : this.gridWidgets) {
            w.visible = !this.naming;
        }
        this.nameBox.visible = this.naming;
        this.namingCancelButton.visible = this.naming;
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

    private AbstractPet selectedPreview() {
        if (this.selected == null) return null;
        return previewPets().get(this.selected.name().toLowerCase(Locale.ROOT));
    }

    private boolean unlocked(PetList species) {
        return this.testingCatalog || PUBLIC_PETS.contains(species.name());
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
        if (this.nameTagButton != null) {
            // naming only applies to the pet you actually have out
            this.nameTagButton.active = this.selected != null
                    && this.selected.name().equals(CONFIG.activePet);
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

    private void applyFilter() {
        String q = this.query.trim().toLowerCase(Locale.ROOT);
        this.filtered = this.allSpecies.stream()
                .filter(p -> this.element == Element.ALL || elementOf(p) == this.element)
                .filter(p -> q.isEmpty() || p.getDisplayName().getString().toLowerCase(Locale.ROOT).contains(q))
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
            int x = this.gridLeft + col * (CELL_WIDTH + CELL_GAP);
            int y = this.gridTop + row * (CELL_HEIGHT + CELL_GAP);
            boolean isActive = species.name().equals(CONFIG.activePet);
            boolean isPublic = PUBLIC_PETS.contains(species.name());
            boolean unlocked = isPublic || this.testingCatalog;
            String label = (isActive ? "✔ " : "") + species.getDisplayName().getString();
            if (this.testingCatalog && !isPublic) {
                label = "⚗ " + label;
            }
            Button cell = Button.builder(Component.literal(label), b -> {
                this.selected = species;
                this.applySelected();
                this.rebuildGrid();
                this.updateSummonState();
            }).bounds(x, y, CELL_WIDTH, CELL_HEIGHT).build();
            cell.active = unlocked && species != this.selected;
            if (!unlocked) {
                cell.setTooltip(net.minecraft.client.gui.components.Tooltip.create(
                        Component.literal("Coming soon")));
            }
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
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        graphics.text(this.font, this.title, this.width / 2 - this.font.width(this.title) / 2, 10, 0xFFFFFFFF);
        if (this.naming) {
            this.renderNamingPage(graphics, mouseX, mouseY);
        } else {
            graphics.text(this.font, Component.literal("Page " + (this.page + 1) + "/"
                            + (Math.max(0, (this.filtered.size() - 1) / this.pageSize()) + 1)
                            + "  (" + this.filtered.size() + " pets)"),
                    62, this.height - 22, 0xFFAAAAAA);
        }

        int boxLeft = this.previewX;
        int boxTop = this.previewY;
        int boxRight = this.previewX + this.previewW;
        int boxBottom = this.previewY + this.previewH;

        // name + origin centered under the stage
        if (this.selected != null) {
            String name = this.selected.getDisplayName().getString();
            String origin = originOf(this.selected);
            int nameWidth = this.font.width(name);
            int cx = boxLeft + this.previewW / 2;
            graphics.text(this.font, Component.literal(name), cx - nameWidth / 2, boxBottom + 4, 0xFFFFFFFF);
            graphics.text(this.font, Component.literal(origin),
                    cx - this.font.width(origin) / 2, boxBottom + 14, 0xFF777777);
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
        int panelRight = this.nameBox.getX() + this.nameBox.getWidth() + 6;
        int panelBottom = this.tagRestY + TAG_ICON_SIZE + 44 + (CONFIG.isBaby ? 40 : 0);
        graphics.fill(panelLeft, panelTop, panelRight, panelBottom, 0xC0101010);
        graphics.outline(panelLeft, panelTop, panelRight, panelBottom, 0xFF555555);
        graphics.text(this.font, Component.literal("Name your pet"), panelLeft + 4, panelTop - 10, 0xFFFFFFFF);

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
            this.minecraft.gui.setScreen(this.parent);
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
