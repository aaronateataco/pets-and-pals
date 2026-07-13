package io.github.aaronateataco.petsandpals.ui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.Mth;

import java.util.List;
import java.util.function.Consumer;

/**
 * Small self-contained horizontal scroll strip - not a vanilla {@code AbstractWidget},
 * just a state+render helper the owning screen drives directly (no scrollable widget
 * exists anywhere else in this codebase to build on; the Menagerie uses {@code <}/{@code >}
 * pagination instead). Built for the adoption screen's always-visible variant gallery,
 * but generic over the cell item type so it isn't tied to pet skins specifically.
 */
public class ScrollRow<T> {

    public interface CellRenderer<T> {
        void render(GuiGraphicsExtractor graphics, T item, int x, int y, int width, int height,
                    boolean hovered, boolean selected);
    }

    // must stay >= the scrollbar sprites' nine-slice border*2 (see gen_theme_textures.py)
    // or the destination corner regions eat the whole strip and leave no center to stretch
    private static final int SCROLLBAR_HEIGHT = 12;
    private static final int SCROLLBAR_GAP = 3;

    private final int x;
    private final int y;
    private final int width;
    private final int height;
    private final int cellWidth;
    private final int cellGap;
    private final CellRenderer<T> renderer;
    private final Consumer<T> onSelect;

    private List<T> items = List.of();
    private T selected;
    private int scroll = 0;

    public ScrollRow(int x, int y, int width, int height, int cellWidth, int cellGap,
                      CellRenderer<T> renderer, Consumer<T> onSelect) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.cellWidth = cellWidth;
        this.cellGap = cellGap;
        this.renderer = renderer;
        this.onSelect = onSelect;
    }

    public void setItems(List<T> items) {
        this.items = items;
        this.scroll = this.clampScroll(this.scroll);
    }

    public void setSelected(T selected) {
        this.selected = selected;
    }

    private int rowHeight() {
        return this.hasScrollbar() ? this.height - SCROLLBAR_HEIGHT - SCROLLBAR_GAP : this.height;
    }

    private int contentWidth() {
        return this.items.isEmpty() ? 0 : this.items.size() * (this.cellWidth + this.cellGap) - this.cellGap;
    }

    private boolean hasScrollbar() {
        return this.contentWidth() > this.width;
    }

    private int maxScroll() {
        return Math.max(0, this.contentWidth() - this.width);
    }

    private int clampScroll(int value) {
        return Mth.clamp(value, 0, this.maxScroll());
    }

    private boolean contains(double mouseX, double mouseY) {
        return mouseX >= this.x && mouseX < this.x + this.width && mouseY >= this.y && mouseY < this.y + this.height;
    }

    public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        int rowHeight = this.rowHeight();
        graphics.enableScissor(this.x, this.y, this.x + this.width, this.y + rowHeight);
        int cx = this.x - this.scroll;
        for (T item : this.items) {
            if (cx + this.cellWidth >= this.x && cx <= this.x + this.width) {
                boolean hovered = mouseX >= cx && mouseX < cx + this.cellWidth
                        && mouseY >= this.y && mouseY < this.y + rowHeight;
                this.renderer.render(graphics, item, cx, this.y, this.cellWidth, rowHeight,
                        hovered, item.equals(this.selected));
            }
            cx += this.cellWidth + this.cellGap;
        }
        graphics.disableScissor();

        if (this.hasScrollbar()) {
            int trackY = this.y + rowHeight + SCROLLBAR_GAP;
            Theme.drawScrollTrack(graphics, this.x, trackY, this.width, SCROLLBAR_HEIGHT);
            int maxScroll = this.maxScroll();
            int thumbWidth = Math.max(16, this.width * this.width / this.contentWidth());
            int thumbX = this.x + (this.width - thumbWidth) * this.scroll / maxScroll;
            Theme.drawScrollThumb(graphics, thumbX, trackY, thumbWidth, SCROLLBAR_HEIGHT);
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0 || !this.contains(mouseX, mouseY) || mouseY >= this.y + this.rowHeight()) {
            return false;
        }
        int cx = this.x - this.scroll;
        for (T item : this.items) {
            if (mouseX >= cx && mouseX < cx + this.cellWidth) {
                this.onSelect.accept(item);
                return true;
            }
            cx += this.cellWidth + this.cellGap;
        }
        return false;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollDeltaY) {
        if (!this.contains(mouseX, mouseY) || !this.hasScrollbar()) {
            return false;
        }
        this.scroll = this.clampScroll(this.scroll - (int) (scrollDeltaY * 24));
        return true;
    }
}
