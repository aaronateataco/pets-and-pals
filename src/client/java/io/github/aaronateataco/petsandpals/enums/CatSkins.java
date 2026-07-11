package io.github.aaronateataco.petsandpals.enums;

import net.minecraft.network.chat.Component;

public enum CatSkins {
    black,
    british_shorthair,
    calico,
    jellie,
    ocelot,
    persian,
    ragdoll,
    red,
    siamese,
    tabby,
    tuxedo,
    white;

    public Component getDisplayName() {
        return Component.literal(String.valueOf(this).replace("_", " "));
    }
}
