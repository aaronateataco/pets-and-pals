package io.github.aaronateataco.petsandpals;

import io.github.aaronateataco.petsandpals.enums.CatSkins;

/**
 * Snapshot build: YACL has no 26.3 release yet, so the advanced YACL screen is
 * disabled and the Menagerie is the only config surface. Names/skins return once
 * YACL updates (or once the in-catalog editors land).
 */
public class PetsConfigScreen {

    private static final PetsConfigScreen INSTANCE = new PetsConfigScreen();

    public Class<? extends Enum<?>> enumClass = CatSkins.class;

    public static PetsConfigScreen getInstance() {
        return INSTANCE;
    }

    public java.util.function.Function<net.minecraft.client.gui.screens.Screen, net.minecraft.client.gui.screens.Screen> getModConfigScreenFactory() {
        return io.github.aaronateataco.petsandpals.gui.MenagerieScreen::new;
    }

    public java.util.function.Function<net.minecraft.client.gui.screens.Screen, net.minecraft.client.gui.screens.Screen> getAdvancedConfigScreenFactory() {
        return io.github.aaronateataco.petsandpals.gui.MenagerieScreen::new;
    }
}
