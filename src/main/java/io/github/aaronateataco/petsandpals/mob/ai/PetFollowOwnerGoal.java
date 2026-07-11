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
 * Follow goal for pets. Handles normal following with distance-scaled catch-up speed,
 * plus the sprint run-alongside mode where the pet paces the owner on screen.
 * Lost/stuck handling is in {@link AbstractPet#tick()} so it survives goal restarts.
 */
public class PetFollowOwnerGoal extends Goal {

    private static final double MAX_CATCH_UP_BOOST = 1.75;
    // transient speed modifier used while pacing a sprinting owner
    private static final Identifier ALONGSIDE_SPEED_ID = Identifier.fromNamespaceAndPath("pets-and-pals", "run_alongside_boost");
    private static final double MAX_ALONGSIDE_BOOST = 3.0;

    private final AbstractPet pet;
    private final double speedModifier;
    private final float startDistance;
    private final float stopDistance;
    private LivingEntity owner;
    private int timeToRecalcPath;
    private boolean wasAlongside;
    // +1 right, -1 left
    private int alongsideSide;
    private double smoothedBoost;
    private int glanceTicks;
    private int glanceCooldown;
    private int alongsideLagTicks;
    private int lagBursts;
    // owner yaw from ~0.4s ago, so direction changes register with a small delay
    private final float[] yawHistory = new float[8];
    private int yawIndex = -1;

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

    // engages after ~1s of sprinting, first person only
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
        // don't check navigation.isDone() here - stopping on finished path segments
        // caused constant goal restarts and stuttering at gaps/stairs
        return this.pet.distanceToSqr(this.owner) > (double) (this.stopDistance * this.stopDistance);
    }

    @Override
    public void start() {
        this.timeToRecalcPath = 0;
        this.alongsideSide = 0;
        this.smoothedBoost = 0.35;
        this.glanceTicks = 0;
        this.glanceCooldown = 40;
        this.lagBursts = 0;
    }

    @Override
    public void stop() {
        this.owner = null;
        this.pet.getNavigation().stop();
        this.clearAlongsideBoost();
    }

    @Override
    public void tick() {
        if (this.yawIndex < 0) {
            java.util.Arrays.fill(this.yawHistory, this.owner.getYHeadRot());
            this.yawIndex = 0;
        }
        this.yawHistory[this.yawIndex] = this.owner.getYHeadRot();
        this.yawIndex = (this.yawIndex + 1) % this.yawHistory.length;

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
            // look ahead while running, glance at the owner every few seconds
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
            // smooth the speed boost every tick or the pace visibly steps
            this.updateAlongsideBoost();

            // direct steering: while pacing the owner, skip pathfinding entirely and
            // drive straight at the (moving) formation point - paths to a target moving
            // at sprint speed rarely completed, which made the whole feature flaky
            Vec3 steer = this.alongsideAnchor();
            Vec3 steerLead = new Vec3(this.owner.getDeltaMovement().x, 0.0, this.owner.getDeltaMovement().z).scale(5.0);
            double steerY = this.owner.getY() + this.pet.followYOffset();
            this.pet.getMoveControl().setWantedPosition(
                    steer.x + steerLead.x, steerY, steer.z + steerLead.z,
                    this.speedModifier * AbstractPet.speedMultiplier.getAsDouble());
            if (this.pet.followYOffset() <= 0.0F) {
                if (this.pet.isInWater()) {
                    // don't wallow: swim hard at the surface and keep pace
                    this.pet.setDeltaMovement(this.pet.getDeltaMovement().add(0.0, 0.05, 0.0));
                    this.pet.getJumpControl().jump();
                } else if (this.pet.onGround()) {
                    double sdx = steer.x - this.pet.getX();
                    double sdz = steer.z - this.pet.getZ();
                    double len = Math.sqrt(sdx * sdx + sdz * sdz);
                    if (len > 0.01) {
                        double nx = sdx / len, nz = sdz / len;
                        BlockPos ahead = BlockPos.containing(
                                this.pet.getX() + nx * 0.9, this.pet.getY() + 0.1, this.pet.getZ() + nz * 0.9);
                        // step-up: hop blocks before bumping them (carpets and other
                        // sub-step shapes don't count, cats were hopping onto rugs)
                        boolean blocked = this.blocksPath(ahead);
                        boolean headroom = this.pet.level().getBlockState(ahead.above())
                                .getCollisionShape(this.pet.level(), ahead.above()).isEmpty();
                        BlockPos frontFloor = ahead.below();
                        boolean gap = this.pet.level().getBlockState(ahead)
                                .getCollisionShape(this.pet.level(), ahead).isEmpty()
                                && this.pet.level().getBlockState(frontFloor)
                                .getCollisionShape(this.pet.level(), frontFloor).isEmpty();
                        if ((blocked && headroom) || this.pet.horizontalCollision) {
                            this.pet.getJumpControl().jump();
                        } else if (gap) {
                            // shallow drop (stairs, slopes, <=3 blocks): just keep running.
                            // deeper: leap it if there's a same-level landing, else it's a
                            // cliff - stop at the edge instead of yeeting off
                            int depth = this.dropDepth(this.pet.getX() + nx, this.pet.getZ() + nz);
                            if (depth > 4) {
                                boolean landing = false;
                                for (int d = 2; d <= 3 && !landing; d++) {
                                    landing = this.dropDepth(this.pet.getX() + nx * d, this.pet.getZ() + nz * d) <= 1;
                                }
                                if (landing) {
                                    this.pet.getJumpControl().jump();
                                } else {
                                    this.pet.getMoveControl().setWantedPosition(
                                            this.pet.getX(), this.pet.getY(), this.pet.getZ(), 0.0);
                                }
                            }
                        }
                    }
                }
            }

            Vec3 anchor = this.alongsideAnchor();
            double lagSqr = this.pet.distanceToSqr(anchor.x, anchor.y + this.pet.followYOffset(), anchor.z);
            if (lagSqr > 12.25) {
                if (++this.alongsideLagTicks >= 15) {
                    this.alongsideLagTicks = 0;
                    if (this.pet.ownerInViewCone()) {
                        // being watched: burst first - but if that keeps failing mid-sprint,
                        // re-enter from behind the camera (off screen even while watched)
                        // instead of lagging forever until the sprint restarts
                        if (++this.lagBursts >= 2) {
                            this.lagBursts = 0;
                            this.leapEntrance();
                            io.github.aaronateataco.petsandpals.PetsInitializer.LOGGER.info(
                                    "[Pets&Pals] alongside re-entry from behind camera (watched)");
                            return;
                        }
                        this.smoothedBoost = MAX_ALONGSIDE_BOOST - 1.0;
                        this.updateAlongsideBoost();
                    } else {
                        // guaranteed arrival: place it straight into formation, running -
                        // pathing its way in at sprint speed proved too unreliable to be seen
                        double anchorY = this.owner.getY() + this.pet.followYOffset();
                        boolean placed = this.pet.tryRepositionTo(anchor.x, anchorY, anchor.z)
                                || this.placeOnGroundNear(anchor);
                        if (!placed) {
                            this.leapEntrance();
                            return;
                        }
                        this.smoothedBoost = MAX_ALONGSIDE_BOOST - 1.0;
                        this.updateAlongsideBoost();
                        io.github.aaronateataco.petsandpals.PetsInitializer.LOGGER.info(
                                "[Pets&Pals] alongside entrance placed at formation");
                    }
                }
            } else {
                this.alongsideLagTicks = 0;
                this.lagBursts = 0;
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
        if (alongside) {
            return; // direct steering above already handled movement
        }
        double boost = Mth.clamp(1.0 + (distance - this.stopDistance) * 0.09, 1.0, MAX_CATCH_UP_BOOST);
        double speed = this.speedModifier * AbstractPet.speedMultiplier.getAsDouble() * boost;
        boolean pathed;
        try {
            pathed = this.pet.getNavigation().moveTo(
                    this.owner.getX(),
                    this.owner.getY() + this.pet.followYOffset(),
                    this.owner.getZ(),
                    speed);
        } catch (Exception e) {
            // some snapshots have server-only casts inside client pathfinding
            pathed = false;
        }
        if (!pathed || this.pet.getNavigation().isDone()) {
            // pathfinding unavailable (or no path): steer straight at the owner with the
            // same probe-and-hop driving the sprint mode uses
            this.steerDirectly(this.owner.getX(), this.owner.getY() + this.pet.followYOffset(),
                    this.owner.getZ(), speed);
        }
    }

    // direct move-control steering with water and step/gap handling
    private void steerDirectly(double tx, double ty, double tz, double speed) {
        this.pet.getMoveControl().setWantedPosition(tx, ty, tz, speed);
        if (this.pet.followYOffset() > 0.0F) return;
        if (this.pet.isInWater()) {
            this.pet.setDeltaMovement(this.pet.getDeltaMovement().add(0.0, 0.05, 0.0));
            this.pet.getJumpControl().jump();
        } else if (this.pet.onGround()) {
            double dx = tx - this.pet.getX();
            double dz = tz - this.pet.getZ();
            double len = Math.sqrt(dx * dx + dz * dz);
            if (len > 0.01) {
                BlockPos ahead = BlockPos.containing(
                        this.pet.getX() + dx / len * 0.9, this.pet.getY() + 0.1, this.pet.getZ() + dz / len * 0.9);
                boolean blocked = this.blocksPath(ahead);
                boolean headroom = this.pet.level().getBlockState(ahead.above())
                        .getCollisionShape(this.pet.level(), ahead.above()).isEmpty();
                if ((blocked && headroom) || this.pet.horizontalCollision) {
                    this.pet.getJumpControl().jump();
                } else if (this.dropDepth(this.pet.getX() + dx / len, this.pet.getZ() + dz / len) > 4) {
                    // pit rule: don't walk off drops deeper than 3 blocks
                    this.pet.getMoveControl().setWantedPosition(
                            this.pet.getX(), this.pet.getY(), this.pet.getZ(), 0.0);
                }
            }
        }
    }

    /** Anything the pet can't just walk onto: taller than step height (carpets, plates don't count). */
    private boolean blocksPath(BlockPos pos) {
        net.minecraft.world.phys.shapes.VoxelShape shape =
                this.pet.level().getBlockState(pos).getCollisionShape(this.pet.level(), pos);
        return !shape.isEmpty() && shape.max(Direction.Axis.Y) > 0.5;
    }

    /** Blocks of air below foot level at (x, z), capped at 5. */
    private int dropDepth(double x, double z) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(
                Mth.floor(x), Mth.floor(this.pet.getY() + 0.1) - 1, Mth.floor(z));
        for (int d = 0; d < 5; d++) {
            if (!this.pet.level().getBlockState(pos).getCollisionShape(this.pet.level(), pos).isEmpty()) {
                return d;
            }
            pos.move(0, -1, 0);
        }
        return 5;
    }

    /** Entrance: place the pet behind the camera and burst it into frame. */
    private void leapEntrance() {
        // Prefer the current side; if its anchor is blocked, swap sides.
        Vec3 anchor = this.alongsideAnchor();
        if (!this.pet.canFitAt(anchor.x, anchor.y + this.pet.followYOffset(), anchor.z)) {
            this.alongsideSide = -this.alongsideSide;
            anchor = this.alongsideAnchor();
        }

        Vec3 behind = this.behindCameraPoint();

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
                if (!this.pet.level().getFluidState(pos).isEmpty()) {
                    continue;
                }
                placed = this.pet.tryRepositionTo(behind.x, pos.getY(), behind.z);
            }
        }
        if (!placed) {
            // no room behind the player, appear near the formation point instead
            if (!this.pet.tryRepositionTo(anchor.x, anchor.y + this.pet.followYOffset(), anchor.z)) {
                this.pet.repositionToOwner();
                return;
            }
        }
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

    /** Ground-scan placement near a point, for walkers. */
    private boolean placeOnGroundNear(Vec3 point) {
        for (int dy = 2; dy >= -3; dy--) {
            BlockPos pos = BlockPos.containing(point.x, this.owner.getY() + dy, point.z);
            BlockPos below = pos.below();
            if (!this.pet.level().getBlockState(below).isFaceSturdy(this.pet.level(), below, Direction.UP)) {
                continue;
            }
            if (!this.pet.level().getFluidState(pos).isEmpty()) {
                continue; // never underwater
            }
            if (this.pet.tryRepositionTo(point.x, pos.getY(), point.z)) {
                return true;
            }
        }
        return false;
    }

    /** Just behind the camera, slightly toward the formation side. */
    private Vec3 behindCameraPoint() {
        Vec3 forward = Vec3.directionFromRotation(0.0F, this.owner.getYHeadRot());
        forward = new Vec3(forward.x, 0.0, forward.z).normalize();
        Vec3 right = new Vec3(-forward.z, 0.0, forward.x);
        return this.owner.position()
                .subtract(forward.scale(2.2))
                .add(right.scale(0.9 * this.alongsideSide));
    }

    /** Formation point: ahead of the camera, offset to the pet's current side. */
    /** Owner head yaw from ~0.4s ago. */
    private float delayedHeadYaw() {
        if (this.yawIndex < 0) return this.owner.getYHeadRot();
        return this.yawHistory[this.yawIndex]; // oldest slot (about to be overwritten)
    }

    private Vec3 alongsideAnchor() {
        Vec3 forward = Vec3.directionFromRotation(0.0F, this.delayedHeadYaw());
        forward = new Vec3(forward.x, 0.0, forward.z).normalize();
        Vec3 right = new Vec3(-forward.z, 0.0, forward.x);

        Vec3 toPet = this.pet.position().subtract(this.owner.position());
        double lateral = toPet.x * right.x + toPet.z * right.z;
        if (this.alongsideSide == 0) {
            this.alongsideSide = lateral >= 0.0 ? 1 : -1;
        } else if (lateral * this.alongsideSide < -1.0) {
            this.alongsideSide = -this.alongsideSide;
        }

        // push the point farther out when looking level, closer when looking down
        float pitchDown = Math.max(0.0F, this.owner.getXRot());
        double forwardDistance = Mth.clamp(3.4 - pitchDown * 0.045, 2.0, 3.8);

        double fitY = this.owner.getY() + Math.max(0.1, this.pet.followYOffset());
        Vec3 base = this.owner.position().add(forward.scale(forwardDistance));
        Vec3 sideAnchor = base.add(right.scale(1.5 * this.alongsideSide));
        if (this.pet.canFitAt(sideAnchor.x, fitY, sideAnchor.z)) {
            return sideAnchor;
        }
        // side blocked, try the other one
        Vec3 otherAnchor = base.add(right.scale(-1.5 * this.alongsideSide));
        if (this.pet.canFitAt(otherAnchor.x, fitY, otherAnchor.z)) {
            this.alongsideSide = -this.alongsideSide;
            return otherAnchor;
        }
        // both sides blocked (tunnel/hallway), run single file ahead
        return this.owner.position().add(forward.scale(Math.max(2.0, forwardDistance * 0.7)));
    }

    /** Speed boost sized by how far the pet lags its formation point. */
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
