package io.github.aaronateataco.petsandpals.enums;

import net.minecraft.network.chat.Component;

public enum SnowGolemSkins {
    pumpkin_on,
    pumpkin_off;

    public Component getDisplayName() {
        return Component.literal(String.valueOf(this).replace("_", " "));
    }
}
