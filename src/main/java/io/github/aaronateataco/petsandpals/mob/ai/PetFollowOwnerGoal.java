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

    private static final double MAX_CATCH_UP_BOOST = 1.75;
    /** Transient speed modifier used while pacing a sprinting owner; value updated dynamically. */
    private static final Identifier ALONGSIDE_SPEED_ID = Identifier.fromNamespaceAndPath("pets-and-pals", "run_alongside_boost");
    private static final double MAX_ALONGSIDE_BOOST = 3.0;

    private final AbstractPet pet;
    private final double speedModifier;
    private final float startDistance;
    private final float stopDistance;
    private LivingEntity owner;
    private int timeToRecalcPath;
    private boolean wasAlongside;
    /** +1 = owner's right, -1 = left; picked from where the pet already is, with hysteresis. */
    private int alongsideSide;
    private double smoothedBoost;
    private int glanceTicks;
    private int glanceCooldown;
    private int alongsideLagTicks;

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
        // Deliberately NOT checking navigation.isDone(): a finished path segment used to
        // stop the goal, whose restart wiped every tracking counter and cancelled paths -
        // the cause of pets stuttering at gaps/stairs and stuck-detection never firing.
        return this.pet.distanceToSqr(this.owner) > (double) (this.stopDistance * this.stopDistance);
    }

    @Override
    public void start() {
        this.timeToRecalcPath = 0;
        this.alongsideSide = 0;
        this.smoothedBoost = 0.35;
        this.glanceTicks = 0;
        this.glanceCooldown = 40;
    }

    @Override
    public void stop() {
        this.owner = null;
        this.pet.getNavigation().stop();
        this.clearAlongsideBoost();
    }

    @Override
    public void tick() {
        boolean alongside = this.runningAlongside();
        if (alongside != this.wasAlongside) {
            this.wasAlongside = alongside;
            io.github.aaronateataco.petsandpals.PetsInitializer.LOGGER.info(
                    "[Pets&Pals] run-alongside {} (sprintTicks={}, firstPerson={})",
                    alongside ? "ENGAGED" : "ended",
                    this.pet.ownerSprintTicks(), AbstractPet.firstPersonView.getAsBoolean());
            if (!alongside) {
                this.clearAlongsideBoost();
                this.alongsideSide = 0;
            }
        }

        if (alongside) {
            // Mostly watch where it's going; occasionally glance at the owner for a moment
            // (the sidekick "checking in on you" look), instead of running with its head
            // craned sideways the whole time.
            if (this.glanceTicks > 0) {
                this.glanceTicks--;
                this.pet.getLookControl().setLookAt(this.owner, 10.0F, (float) this.pet.getMaxHeadXRot());
            } else {
                Vec3 ahead = this.pet.position().add(this.pet.getDeltaMovement().scale(4.0)).add(0.0, this.pet.getEyeHeight(), 0.0);
                this.pet.getLookControl().setLookAt(ahead.x, ahead.y, ahead.z);
                if (--this.glanceCooldown <= 0) {
                    this.glanceTicks = 20 + this.pet.getRandom().nextInt(15);
                    this.glanceCooldown = 70 + this.pet.getRandom().nextInt(70);
                }
            }
            // Smooth the dynamic speed every tick (updating it only on path recalcs made
            // the pace visibly step, which read as jank).
            this.updateAlongsideBoost();

            // Leap entrance: if the pet is lagging its formation point while unwatched
            // (behind the player), quietly move it to just BEHIND the camera and give it a
            // full speed burst - so it visibly leaps past the player's shoulder into
            // formation, running the whole way. Also the obstacle recovery: if its side is
            // blocked, it swaps to the other side during the same maneuver.
            Vec3 anchor = this.alongsideAnchor();
            double lagSqr = this.pet.distanceToSqr(anchor.x, anchor.y + this.pet.followYOffset(), anchor.z);
            if (lagSqr > 12.25 && !this.pet.ownerInViewCone()) {
                if (++this.alongsideLagTicks >= 15) {
                    this.alongsideLagTicks = 0;
                    this.leapEntrance();
                    return;
                }
            } else {
                this.alongsideLagTicks = 0;
            }
        } else {
            this.pet.getLookControl().setLookAt(this.owner, 10.0F, (float) this.pet.getMaxHeadXRot());
        }

        if (--this.timeToRecalcPath > 0) {
            return;
        }
        this.timeToRecalcPath = this.adjustedTickDelay(alongside ? 5 : 10);

        double distanceSqr = this.pet.distanceToSqr(this.owner);
        double distance = Math.sqrt(distanceSqr);
        // (Lost-pet handling - out of view, stuck, too far - lives in AbstractPet.tick,
        // where it can't be reset by this goal stopping and restarting.)

        double boost = Mth.clamp(1.0 + (distance - this.stopDistance) * 0.09, 1.0, MAX_CATCH_UP_BOOST);
        double targetX = this.owner.getX();
        double targetZ = this.owner.getZ();
        if (alongside) {
            Vec3 anchor = this.alongsideAnchor();
            Vec3 lead = new Vec3(this.owner.getDeltaMovement().x, 0.0, this.owner.getDeltaMovement().z).scale(5.0);
            targetX = anchor.x + lead.x;
            targetZ = anchor.z + lead.z;
        }
        double speed = this.speedModifier * AbstractPet.speedMultiplier.getAsDouble() * boost;
        this.pet.getNavigation().moveTo(
                targetX,
                this.owner.getY() + this.pet.followYOffset(),
                targetZ,
                speed
        );
    }

    /**
     * Places the pet just behind the camera (out of view) with a max speed burst so it
     * sprints into frame past the player's shoulder. If the formation side is blocked
     * by an obstacle, flips to the other side first.
     */
    private void leapEntrance() {
        // Prefer the current side; if its anchor is blocked, swap sides.
        Vec3 anchor = this.alongsideAnchor();
        if (!this.pet.canFitAt(anchor.x, anchor.y + this.pet.followYOffset(), anchor.z)) {
            this.alongsideSide = -this.alongsideSide;
            anchor = this.alongsideAnchor();
        }

        Vec3 forward = Vec3.directionFromRotation(0.0F, this.owner.getYHeadRot());
        forward = new Vec3(forward.x, 0.0, forward.z).normalize();
        Vec3 right = new Vec3(-forward.z, 0.0, forward.x);
        Vec3 behind = this.owner.position()
                .subtract(forward.scale(2.2))
                .add(right.scale(0.9 * this.alongsideSide));

        boolean placed = false;
        if (this.pet.followYOffset() > 0.0F) {
            placed = this.pet.tryRepositionTo(behind.x, this.owner.getY() + this.pet.followYOffset(), behind.z);
        } else {
            for (int dy = 2; dy >= -3 && !placed; dy--) {
                BlockPos pos = BlockPos.containing(behind.x, this.owner.getY() + dy, behind.z);
                BlockPos below = pos.below();
                if (!this.pet.level().getBlockState(below).isFaceSturdy(this.pet.level(), below, Direction.UP)) {
                    continue;
                }
                placed = this.pet.tryRepositionTo(behind.x, pos.getY(), behind.z);
            }
        }
        if (!placed) {
            // No room behind the camera (wall right behind you) - appear past the
            // obstruction near the formation point instead.
            if (!this.pet.tryRepositionTo(anchor.x, anchor.y + this.pet.followYOffset(), anchor.z)) {
                this.pet.repositionToOwner();
                return;
            }
        }
        // Full burst so the entrance is a leap, easing back down as it reaches formation.
        this.smoothedBoost = MAX_ALONGSIDE_BOOST - 1.0;
        this.updateAlongsideBoost();
        Vec3 lead = new Vec3(this.owner.getDeltaMovement().x, 0.0, this.owner.getDeltaMovement().z).scale(5.0);
        this.pet.getNavigation().moveTo(
                anchor.x + lead.x,
                this.owner.getY() + this.pet.followYOffset(),
                anchor.z + lead.z,
                this.speedModifier * AbstractPet.speedMultiplier.getAsDouble());
        this.timeToRecalcPath = this.adjustedTickDelay(5);
    }

    /**
     * The formation point: well ahead of the camera (so the pet sits comfortably on
     * screen, not at the bottom edge) and offset to whichever side the pet is already
     * on - it may switch sides naturally if it drifts across, with hysteresis so it
     * doesn't flicker between them.
     */
    private Vec3 alongsideAnchor() {
        Vec3 forward = Vec3.directionFromRotation(0.0F, this.owner.getYHeadRot());
        forward = new Vec3(forward.x, 0.0, forward.z).normalize();
        Vec3 right = new Vec3(-forward.z, 0.0, forward.x);

        Vec3 toPet = this.pet.position().subtract(this.owner.position());
        double lateral = toPet.x * right.x + toPet.z * right.z;
        if (this.alongsideSide == 0) {
            this.alongsideSide = lateral >= 0.0 ? 1 : -1;
        } else if (lateral * this.alongsideSide < -1.0) {
            this.alongsideSide = -this.alongsideSide;
        }

        // Scale how far ahead the pet runs by the camera pitch: looking level/up pushes it
        // farther out so it sits comfortably in frame; looking down brings it in closer so
        // it doesn't drift out of the top of the view.
        float pitchDown = Math.max(0.0F, this.owner.getXRot());
        double forwardDistance = Mth.clamp(3.4 - pitchDown * 0.045, 2.0, 3.8);

        double fitY = this.owner.getY() + Math.max(0.1, this.pet.followYOffset());
        Vec3 base = this.owner.position().add(forward.scale(forwardDistance));
        Vec3 sideAnchor = base.add(right.scale(1.5 * this.alongsideSide));
        if (this.pet.canFitAt(sideAnchor.x, fitY, sideAnchor.z)) {
            return sideAnchor;
        }
        // Side blocked (wall, tree...): try the other side.
        Vec3 otherAnchor = base.add(right.scale(-1.5 * this.alongsideSide));
        if (this.pet.canFitAt(otherAnchor.x, fitY, otherAnchor.z)) {
            this.alongsideSide = -this.alongsideSide;
            return otherAnchor;
        }
        // Both sides blocked (narrow tunnel, hallway): run single file, directly ahead.
        return this.owner.position().add(forward.scale(Math.max(2.0, forwardDistance * 0.7)));
    }

    /** Smoothly sized speed boost, updated every tick: big when lagging, gentle in formation. */
    private void updateAlongsideBoost() {
        Vec3 anchor = this.alongsideAnchor();
        double dx = anchor.x - this.pet.getX();
        double dz = anchor.z - this.pet.getZ();
        double lag = Math.sqrt(dx * dx + dz * dz);
        double target = Mth.clamp(0.35 + lag * 0.32, 0.35, MAX_ALONGSIDE_BOOST - 1.0);
        this.smoothedBoost = Mth.lerp(0.2, this.smoothedBoost, target);
        AttributeModifier modifier = new AttributeModifier(
                ALONGSIDE_SPEED_ID, this.smoothedBoost, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        this.applyAlongsideBoost(Attributes.MOVEMENT_SPEED, modifier);
        this.applyAlongsideBoost(Attributes.FLYING_SPEED, modifier);
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


}
