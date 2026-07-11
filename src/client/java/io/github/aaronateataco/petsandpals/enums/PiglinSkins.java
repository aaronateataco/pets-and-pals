package io.github.aaronateataco.petsandpals.enums;

import net.minecraft.network.chat.Component;

public enum PiglinSkins {
    piglin,
    piglin_brute,
    zombified_piglin;


    public Component getDisplayName() {
        return Component.literal(String.valueOf(this).replace("_", " "));
    }
}
