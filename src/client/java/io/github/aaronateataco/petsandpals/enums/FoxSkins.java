package io.github.aaronateataco.petsandpals.enums;

import net.minecraft.network.chat.Component;

public enum FoxSkins {
    red,
    snow;

    public Component getDisplayName() {
        return Component.literal(String.valueOf(this).replace("_", " "));
    }
}
