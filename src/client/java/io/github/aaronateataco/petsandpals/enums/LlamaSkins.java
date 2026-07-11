package io.github.aaronateataco.petsandpals.enums;

import net.minecraft.network.chat.Component;

public enum LlamaSkins {
    brown,
    creamy,
    gray,
    white;

    public Component getDisplayName() {
        return Component.literal(String.valueOf(this).replace("_", " "));
    }
}
