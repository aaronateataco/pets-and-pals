package io.github.aaronateataco.petsandpals.enums;

import net.minecraft.network.chat.Component;

public enum NautilusSkins {
    nautilus,
    coral_zombie,
    zombie;

    public Component getDisplayName() {
        return Component.literal(String.valueOf(this).replace("_", " "));
    }
}
