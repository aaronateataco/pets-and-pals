package io.github.aaronateataco.petsandpals.mob.ai;

import io.github.aaronateataco.petsandpals.mob.AbstractPet;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;

import java.util.EnumSet;

/**
 * Client-safe replacement for the vanilla {@code LookAtPlayerGoal}: identical behavior
 * (occasionally watch a nearby player for a few seconds), but without the
 * {@code Goal.getServerLevel} call that crashes with a ClassCastException when the goal
 * runs in a ClientLevel - which is the only place pets ever exist.
 */
public class PetLookAtPlayerGoal extends Goal {

    private final AbstractPet pet;
    private final float lookDistance;
    private final float probability;
    private Player lookAt;
    private int lookTime;

    public PetLookAtPlayerGoal(AbstractPet pet, float lookDistance) {
        this.pet = pet;
        this.lookDistance = lookDistance;
        this.probability = 0.02F;
        this.setFlags(EnumSet.of(Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.pet.getRandom().nextFloat() >= this.probability) {
            return false;
        }
        this.lookAt = this.pet.level().getNearestPlayer(this.pet, this.lookDistance);
        return this.lookAt != null;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.lookAt == null || !this.lookAt.isAlive()) {
            return false;
        }
        if (this.pet.distanceToSqr(this.lookAt) > (double) (this.lookDistance * this.lookDistance)) {
            return false;
        }
        return this.lookTime > 0;
    }

    @Override
    public void start() {
        this.lookTime = this.adjustedTickDelay(40 + this.pet.getRandom().nextInt(40));
    }

    @Override
    public void stop() {
        this.lookAt = null;
    }

    @Override
    public void tick() {
        if (this.lookAt == null || !this.lookAt.isAlive()) {
            return;
        }
        this.pet.getLookControl().setLookAt(this.lookAt.getX(), this.lookAt.getEyeY(), this.lookAt.getZ());
        this.lookTime--;
    }
}
