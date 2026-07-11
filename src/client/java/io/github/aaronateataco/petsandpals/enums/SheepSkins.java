package io.github.aaronateataco.petsandpals.enums;

import net.minecraft.network.chat.Component;

public enum SheepSkins {
    black,
    blue,
    brown,
    cyan,
    gray,
    green,
    light_blue,
    light_gray,
    lime,
    magenta,
    orange,
    pink,
    purple,
    red,
    white,
    yellow;


    public Component getDisplayName() {
        return Component.literal(String.valueOf(this).replace("_", " "));
    }
}
