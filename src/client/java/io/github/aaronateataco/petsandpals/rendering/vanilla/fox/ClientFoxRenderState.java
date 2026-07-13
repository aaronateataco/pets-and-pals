package io.github.aaronateataco.petsandpals.rendering.vanilla.fox;

import net.minecraft.client.renderer.entity.state.FoxRenderState;
import org.jetbrains.annotations.Nullable;

/**
 * Extends vanilla's {@code FoxRenderState} with the skin this specific render was
 * extracted with, baked in at {@link ClientFoxRenderer#extractRenderState} time.
 *
 * <p>{@code getTextureLocation(state)} isn't called until the deferred GUI render
 * flush, which happens after the screen method that queued the draw call has already
 * returned - so code that mutates {@code CONFIG.foxSkin}, queues a render, then
 * restores {@code CONFIG.foxSkin} (the adoption screen's variant gallery, showing
 * several different skins side by side in one frame) has always restored the real
 * value again before the texture is actually picked, so every fox in that frame drew
 * with whatever skin happened to be "real" at flush time instead of its own. Baking
 * the skin into the render state at extraction time (synchronous, so it correctly
 * captures whatever {@code CONFIG.foxSkin} was at that exact call) and reading it
 * back here instead of the live config fixes that.
 */
public class ClientFoxRenderState extends FoxRenderState {
    public @Nullable String skinOverride;
}
