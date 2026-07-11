package io.github.aaronateataco.petsandpals.enums;

import net.minecraft.network.chat.Component;

public enum AxolotlSkins {
    blue,
    brown,
    cyan,
    gold,
    pink;

    public Component getDisplayName() {
        return Component.literal(String.valueOf(this).replace("_", " "));
    }
}
