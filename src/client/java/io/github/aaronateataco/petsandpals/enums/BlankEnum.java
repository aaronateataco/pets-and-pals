package io.github.aaronateataco.petsandpals.enums;

import dev.isxander.yacl3.api.NameableEnum;
import net.minecraft.network.chat.Component;

public enum BlankEnum implements NameableEnum {
    no_skins_are_available;

    @Override
    public Component getDisplayName() {
        return Component.literal(String.valueOf(this).replace("_", " "));
    }
}
