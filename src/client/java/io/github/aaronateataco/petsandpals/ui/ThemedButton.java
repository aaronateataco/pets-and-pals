package io.github.aaronateataco.petsandpals.ui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

/**
 * Drop-in replacement for {@code Button.builder(...)} that draws through
 * {@link Theme} instead of vanilla's own button sprite - see {@link Theme}'s javadoc
 * for why vanilla {@code Button} can't just be reskinned from the outside.
 */
public class ThemedButton extends Button {

    public ThemedButton(int x, int y, int width, int height, Component message, OnPress onPress) {
        super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
    }

    public static ThemedButton of(int x, int y, int width, int height, Component message, OnPress onPress) {
        return new ThemedButton(x, y, width, height, message, onPress);
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        Theme.ButtonState state = !this.active ? Theme.ButtonState.DISABLED
                : this.isHoveredOrFocused() ? Theme.ButtonState.HOVER : Theme.ButtonState.NORMAL;
        Theme.drawButtonBackground(graphics, this.getX(), this.getY(), this.getWidth(), this.getHeight(), state);
        this.extractDefaultLabel(graphics.textRendererForWidget(this, GuiGraphicsExtractor.HoveredTextEffects.NONE));
    }
}
