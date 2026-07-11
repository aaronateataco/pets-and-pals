package io.github.aaronateataco.petsandpals.enums;

import net.minecraft.network.chat.Component;

public enum PandaSkins {
    agressive,
    brown,
    lazy,
    normal,
    playful,
    weak,
    worried;

    public Component getDisplayName() {
        return Component.literal(String.valueOf(this).replace("_", " "));
    }
}
