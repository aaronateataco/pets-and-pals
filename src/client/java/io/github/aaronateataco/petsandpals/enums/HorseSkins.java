package io.github.aaronateataco.petsandpals.enums;

import net.minecraft.network.chat.Component;

public enum HorseSkins {
    black,
    brown,
    chestnut,
    creamy,
    dark_brown,
    gray,
    skeleton,
    white,
    zombie;

    public Component getDisplayName() {
        return Component.literal(String.valueOf(this).replace("_", " "));
    }
}
