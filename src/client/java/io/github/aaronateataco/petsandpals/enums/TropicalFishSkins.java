package io.github.aaronateataco.petsandpals.enums;

import dev.isxander.yacl3.api.NameableEnum;
import net.minecraft.network.chat.Component;

public enum TropicalFishSkins implements NameableEnum {
    cichlid,
    clownfish,
    cotton_candy_betta,
    goatfish,
    parrotfish,
    queen_angelfish,
    red_lipped_blenny,
    tomato_clownfish,
    triggerfish,
    yellowtail_parrotfish;

    @Override
    public Component getDisplayName() {
        return Component.literal(String.valueOf(this).replace("_", " "));
    }
}
