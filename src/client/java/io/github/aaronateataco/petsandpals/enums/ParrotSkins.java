package io.github.aaronateataco.petsandpals.enums;

import net.minecraft.network.chat.Component;

public enum ParrotSkins {
    blue,
    cyan,
    gray,
    green,
    red;

    public Component getDisplayName() {
        return Component.literal(String.valueOf(this).replace("_", " "));
    }
}
