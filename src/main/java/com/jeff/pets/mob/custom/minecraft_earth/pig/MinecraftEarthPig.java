package com.jeff.pets.mob.custom.minecraft_earth.pig;

import com.jeff.pets.mob.GroundPet;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.pig.PigSoundVariants;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class MinecraftEarthPig extends GroundPet {
    public MinecraftEarthPig(EntityType<? extends @NotNull TamableAnimal> type, Level level) {
        super(type, level);
    }

    @Override
    protected int stopDistance() {
        return 2;
    }

    @Override
    protected float heartHeight() {
        return 1.5f;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.PIG_SOUNDS.get(PigSoundVariants.SoundSet.CLASSIC).adultSounds().ambientSound().value();
    }
}
