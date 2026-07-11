package io.github.aaronateataco.petsandpals.enums;

import net.minecraft.network.chat.Component;

public enum TropicalFishSkins {
    chichlid,
    clownfish,
    cotten_candy_betta,
    goatfish,
    parrotfish,
    queen_angelfish,
    red_lipped_blenny,
    tomato_clownfish,
    triggerfish,
    yellowtail_parrotfish;

    public Component getDisplayName() {
        return Component.literal(String.valueOf(this).replace("_", " "));
    }
}
