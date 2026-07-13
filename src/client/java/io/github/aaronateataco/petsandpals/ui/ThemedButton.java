package io.github.aaronateataco.petsandpals.ui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

/**
 * Drop-in replacement for {@code Button.builder(...)} that draws through
 * {@link Theme} instead of vanilla's own button sprite - see {@link Theme}'s javadoc
 * for why vanilla {@code Button} can't just be reskinned from the outside.
 */
public class ThemedButton extends Button {

    private Integer backgroundColorOverride;

    public ThemedButton(int x, int y, int width, int height, Component message, OnPress onPress) {
        super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
    }

    public static ThemedButton of(int x, int y, int width, int height, Component message, OnPress onPress) {
        return new ThemedButton(x, y, width, height, message, onPress);
    }

    /** Flat color fill instead of the shared grey chrome sprite - for the Shop's
     *  color-coded category tabs, which each need their own distinct color rather
     *  than blending into every other button. Square, not nine-slice: these tabs
     *  are the one place a flat fill plus a border reads better than stretching a
     *  fixed-corner-radius sprite across a size the theme wasn't drawn for. */
    public ThemedButton withBackgroundColor(int argb) {
        this.backgroundColorOverride = argb;
        return this;
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        if (this.backgroundColorOverride != null) {
            int color = this.backgroundColorOverride;
            if (!this.active) {
                color = shade(color, 0.4F);
            } else if (this.isHoveredOrFocused()) {
                color = shade(color, 1.25F);
            }
            graphics.fill(this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), color);
            graphics.outline(this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(),
                    shade(color, 0.6F) | 0xFF000000);
        } else {
            Theme.ButtonState state = !this.active ? Theme.ButtonState.DISABLED
                    : this.isHoveredOrFocused() ? Theme.ButtonState.HOVER : Theme.ButtonState.NORMAL;
            Theme.drawButtonBackground(graphics, this.getX(), this.getY(), this.getWidth(), this.getHeight(), state);
        }
        this.extractDefaultLabel(graphics.textRendererForWidget(this, GuiGraphicsExtractor.HoveredTextEffects.NONE));
    }

    private static int shade(int argb, float factor) {
        int alpha = argb & 0xFF000000;
        int r = Mth.clamp((int) (((argb >> 16) & 0xFF) * factor), 0, 255);
        int g = Mth.clamp((int) (((argb >> 8) & 0xFF) * factor), 0, 255);
        int b = Mth.clamp((int) ((argb & 0xFF) * factor), 0, 255);
        return alpha | (r << 16) | (g << 8) | b;
    }
}
