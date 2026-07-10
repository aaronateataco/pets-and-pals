package io.github.aaronateataco.petsandpals.mob.ai;

import io.github.aaronateataco.petsandpals.mob.AbstractPet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;

/**
 * A vanilla-style follow goal modeled on {@link net.minecraft.world.entity.ai.goal.FollowOwnerGoal},
 * tuned so the owner never loses their pet:
 * <ul>
 *   <li><b>Visible pet</b>: catches up smoothly - speed scales with distance (capped at
 *   {@link #MAX_CATCH_UP_BOOST}) so it hurries without teleport-popping on screen.</li>
 *   <li><b>Unseen pet</b>: if it falls behind while outside the owner's view cone (or behind a
 *   wall), it silently teleports to a safe spot <i>inside</i> the owner's field of view - so
 *   whenever you turn around, your pet is already there.</li>
 *   <li>Hard teleport regardless of visibility past {@link #TELEPORT_DISTANCE} blocks.</li>
 * </ul>
 * Flying pets follow to {@code followYOffset()} above the owner's feet (bees hover near your
 * head instead of hugging the ground). The final movement speed is
 * {@code speedModifier * AbstractPet.speedMultiplier} (the "Pet Speed" config setting)
 * on top of the pet's own vanilla movement-speed attribute.
 */
public class PetFollowOwnerGoal extends Goal {

    private static final double TELEPORT_DISTANCE = 24.0;
    private static final double OUT_OF_SIGHT_TELEPORT_DISTANCE = 8.0;
    private static final double MAX_CATCH_UP_BOOST = 1.75;
    /** Transient speed modifier used while pacing a sprinting owner; value updated dynamically. */
    private static final Identifier ALONGSIDE_SPEED_ID = Identifier.fromNamespaceAndPath("pets-and-pals", "run_alongside_boost");
    private static final double MAX_ALONGSIDE_BOOST = 3.0;
    /** cos(75 degrees) - half-angle of what counts as "the owner can see the pet". */
    private static final double VIEW_CONE_COS = 0.2588;

    private final AbstractPet pet;
    private final double speedModifier;
    private final float startDistance;
    private final float stopDistance;
    private LivingEntity owner;
    private int timeToRecalcPath;
    private double lastDistance;
    private int noProgressTicks;
    private boolean wasAlongside;

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
        if (this.pet.isOrderedToSit() || this.pet.isPassenger() || this.pet.isPerched() || this.pet.isOrbMode()) {
            return false;
        }
        double distanceSqr = this.pet.distanceToSqr(livingEntity);
        boolean wantsAlongside = this.runningAlongside() && distanceSqr > 1.0;
        if (!wantsAlongside && distanceSqr < (double) (this.startDistance * this.startDistance)) {
            return false;
        }
        this.owner = livingEntity;
        return true;
    }

    /**
     * Fortnite-style: after the owner has been sprinting for ~1s (first person only, since
     * that's when a trailing pet is invisible), the pet runs at the owner's front-right -
     * on screen - with a dynamically boosted speed attribute so it genuinely keeps pace.
     */
    private boolean runningAlongside() {
        return this.pet.ownerSprintTicks() > 20 && AbstractPet.firstPersonView.getAsBoolean();
    }

    @Override
    public boolean canContinueToUse() {
        if (this.pet.isOrderedToSit() || this.pet.isPassenger() || this.pet.isPerched() || this.pet.isOrbMode()) {
            return false;
        }
        if (this.runningAlongside()) {
            return true;
        }
        if (this.pet.getNavigation().isDone()) {
            return false;
        }
        return this.pet.distanceToSqr(this.owner) > (double) (this.stopDistance * this.stopDistance);
    }

    @Override
    public void start() {
        this.timeToRecalcPath = 0;
        this.lastDistance = Double.MAX_VALUE;
        this.noProgressTicks = 0;
    }

    @Override
    public void stop() {
        this.owner = null;
        this.pet.getNavigation().stop();
        this.clearAlongsideBoost();
    }

    @Override
    public void tick() {
        this.pet.getLookControl().setLookAt(this.owner, 10.0F, (float) this.pet.getMaxHeadXRot());
        if (--this.timeToRecalcPath > 0) {
            return;
        }
        boolean alongside = this.runningAlongside();
        this.timeToRecalcPath = this.adjustedTickDelay(alongside ? 4 : 10);

        double distanceSqr = this.pet.distanceToSqr(this.owner);
        boolean seen = this.ownerCanSeePet();

        // Way too far, or lagging behind where the owner can't see it anyway:
        // reposition silently into the owner's view so the pet is never lost.
        if (distanceSqr > TELEPORT_DISTANCE * TELEPORT_DISTANCE
                || (!seen && distanceSqr > OUT_OF_SIGHT_TELEPORT_DISTANCE * OUT_OF_SIGHT_TELEPORT_DISTANCE)) {
            this.pet.enterOrbMode();
            return;
        }

        double distance = Math.sqrt(distanceSqr);

        // Never lose the pet: even when it's visible, if it makes no progress toward the
        // owner for ~5s while far away (stuck on a cliff, across water, broken path...),
        // reposition it rather than leaving it behind.
        if (distance > 6.0 && distance > this.lastDistance - 0.5) {
            this.noProgressTicks += 10;
            if (this.noProgressTicks >= 80) {
                this.pet.enterOrbMode();
                this.noProgressTicks = 0;
                this.lastDistance = Double.MAX_VALUE;
                return;
            }
        } else {
            this.noProgressTicks = 0;
        }
        this.lastDistance = distance;

        double boost = Mth.clamp(1.0 + (distance - this.stopDistance) * 0.09, 1.0, MAX_CATCH_UP_BOOST);
        double targetX = this.owner.getX();
        double targetZ = this.owner.getZ();

        if (alongside != this.wasAlongside) {
            this.wasAlongside = alongside;
            io.github.aaronateataco.petsandpals.PetsInitializer.LOGGER.info(
                    "[Pets&Pals] run-alongside {} (sprintTicks={}, firstPerson={})",
                    alongside ? "ENGAGED" : "ended",
                    this.pet.ownerSprintTicks(), AbstractPet.firstPersonView.getAsBoolean());
        }
        if (alongside) {
            // Anchor to the CAMERA yaw (not the motion vector) so the formation point sits
            // at the front-right of what the player actually sees - turning the view keeps
            // the pet on screen instead of leaving it wherever the velocity points. Still
            // led by motion so the path stays valid between recalcs.
            Vec3 motion = this.owner.getDeltaMovement();
            Vec3 forward = Vec3.directionFromRotation(0.0F, this.owner.getYHeadRot());
            forward = new Vec3(forward.x, 0.0, forward.z).normalize();
            Vec3 right = new Vec3(-forward.z, 0.0, forward.x);
            Vec3 lead = new Vec3(motion.x, 0.0, motion.z).scale(4.0);
            targetX += forward.x * 1.5 + right.x * 1.4 + lead.x;
            targetZ += forward.z * 1.5 + right.z * 1.4 + lead.z;

            double anchorDx = targetX - this.pet.getX();
            double anchorDz = targetZ - this.pet.getZ();
            double lag = Math.sqrt(anchorDx * anchorDx + anchorDz * anchorDz);
            double dynamicBoost = Mth.clamp(0.35 + lag * 0.32, 0.35, MAX_ALONGSIDE_BOOST - 1.0);
            AttributeModifier modifier = new AttributeModifier(
                    ALONGSIDE_SPEED_ID, dynamicBoost, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
            this.applyAlongsideBoost(Attributes.MOVEMENT_SPEED, modifier);
            this.applyAlongsideBoost(Attributes.FLYING_SPEED, modifier);
        } else {
            this.clearAlongsideBoost();
        }

        double speed = this.speedModifier * AbstractPet.speedMultiplier.getAsDouble() * boost;
        this.pet.getNavigation().moveTo(
                targetX,
                this.owner.getY() + this.pet.followYOffset(),
                targetZ,
                speed
        );
    }

    private void applyAlongsideBoost(Holder<Attribute> attribute, AttributeModifier modifier) {
        AttributeInstance instance = this.pet.getAttribute(attribute);
        if (instance != null) {
            instance.addOrUpdateTransientModifier(modifier);
        }
    }

    private void clearAlongsideBoost() {
        for (Holder<Attribute> attribute : java.util.List.of(Attributes.MOVEMENT_SPEED, Attributes.FLYING_SPEED)) {
            AttributeInstance instance = this.pet.getAttribute(attribute);
            if (instance != null && instance.hasModifier(ALONGSIDE_SPEED_ID)) {
                instance.removeModifier(ALONGSIDE_SPEED_ID);
            }
        }
    }

    /**
     * Whether the pet is roughly within the owner's field of view (a generous horizontal
     * cone around the owner's head yaw) with a clear line of sight.
     */
    private boolean ownerCanSeePet() {
        Vec3 view = Vec3.directionFromRotation(0.0F, this.owner.getYHeadRot());
        Vec3 toPet = this.pet.position().subtract(this.owner.getEyePosition());
        Vec3 flat = new Vec3(toPet.x, 0.0, toPet.z);
        if (flat.lengthSqr() < 1.0e-4) {
            return true;
        }
        if (new Vec3(view.x, 0.0, view.z).normalize().dot(flat.normalize()) < VIEW_CONE_COS) {
            return false;
        }
        return this.owner.hasLineOfSight(this.pet);
    }

}
