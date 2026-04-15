package com.jeff.pets.mob.custom.minecraft_earth.sheep;

import com.jeff.pets.mob.GroundPet;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class RainbowSheep extends MinecraftEarthSheep {
    public RainbowSheep(EntityType<? extends @NotNull TamableAnimal> type, Level level) {
        super(type, level);
    }
}
