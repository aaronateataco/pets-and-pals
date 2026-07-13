package io.github.aaronateataco.petsandpals.ui;

import io.github.aaronateataco.petsandpals.PetsInitializer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

/**
 * Pets &amp; Pals' original dark, square-cornered UI theme (green accent, sampled
 * from the mod's own icon). Every panel, button, and tile in this palette is drawn
 * from the nine-slice sprites under {@code textures/gui/sprites/theme/} rather than
 * vanilla's own widget sprites - vanilla's {@code Button}/{@code AbstractSliderButton}
 * render through their own hardcoded sprites that nothing drawn on top of them can
 * change, so anything that wants this theme has to be its own widget (see
 * {@link ThemedButton}).
 */
public final class Theme {

    private Theme() {
    }

    private static Identifier sprite(String name) {
        return Identifier.fromNamespaceAndPath(PetsInitializer.MOD_ID, "theme/" + name);
    }

    private static final Identifier INSET = sprite("inset");
    private static final Identifier BUTTON = sprite("button");
    private static final Identifier BUTTON_HOVER = sprite("button_hover");
    private static final Identifier BUTTON_DISABLED = sprite("button_disabled");
    private static final Identifier TILE = sprite("tile");
    private static final Identifier TILE_SELECTED = sprite("tile_selected");
    private static final Identifier SCROLLBAR_TRACK = sprite("scrollbar_track");
    private static final Identifier SCROLLBAR_THUMB = sprite("scrollbar_thumb");

    public static final int TEXT_PRIMARY = 0xFFF2F3F7;
    public static final int TEXT_SECONDARY = 0xFFA0A6B8;
    public static final int TEXT_DISABLED = 0xFF5A5F6E;
    // green, sampled from the mod's own icon rather than Essential's blue - see
    // gen_theme_textures.py's palette comment for the exact source colors. Chrome
    // (panels/buttons/tiles) is neutral grey with no color tint; green is reserved
    // for headers, selection state, and other accents - never a background fill.
    public static final int ACCENT = 0xFF6CD242;
    public static final int TEXT_HEADER = ACCENT;

    public enum ButtonState { NORMAL, HOVER, DISABLED }

    /** A slightly darker, recessed panel - the big detail pane and header strips. */
    public static void drawInset(GuiGraphicsExtractor graphics, int x, int y, int width, int height) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, INSET, x, y, width, height);
    }

    public static void drawButtonBackground(GuiGraphicsExtractor graphics, int x, int y, int width, int height,
                                             ButtonState state) {
        Identifier sprite = switch (state) {
            case NORMAL -> BUTTON;
            case HOVER -> BUTTON_HOVER;
            case DISABLED -> BUTTON_DISABLED;
        };
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y, width, height);
    }

    public static void drawTile(GuiGraphicsExtractor graphics, int x, int y, int width, int height,
                                 boolean selected) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, selected ? TILE_SELECTED : TILE, x, y, width, height);
    }

    public static void drawScrollTrack(GuiGraphicsExtractor graphics, int x, int y, int width, int height) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SCROLLBAR_TRACK, x, y, width, height);
    }

    public static void drawScrollThumb(GuiGraphicsExtractor graphics, int x, int y, int width, int height) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SCROLLBAR_THUMB, x, y, width, height);
    }
}
