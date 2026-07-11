package io.github.aaronateataco.petsandpals.enums;

import net.minecraft.network.chat.Component;

public enum WolfSkins {
    ashen,
    black,
    chestnut,
    pale,
    rusty,
    snowy,
    spotted,
    striped,
    woods;

    public Component getDisplayName() {
        return Component.literal(String.valueOf(this).replace("_", " "));
    }
}
