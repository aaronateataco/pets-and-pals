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
    private static final double MAX_ALONGSIDE_BOOST = 2.5;
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
        if (this.pet.isOrderedToSit() || this.pet.isPassenger() || this.pet.isPerched()) {
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
        if (this.pet.isOrderedToSit() || this.pet.isPassenger() || this.pet.isPerched()) {
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
            this.teleportIntoOwnersView();
            return;
        }

        double distance = Math.sqrt(distanceSqr);

        // Never lose the pet: even when it's visible, if it makes no progress toward the
        // owner for ~5s while far away (stuck on a cliff, across water, broken path...),
        // reposition it rather than leaving it behind.
        if (distance > 10.0 && distance > this.lastDistance - 0.5) {
            this.noProgressTicks += 10;
            if (this.noProgressTicks >= 100) {
                this.teleportIntoOwnersView();
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

        if (alongside) {
            // Aim at a point beside-and-ahead of the owner, led by their motion so the
            // path stays valid between recalcs, and dynamically raise the pet's speed
            // attribute just enough to hold formation - farther behind = bigger boost,
            // in position = barely boosted, so the pace change reads as natural.
            Vec3 motion = this.owner.getDeltaMovement();
            Vec3 forward = new Vec3(motion.x, 0.0, motion.z);
            if (forward.lengthSqr() < 1.0e-4) {
                forward = Vec3.directionFromRotation(0.0F, this.owner.yBodyRot);
            } else {
                forward = forward.normalize();
            }
            Vec3 right = new Vec3(-forward.z, 0.0, forward.x);
            Vec3 lead = motion.scale(6.0);
            targetX += forward.x * 1.5 + right.x * 1.4 + lead.x;
            targetZ += forward.z * 1.5 + right.z * 1.4 + lead.z;

            double anchorDx = targetX - this.pet.getX();
            double anchorDz = targetZ - this.pet.getZ();
            double lag = Math.sqrt(anchorDx * anchorDx + anchorDz * anchorDz);
            double dynamicBoost = Mth.clamp(0.35 + lag * 0.28, 0.35, MAX_ALONGSIDE_BOOST - 1.0);
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

    /**
     * Teleports the pet to a safe spot just ahead of where the owner is looking, inside the
     * view cone - the warp itself is never on screen (only called while unseen or extremely
     * far), so the pet simply "is there" when the owner looks around.
     */
    private void teleportIntoOwnersView() {
        this.pet.getNavigation().stop();
        Level level = this.pet.level();

        for (int attempt = 0; attempt < 12; attempt++) {
            float yaw = this.owner.getYHeadRot() + (this.pet.getRandom().nextFloat() * 80.0F - 40.0F);
            Vec3 direction = Vec3.directionFromRotation(0.0F, yaw);
            double distance = this.stopDistance + 1.0 + this.pet.getRandom().nextDouble() * 2.0;
            double x = this.owner.getX() + direction.x * distance;
            double z = this.owner.getZ() + direction.z * distance;

            if (this.pet.followYOffset() > 0.0F) {
                // Flying pet: place it hovering at its follow height, it just needs free space.
                double y = this.owner.getY() + this.pet.followYOffset();
                if (this.noCollisionAt(level, x, y, z)) {
                    this.pet.snapTo(x, y, z, this.pet.getYRot(), this.pet.getXRot());
                    return;
                }
            } else {
                // Ground pet: scan for standable ground near the owner's feet level.
                for (int dy = 2; dy >= -3; dy--) {
                    BlockPos pos = BlockPos.containing(x, this.owner.getY() + dy, z);
                    BlockPos below = pos.below();
                    if (!level.getBlockState(below).isFaceSturdy(level, below, Direction.UP)) {
                        continue;
                    }
                    if (this.noCollisionAt(level, x, pos.getY(), z)) {
                        this.pet.snapTo(x, pos.getY(), z, this.pet.getYRot(), this.pet.getXRot());
                        return;
                    }
                }
            }
        }
        // No clean spot in view found (tight cave, wall of blocks...) - vanilla fallback.
        this.pet.tryToTeleportToOwner();
    }

    private boolean noCollisionAt(@NotNull Level level, double x, double y, double z) {
        AABB box = this.pet.getBoundingBox().move(
                x - this.pet.getX(),
                y - this.pet.getY(),
                z - this.pet.getZ()
        );
        return level.noCollision(this.pet, box);
    }
}
