package io.github.aaronateataco.petsandpals.enums;

import net.minecraft.network.chat.Component;

public enum SquidSkins {
    squid,
    glow_squid;

    public Component getDisplayName() {
        return Component.literal(String.valueOf(this).replace("_", " "));
    }
}
