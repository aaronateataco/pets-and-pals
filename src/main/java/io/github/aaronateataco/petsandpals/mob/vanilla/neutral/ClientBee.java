package io.github.aaronateataco.petsandpals.mob.vanilla.neutral;

import io.github.aaronateataco.petsandpals.CanFly;
import io.github.aaronateataco.petsandpals.mob.FlyingPet;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

@CanFly
public class ClientBee extends FlyingPet {

    /** Bees emerge from a bee nest in the spawn animation. */
    @Override
    public net.minecraft.world.level.block.state.BlockState spawnDwellingBlock() {
        return net.minecraft.world.level.block.Blocks.BEE_NEST.defaultBlockState();
    }

    public ClientBee(EntityType<? extends @NotNull TamableAnimal> entityType, Level level) {
        super(entityType, level);
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
        return SoundEvents.BEE_LOOP;
    }
}
