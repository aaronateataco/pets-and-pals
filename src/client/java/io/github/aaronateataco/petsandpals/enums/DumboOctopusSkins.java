package io.github.aaronateataco.petsandpals.enums;

import net.minecraft.network.chat.Component;

public enum DumboOctopusSkins {
    blue,
    green,
    orange,
    pink,
    red,
    yellow;

    public Component getDisplayName() {
        return Component.literal(String.valueOf(this));
    }
}
