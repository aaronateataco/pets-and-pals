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
    private static final double OUT_OF_SIGHT_TELEPORT_DISTANCE = 6.0;
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
    private int unseenTicks;
    private boolean wasAlongside;
    /** +1 = owner's right, -1 = left; picked from where the pet already is, with hysteresis. */
    private int alongsideSide;
    private double smoothedBoost;
    private int glanceTicks;
    private int glanceCooldown;

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
        this.unseenTicks = 0;
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
        } else {
            this.pet.getLookControl().setLookAt(this.owner, 10.0F, (float) this.pet.getMaxHeadXRot());
        }

        if (--this.timeToRecalcPath > 0) {
            return;
        }
        this.timeToRecalcPath = this.adjustedTickDelay(alongside ? 5 : 10);

        double distanceSqr = this.pet.distanceToSqr(this.owner);
        boolean seen = this.ownerCanSeePet();

        // Ghost form when the pet is effectively lost: sustained out-of-sight beyond the
        // stop ring, clearly out of sight and lagging, or simply way too far.
        if (!seen && distanceSqr > (double) ((this.stopDistance + 1.0f) * (this.stopDistance + 1.0f))) {
            this.unseenTicks += 10;
        } else {
            this.unseenTicks = 0;
        }
        if (distanceSqr > TELEPORT_DISTANCE * TELEPORT_DISTANCE
                || this.unseenTicks >= 50
                || (!seen && distanceSqr > OUT_OF_SIGHT_TELEPORT_DISTANCE * OUT_OF_SIGHT_TELEPORT_DISTANCE)) {
            this.pet.enterOrbMode();
            return;
        }

        double distance = Math.sqrt(distanceSqr);

        // Never lose the pet: even when it's visible, if it makes no progress toward the
        // owner for ~4s while far away (stuck on a cliff, across water, broken path...),
        // let it rift to the owner rather than leaving it behind.
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

        return this.owner.position()
                .add(forward.scale(forwardDistance))
                .add(right.scale(1.5 * this.alongsideSide));
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
