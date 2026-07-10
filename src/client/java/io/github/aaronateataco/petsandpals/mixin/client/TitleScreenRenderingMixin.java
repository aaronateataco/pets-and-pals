package io.github.aaronateataco.petsandpals.mixin.client;

import io.github.aaronateataco.petsandpals.Central;
import io.github.aaronateataco.petsandpals.PetsConfig;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static io.github.aaronateataco.petsandpals.Central.CONFIG;

/**
 * Re-assign the title screen and edition locations to custom ones, if {@link PetsConfig#customTitleEnabled} is {@code true}.
 */
@Mixin(TitleScreen.class)
public class TitleScreenRenderingMixin {
    @Inject(at = @At("HEAD"), method = "extractRenderState")
    private void init(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a, CallbackInfo ci) {
        Central.reassignLogo(CONFIG.customTitleEnabled);
    }
}
