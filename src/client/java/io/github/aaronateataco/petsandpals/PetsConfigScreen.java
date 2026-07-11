package io.github.aaronateataco.petsandpals;

import io.github.aaronateataco.petsandpals.enums.DuckSkins;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

/**
 * Snapshot build: YACL has no 26.3 release yet, so the advanced YACL screen is
 * disabled and the Menagerie is the only config surface. Names/skins return once
 * YACL updates (or once the in-catalog editors land).
 */
public class PetsConfigScreen implements ModMenuApi {

    private static final PetsConfigScreen INSTANCE = new PetsConfigScreen();

    public Class<? extends Enum<?>> enumClass = DuckSkins.class;

    public static PetsConfigScreen getInstance() {
        return INSTANCE;
    }

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return io.github.aaronateataco.petsandpals.gui.MenagerieScreen::new;
    }

    public ConfigScreenFactory<?> getAdvancedConfigScreenFactory() {
        return io.github.aaronateataco.petsandpals.gui.MenagerieScreen::new;
    }
}
