package io.github.aaronateataco.petsandpals.mob;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

/**
 * Abstract class representing any pet that cannot fly (wolves, chickens, etc).
 * <p>
 * Movement is entirely vanilla: the default {@code GroundPathNavigation} plus the goals
 * registered in {@link AbstractPet#registerGoals()} handle following the owner, jumping
 * up blocks, avoiding drops, swimming, and all walk/look animations.
 *
 * @see AbstractPet
 * @see FlyingPet
 */
public abstract class GroundPet extends AbstractPet {

    public GroundPet(EntityType<? extends @NotNull TamableAnimal> type, Level level) {
        super(type, level);
    }

    @Override
    protected boolean usesGoalMovement() {
        return true;
    }
}
