package com.jeff.pets.mixin.client;

import net.minecraft.client.resources.SplashManager;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import static com.jeff.pets.Central.CONFIG;
import static com.jeff.pets.PetsInitializer.MOD_ID;

@Mixin(SplashManager.class)
public class SplashManagerMixin {

    @Redirect(method = "<clinit>", at = @At(value = "FIELD", opcode = Opcodes.PUTSTATIC, target = "Lnet/minecraft/client/resources/SplashManager;SPLASHES_LOCATION:Lnet/minecraft/resources/Identifier;"))
    private static void redirect(Identifier identifier) {
        SplashManager.SPLASHES_LOCATION = CONFIG.customTitleEnabled
                ? Identifier.fromNamespaceAndPath(MOD_ID, "texts/splashes.txt")
                : Identifier.withDefaultNamespace("texts/splashes.txt");
    }
}
