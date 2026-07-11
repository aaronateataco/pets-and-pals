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
    private final List<PetList> allSpecies;
    private List<PetList> filtered;
    private final List<Button> gridWidgets = new ArrayList<>();
    private EditBox searchBox;
    private Button prevButton;
    private Button nextButton;
    private PetList selected;
    private int page = 0;
    private int columns = 3;
    private int rows = 6;
    private String query = "";
    private Element element = Element.ALL;

    // element categories
    private enum Element { ALL, LAND, SKY, SEA }

    private static final java.util.Set<String> SKY_PETS = java.util.Set.of(
            "allay", "bat", "bee", "blaze", "breeze", "ghast", "happy_ghast", "angry_ghast",
            "parrot", "phantom", "vex", "wither", "pink_wither", "ender_dragon");
    private static final java.util.Set<String> SEA_PETS = java.util.Set.of(
            "squid", "cod", "salmon", "tropical_fish", "pufferfish", "tadpole", "axolotl",
            "dolphin", "nautilus", "guardian", "elder_guardian", "koi", "stingray",
            "dumbo_octopus", "turtle", "plaguewhale_slab", "toxifin_slab");

    private static Element elementOf(PetList species) {
        String name = species.name().toLowerCase(Locale.ROOT);
        if (SEA_PETS.contains(name)) return Element.SEA;
        if (SKY_PETS.contains(name)) return Element.SKY;
        return Element.LAND;
    }

    public MenagerieScreen(Screen parent) {
        super(Component.literal("Menagerie"));
        this.parent = parent;
        this.allSpecies = new ArrayList<>(List.of(PetList.values()));
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
        int gridAreaWidth = this.width - PANEL_WIDTH - 24;
        this.columns = Math.max(2, gridAreaWidth / (CELL_WIDTH + CELL_GAP));
        this.rows = Math.max(3, (this.height - GRID_TOP - 40) / (CELL_HEIGHT + CELL_GAP));

        this.searchBox = new EditBox(this.font, 12, 26, Math.min(220, gridAreaWidth - 4), 18,
                Component.literal("Search"));
        this.searchBox.setValue(this.query);
        this.searchBox.setResponder(text -> {
            this.query = text;
            this.page = 0;
            this.applyFilter();
            this.rebuildGrid();
        });
        this.addRenderableWidget(this.searchBox);

        // element tabs
        int tabX = this.searchBox.getX() + this.searchBox.getWidth() + 6;
        for (Element el : Element.values()) {
            Element tabElement = el;
            Button tab = Button.builder(Component.literal(switch (el) {
                case ALL -> "All"; case LAND -> "Land"; case SKY -> "Sky"; case SEA -> "Sea";
            }), b -> {
                this.element = tabElement;
                this.page = 0;
                this.applyFilter();
                this.rebuildGrid();
            }).bounds(tabX, 26, 34, 18).build();
            tabX += 36;
            this.addRenderableWidget(tab);
        }

        int pageY = this.height - 28;
        this.prevButton = this.addRenderableWidget(Button.builder(Component.literal("<"), b -> {
            if (this.page > 0) this.page--;
            this.rebuildGrid();
        }).bounds(12, pageY, 20, 20).build());
        this.nextButton = this.addRenderableWidget(Button.builder(Component.literal(">"), b -> {
            if ((this.page + 1) * this.pageSize() < this.filtered.size()) this.page++;
            this.rebuildGrid();
        }).bounds(36, pageY, 20, 20).build());

        int panelX = this.width - PANEL_WIDTH - 6;
        int y = GRID_TOP + PREVIEW_HEIGHT + 4;
        this.addRenderableWidget(Button.builder(Component.literal("Summon"), b -> this.summonSelected())
                .bounds(panelX, y, PANEL_WIDTH, 20).build());
        y += 24;
        this.addRenderableWidget(Button.builder(this.petToggleLabel(), b -> {
            CONFIG.petOn = !Boolean.TRUE.equals(CONFIG.petOn);
            if (CONFIG.petOn) Central.summonPet();
            else Central.despawnPet();
            b.setMessage(this.petToggleLabel());
        }).bounds(panelX, y, PANEL_WIDTH, 20).build());
        y += 24;
        this.addRenderableWidget(new PercentSlider(panelX, y, PANEL_WIDTH, 20, "Speed", 0.25, 3.0,
                CONFIG.petSpeed, value -> CONFIG.petSpeed = (float) value));
        y += 24;
        this.addRenderableWidget(new PercentSlider(panelX, y, PANEL_WIDTH, 20, "Volume", 0.0, 1.0,
                CONFIG.petVolume == null ? 1.0f : CONFIG.petVolume, value -> CONFIG.petVolume = (float) value));
        y += 28;
        this.addRenderableWidget(Button.builder(Component.literal("Advanced settings..."), b -> {
            if (this.minecraft != null) {
                this.minecraft.setScreen(PetsConfigScreen.getInstance().getAdvancedConfigScreenFactory().create(this));
            }
        }).bounds(panelX, y, PANEL_WIDTH, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Done"), b -> this.onClose())
                .bounds(panelX, this.height - 28, PANEL_WIDTH, 20).build());

        this.applyFilter();
        this.rebuildGrid();
    }

    /** Maps species ids to Central's pre-built pet instances for the preview. */
    private static Map<String, AbstractPet> previewPets() {
        if (previewPets == null) {
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

    private int pageSize() {
        return this.columns * this.rows;
    }

    private Component petToggleLabel() {
        return Component.literal(Boolean.TRUE.equals(CONFIG.petOn) ? "Pet: ON" : "Pet: OFF");
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
            int x = 12 + col * (CELL_WIDTH + CELL_GAP);
            int y = GRID_TOP + row * (CELL_HEIGHT + CELL_GAP);
            boolean isActive = species.name().equals(CONFIG.activePet);
            String label = (isActive ? "✔ " : "") + species.getDisplayName().getString();
            Button cell = Button.builder(Component.literal(label), b -> {
                this.selected = species;
                this.applySelected();
                this.rebuildGrid();
            }).bounds(x, y, CELL_WIDTH, CELL_HEIGHT).build();
            cell.active = species != this.selected;
            this.gridWidgets.add(this.addRenderableWidget(cell));
        }

        int maxPage = Math.max(0, (this.filtered.size() - 1) / this.pageSize());
        this.page = Mth.clamp(this.page, 0, maxPage);
        this.prevButton.active = this.page > 0;
        this.nextButton.active = this.page < maxPage;
    }

    // clicking a species applies it right away
    private void applySelected() {
        if (this.selected == null) return;
        CONFIG.activePet = this.selected.name();
        this.saveConfig();
        if (this.minecraft != null && this.minecraft.level != null && Boolean.TRUE.equals(CONFIG.petOn)) {
            Central.despawnPet();
            Central.summonPet();
            Central.refreshChatSuggestor(this.minecraft);
        }
    }

    private void summonSelected() {
        if (this.selected == null) return;
        CONFIG.activePet = this.selected.name();
        CONFIG.petOn = true;
        this.saveConfig();
        Central.despawnPet();
        Central.summonPet();
        if (this.minecraft != null) {
            Central.refreshChatSuggestor(this.minecraft);
        }
        this.rebuildGrid();
    }

    private void saveConfig() {
        AutoConfig.getConfigHolder(PetsConfig.class).save();
    }

    @Override
    public void extractRenderState(@NotNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        graphics.text(this.font, this.title, this.width / 2 - this.font.width(this.title) / 2, 10, 0xFFFFFFFF);
        graphics.text(this.font, Component.literal("Page " + (this.page + 1) + "/"
                        + (Math.max(0, (this.filtered.size() - 1) / this.pageSize()) + 1)
                        + "  (" + this.filtered.size() + " pets)"),
                62, this.height - 22, 0xFFAAAAAA);

        int panelX = this.width - PANEL_WIDTH - 6;
        if (this.selected != null) {
            graphics.text(this.font, Component.literal("Selected:"), panelX, 28, 0xFFAAAAAA);
            graphics.text(this.font, this.selected.getDisplayName(), panelX, 38, 0xFFFFFFFF);
        }

        // live preview: pet next to the player at one shared scale
        AbstractPet pet = this.selectedPreview();
        LivingEntity player = this.minecraft != null ? this.minecraft.player : null;
        int boxTop = GRID_TOP;
        int boxBottom = GRID_TOP + PREVIEW_HEIGHT;
        if (pet != null && player != null && !pet.isRemoved()) {
            float tallest = Math.max(player.getBbHeight(), pet.getBbHeight());
            int scale = Math.max(8, (int) ((PREVIEW_HEIGHT - 20) / tallest));
            int half = PANEL_WIDTH / 2;
            InventoryScreen.extractEntityInInventoryFollowsMouse(graphics,
                    panelX, boxTop, panelX + half - 2, boxBottom,
                    scale, 0.0625F, mouseX, mouseY, pet);
            InventoryScreen.extractEntityInInventoryFollowsMouse(graphics,
                    panelX + half + 2, boxTop, panelX + PANEL_WIDTH, boxBottom,
                    scale, 0.0625F, mouseX, mouseY, player);
            graphics.text(this.font, Component.literal(String.format(Locale.ROOT, "%.1fm", pet.getBbHeight())),
                    panelX + 6, boxBottom - 10, 0xFFAAAAAA);
            graphics.text(this.font, Component.literal(String.format(Locale.ROOT, "%.1fm", player.getBbHeight())),
                    panelX + half + 8, boxBottom - 10, 0xFFAAAAAA);
        } else {
            graphics.text(this.font, Component.literal("Previews need"), panelX, boxTop + 30, 0xFF888888);
            graphics.text(this.font, Component.literal("a loaded world"), panelX, boxTop + 42, 0xFF888888);
        }
    }

    @Override
    public void onClose() {
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
