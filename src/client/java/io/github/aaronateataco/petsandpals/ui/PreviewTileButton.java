package io.github.aaronateataco.petsandpals.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;

/**
 * A square catalog tile that shows a live entity preview instead of a text label -
 * the Shop's species grid used to be plain {@link ThemedButton}s with the species
 * name written out; this replaces that with a Marketplace-style picture-only pick,
 * keeping {@link ThemedButton}'s click/hover/active/tooltip handling (the species
 * name still shows on hover, just not written out permanently on every tile).
 */
public class PreviewTileButton extends ThemedButton {

    // fraction of the tile the entity's own bounding-box height maps to - matches
    // AdoptionScreen's PREVIEW_FILL_RATIO so every preview across the mod reads at
    // the same "generous headroom, nothing cropped at the edges" scale
    private static final float PREVIEW_FILL_RATIO = 0.45F;

    /** null = not owned/adopted yet but unlocked (shows a $ badge); ignored (no
     *  badge at all) when the tile is inactive (locked/"coming soon"). */
    public enum Badge { NONE, OWNED, NEEDS_ADOPTING }

    private final LivingEntity preview;
    private final boolean selected;
    private final Badge badge;

    public PreviewTileButton(int x, int y, int size, LivingEntity preview, boolean selected, Badge badge, OnPress onPress) {
        super(x, y, size, size, Component.empty(), onPress);
        this.preview = preview;
        this.selected = selected;
        this.badge = badge;
    }

    public static PreviewTileButton of(int x, int y, int size, LivingEntity preview, boolean selected, Badge badge, OnPress onPress) {
        return new PreviewTileButton(x, y, size, preview, selected, badge, onPress);
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        Theme.drawTile(graphics, this.getX(), this.getY(), this.getWidth(), this.getHeight(), this.selected);
        if (!this.active) {
            // dim instead of a separate disabled sprite - this tile has no text
            // label to grey out the vanilla way, so mute the whole thing instead
            graphics.fill(this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), 0x90000000);
        }
        if (this.preview != null && !this.preview.isRemoved()) {
            int size = this.getWidth();
            int scale = Math.max(8, (int) (size * PREVIEW_FILL_RATIO / Math.max(0.3F, this.preview.getBbHeight())));
            InventoryScreen.extractEntityInInventoryFollowsMouse(graphics,
                    this.getX(), this.getY(), this.getX() + size, this.getY() + size,
                    scale, 0.0625F, this.getX() + size / 2, this.getY() + size, this.preview);
        }
        if (this.active && this.badge != Badge.NONE) {
            String glyph = this.badge == Badge.OWNED ? "✔" : "$";
            int color = this.badge == Badge.OWNED ? Theme.ACCENT : 0xFFFFD966;
            Font font = Minecraft.getInstance().font;
            graphics.text(font, Component.literal(glyph),
                    this.getX() + this.getWidth() - font.width(glyph) - 3,
                    this.getY() + this.getHeight() - 10, color);
        }
    }
}
