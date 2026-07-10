package io.github.aaronateataco.petsandpals.mob;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

/**
 * Abstract class representing any pet that can fly (ghasts, vexes, allays, etc).
 * <p>
 * Uses the vanilla {@link FlyingMoveControl} (hover-in-place, like the allay) and
 * {@link FlyingPathNavigation}, so flight paths, hovering, and animations all behave
 * like real flying mobs instead of hand-rolled velocity math.
 *
 * @see AbstractPet
 * @see GroundPet
 */
public abstract class FlyingPet extends AbstractPet {

    protected FlyingPet(EntityType<? extends @NotNull TamableAnimal> type, Level level) {
        super(type, level);
        this.moveControl = new FlyingMoveControl(this, 20, true);
    }

    @Override
    protected boolean usesGoalMovement() {
        return true;
    }

    @Override
    protected @NotNull PathNavigation createNavigation(@NotNull Level level) {
        FlyingPathNavigation navigation = new FlyingPathNavigation(this, level);
        navigation.setCanOpenDoors(false);
        navigation.setCanFloat(true);
        return navigation;
    }
}
