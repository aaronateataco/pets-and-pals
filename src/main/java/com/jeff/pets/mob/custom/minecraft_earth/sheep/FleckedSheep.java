package com.jeff.pets.mob.custom.minecraft_earth.sheep;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class FleckedSheep extends MinecraftEarthSheep{
    public FleckedSheep(EntityType<? extends @NotNull TamableAnimal> type, Level level) {
        super(type, level);
    }
}
