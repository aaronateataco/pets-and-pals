package io.github.aaronateataco.petsandpals.enums;

import net.minecraft.network.chat.Component;

public enum CopperGolemSkins {
    exposed,
    oxidized,
    unoxidized,
    weathered;

    public Component getDisplayName() {
        return Component.literal(String.valueOf(this).replace("_", " "));
    }
}
