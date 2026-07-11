package io.github.aaronateataco.petsandpals.enums;

import net.minecraft.network.chat.Component;

public enum RabbitSkins {
    black,
    brown,
    gold,
    killer,
    salt,
    splotched,
    toast,
    white;

    public Component getDisplayName() {
        return Component.literal(String.valueOf(this).replace("_", " "));
    }
}
