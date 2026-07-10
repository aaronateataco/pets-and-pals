package io.github.aaronateataco.petsandpals.mob.ai;

import io.github.aaronateataco.petsandpals.mob.AbstractPet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;

/**
 * A vanilla-style follow goal modeled on {@link net.minecraft.world.entity.ai.goal.FollowOwnerGoal},
 * with two pet-specific differences:
 * <ul>
 *   <li>Bounded catch-up: past {@link #CATCH_UP_DISTANCE} blocks the pet speeds up (capped)
 *   instead of instantly teleporting.</li>
 *   <li>Hard teleport only happens past {@link #TELEPORT_DISTANCE} blocks, so the pet never
 *   pops mid-frame while it can still realistically walk to you.</li>
 * </ul>
 * The final movement speed is {@code speedModifier * AbstractPet.speedMultiplier} (the
 * user-adjustable "Pet Speed" config setting) applied on top of the pet's own vanilla
 * movement-speed attribute.
 */
public class PetFollowOwnerGoal extends Goal {

    private static final double CATCH_UP_DISTANCE = 12.0;
    private static final double CATCH_UP_BOOST = 1.5;
    private static final double TELEPORT_DISTANCE = 24.0;

    private final AbstractPet pet;
    private final double speedModifier;
    private final float startDistance;
    private final float stopDistance;
    private LivingEntity owner;
    private int timeToRecalcPath;

    public PetFollowOwnerGoal(AbstractPet pet, double speedModifier, float startDistance, float stopDistance) {
        this.pet = pet;
        this.speedModifier = speedModifier;
        this.startDistance = startDistance;
        this.stopDistance = stopDistance;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity livingEntity = this.pet.getOwner();
        if (livingEntity == null || livingEntity.isSpectator()) {
            return false;
        }
        if (this.pet.isOrderedToSit() || this.pet.isPassenger()) {
            return false;
        }
        if (this.pet.distanceToSqr(livingEntity) < (double) (this.startDistance * this.startDistance)) {
            return false;
        }
        this.owner = livingEntity;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.pet.getNavigation().isDone()) {
            return false;
        }
        if (this.pet.isOrderedToSit() || this.pet.isPassenger()) {
            return false;
        }
        return this.pet.distanceToSqr(this.owner) > (double) (this.stopDistance * this.stopDistance);
    }

    @Override
    public void start() {
        this.timeToRecalcPath = 0;
    }

    @Override
    public void stop() {
        this.owner = null;
        this.pet.getNavigation().stop();
    }

    @Override
    public void tick() {
        this.pet.getLookControl().setLookAt(this.owner, 10.0F, (float) this.pet.getMaxHeadXRot());
        if (--this.timeToRecalcPath > 0) {
            return;
        }
        this.timeToRecalcPath = this.adjustedTickDelay(10);

        double distanceSqr = this.pet.distanceToSqr(this.owner);
        if (distanceSqr > TELEPORT_DISTANCE * TELEPORT_DISTANCE) {
            this.pet.tryToTeleportToOwner();
            return;
        }

        double speed = this.speedModifier * AbstractPet.speedMultiplier.getAsDouble();
        if (distanceSqr > CATCH_UP_DISTANCE * CATCH_UP_DISTANCE) {
            speed *= CATCH_UP_BOOST;
        }
        this.pet.getNavigation().moveTo(this.owner, speed);
    }
}
