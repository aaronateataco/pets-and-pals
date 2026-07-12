package io.github.aaronateataco.petsandpals.mob;

import io.github.aaronateataco.petsandpals.PetsInitializer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

/**
 * Little raft that floats alongside the owner's boat so land pets don't have to swim.
 * Purely visual (renders the pet_raft block, no collision); the pet stands on deck and
 * hops off when the owner leaves the boat.
 */
public class PetRaft extends FallingBlockEntity implements net.minecraft.world.entity.Leashable {

    private AbstractPet pet;
    private boolean ferry = false;
    private Vec3 velocity = Vec3.ZERO;
    private float scale = 1.0F;
    private net.minecraft.world.entity.Leashable.LeashData leashData;
    private int idleTimer = 40;
    private int idleAction = 0; // 0 stand, 1 sit, 2 look left, 3 look right


    public PetRaft(EntityType<? extends @NotNull FallingBlockEntity> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
        this.noPhysics = true;
    }

    // AbstractBoat actively pushes away anything nearby where canBeCollidedWith() or
    // isPushable() is true, every tick - the raft was fighting that push against its own
    // tow physics the entire time, which is a big part of why the tow looked broken
    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean canBeCollidedWith(@NotNull net.minecraft.world.entity.Entity entity) {
        return false;
    }

    public static PetRaft create(Level level, AbstractPet pet, AbstractBoat boat) {
        PetRaft raft = new PetRaft(PetsInitializer.PET_RAFT, level);
        raft.blockState = PetsInitializer.PET_RAFT_BLOCK.defaultBlockState()
                .setValue(PetRaftBlock.STYLE, styleFor(boat))
                .setValue(PetRaftBlock.CUSHION, cushionValue());
        raft.pet = pet;
        raft.scale = scaleFor(pet);
        Vec3 side = sideAnchor(boat);
        raft.setPos(side.x, side.y, side.z);
        raft.setLeashedTo(boat, false);
        return raft;
    }

    /** Raft grows with its passenger; the base deck fits a fox. */
    private static float scaleFor(AbstractPet pet) {
        return pet == null ? 1.0F : Mth.clamp(pet.getBbWidth() / 0.7F, 1.0F, 2.4F);
    }

    public float renderScale() {
        return this.scale;
    }

    @Override
    public net.minecraft.world.entity.Leashable.LeashData getLeashData() {
        return this.leashData;
    }

    @Override
    public void setLeashData(net.minecraft.world.entity.Leashable.@NotNull LeashData leashData) {
        this.leashData = leashData;
    }

    // boat + raft are each roughly 1-1.4 blocks long; 2.8 center-to-center left a wide
    // gap of open water between their edges that read as "not connected" to the boat
    private static final double ROPE_LENGTH = 1.6;

    /** Ferry: pops in when a land pet must cross water to reach you - no boat involved. */
    public static PetRaft createFerry(Level level, AbstractPet pet) {
        PetRaft raft = new PetRaft(PetsInitializer.PET_RAFT, level);
        raft.blockState = PetsInitializer.PET_RAFT_BLOCK.defaultBlockState()
                .setValue(PetRaftBlock.STYLE, Math.floorMod(AbstractPet.raftStyle.getAsInt(), PetRaftBlock.WOODS.length))
                .setValue(PetRaftBlock.CUSHION, cushionValue());
        raft.pet = pet;
        raft.scale = scaleFor(pet);
        raft.ferry = true;
        raft.setPos(pet.getX(), pet.getY(), pet.getZ());
        return raft;
    }

    private static int cushionValue() {
        return Math.floorMod(AbstractPet.cushionColor.getAsInt(), PetRaftBlock.NO_CUSHION + 1);
    }

    /** Match the raft wood to the boat being ridden; config style is the fallback. */
    private static int styleFor(AbstractBoat boat) {
        String path = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(boat.getType()).getPath();
        for (int i = 0; i < PetRaftBlock.WOODS.length; i++) {
            if (path.startsWith(PetRaftBlock.WOODS[i])) return i;
        }
        return Math.floorMod(AbstractPet.raftStyle.getAsInt(), PetRaftBlock.WOODS.length);
    }

    private static Vec3 sideAnchor(AbstractBoat boat) {
        // spawn position: directly behind the boat
        float rad = boat.getYRot() * ((float) Math.PI / 180.0F);
        Vec3 forward = new Vec3(-Mth.sin(rad), 0.0, Mth.cos(rad));
        return boat.position().subtract(forward.scale(ROPE_LENGTH));
    }

    @Override
    public void tick() {
        this.setOldPosAndRot();
        if (this.pet == null || this.pet.isRemoved() || !this.pet.isRafted()) {
            this.release();
            return;
        }
        LivingEntity owner = this.pet.getOwner();
        if (owner == null || !owner.isAlive() || owner.level() != this.level()) {
            this.release();
            return;
        }
        if (this.ferry) {
            this.tickFerry(owner);
            return;
        }
        if (!(owner.getVehicle() instanceof AbstractBoat boat)) {
            this.release();
            return;
        }

        // rope physics: the raft is pulled only when the rope to the boat goes taut,
        // drifts with water drag otherwise - it trails behind, swings wide in turns,
        // and straightens out when cruising
        Vec3 toBoat = new Vec3(boat.getX() - this.getX(), 0.0, boat.getZ() - this.getZ());
        double ropeDistance = toBoat.length();
        // bigger rafts ride a little farther back
        double ropeLength = ROPE_LENGTH + (this.scale - 1.0) * 0.6;
        if (ropeDistance > 14.0) {
            Vec3 reset = sideAnchor(boat);
            double y0 = this.waterSurfaceY(reset.x, boat.getY(), reset.z);
            this.snapTo(reset.x, y0 - 0.01, reset.z, boat.getYRot(), 0.0F);
            this.velocity = Vec3.ZERO;
        } else {
            if (ropeDistance > ropeLength) {
                this.velocity = this.velocity.add(toBoat.scale((ropeDistance - ropeLength) * 0.12 / ropeDistance));
            }
            // same drag as the ferry glide (0.9) - this used to be noticeably twitchier
            // than the ferry despite towing the same kind of hull
            this.velocity = this.velocity.scale(0.9);
            double nextX = this.getX() + this.velocity.x;
            double nextZ = this.getZ() + this.velocity.z;
            // locked to water: never slides onto land - if the next column has no water,
            // stay put and bleed the motion off. Searches around the raft's own Y, not
            // the boat's - over uneven water (waterfalls, locks) those can differ by more
            // than the search range, which read as "no water" and stalled the tow entirely
            double surface = this.waterSurfaceY(nextX, this.getY(), nextZ);
            if (surface == Double.MIN_VALUE) {
                this.velocity = this.velocity.scale(0.3);
            } else {
                double bob = 0.02 * Math.sin(this.tickCount * 0.09);
                this.setPos(nextX, surface - 0.01 + bob + this.riseIn(), nextZ);
            }
            // hull swings like a towed boat: the leashed bow leads toward the rope
            // when it's taut, otherwise the hull drifts around to face its motion
            float targetYaw;
            if (ropeDistance > ropeLength * 0.9) {
                targetYaw = (float) Math.toDegrees(Mth.atan2(toBoat.z, toBoat.x)) - 90.0F;
            } else if (this.velocity.horizontalDistanceSqr() > 4.0e-4) {
                targetYaw = (float) Math.toDegrees(Mth.atan2(this.velocity.z, this.velocity.x)) - 90.0F;
            } else {
                targetYaw = this.getYRot();
            }
            // turn rate follows speed so the hull feels heavy in the water
            float turn = (float) Mth.clamp(2.0 + this.velocity.horizontalDistance() * 25.0, 2.0, 9.0);
            this.setYRot(Mth.approachDegrees(this.getYRot(), targetYaw, turn));
        }

        // pet rides the deck (hull is 1px, deck top is +0.0625)
        this.pet.setPos(this.getX(), this.getY() + 0.07, this.getZ());
        this.pet.setDeltaMovement(Vec3.ZERO);
        this.pet.fallDistance = 0;
        this.tickDeckIdle(boat);
    }

    // little life on deck: sitting, standing back up, looking around
    private void tickDeckIdle(AbstractBoat boat) {
        if (--this.idleTimer <= 0) {
            this.idleTimer = 50 + this.pet.getRandom().nextInt(120);
            this.idleAction = this.pet.getRandom().nextInt(4);
            this.pet.setInSittingPose(this.idleAction == 1);
        }
        // pet stands square on the deck, so it turns with the hull
        float baseYaw = this.getYRot();
        this.pet.setYRot(baseYaw);
        this.pet.yBodyRot = baseYaw;
        float headYaw = switch (this.idleAction) {
            case 2 -> baseYaw - 45.0F;
            case 3 -> baseYaw + 45.0F;
            default -> baseYaw;
        };
        // ease the head toward its target so glances look natural
        this.pet.setYHeadRot(Mth.approachDegrees(this.pet.getYHeadRot(), headYaw, 4.0F));
    }

    // spawn entrance: the raft floats up out of the water instead of popping in
    private double riseIn() {
        if (this.tickCount >= 8) return 0.0;
        double t = this.tickCount / 8.0;
        return -0.35 * (1.0 - t) * (1.0 - t);
    }

    // actual water surface at this column; MIN_VALUE when there's no water (land)
    private double waterSurfaceY(double x, double aroundY, double z) {
        for (int dy = 2; dy >= -2; dy--) {
            net.minecraft.core.BlockPos pos = net.minecraft.core.BlockPos.containing(x, aroundY + dy, z);
            net.minecraft.world.level.material.FluidState fluid = this.level().getFluidState(pos);
            if (!fluid.isEmpty() && this.level().getFluidState(pos.above()).isEmpty()) {
                return pos.getY() + fluid.getHeight(this.level(), pos);
            }
        }
        return Double.MIN_VALUE;
    }

    // carries the pet toward the owner across water, drops it at the far shore
    private void tickFerry(LivingEntity owner) {
        Vec3 toOwner = new Vec3(owner.getX() - this.getX(), 0.0, owner.getZ() - this.getZ());
        double distance = toOwner.length();

        // same feel as the tow rope: thrust toward the owner, water drag, so the
        // hull coasts and eases to a stop instead of braking dead at the mark
        if (distance > 2.5) {
            this.velocity = this.velocity.add(toOwner.scale(0.042 / Math.max(0.001, distance)));
        }
        this.velocity = this.velocity.scale(0.9);
        double speed = this.velocity.horizontalDistance();
        if (speed > 0.38) {
            this.velocity = this.velocity.scale(0.38 / speed);
        }

        double nextX = this.getX() + this.velocity.x;
        double nextZ = this.getZ() + this.velocity.z;
        double surface = this.waterSurfaceY(nextX, this.getY(), nextZ);
        if (surface == Double.MIN_VALUE) {
            if (distance > 2.0) {
                this.release(); // reached the shore the owner is on
                return;
            }
            // owner swimming right here: bleed off against the bank and hold
            this.velocity = this.velocity.scale(0.3);
            nextX = this.getX();
            nextZ = this.getZ();
            surface = this.waterSurfaceY(nextX, this.getY(), nextZ);
            if (surface == Double.MIN_VALUE) { this.release(); return; }
        }
        this.setPos(nextX, surface - 0.01 + 0.02 * Math.sin(this.tickCount * 0.09) + this.riseIn(), nextZ);
        this.pet.setPos(this.getX(), this.getY() + 0.07, this.getZ());
        this.pet.setDeltaMovement(Vec3.ZERO);
        this.pet.fallDistance = 0;

        // bow follows the motion while under way, holds its line while coasting
        if (speed > 0.02) {
            float yaw = (float) Math.toDegrees(Mth.atan2(this.velocity.z, this.velocity.x)) - 90.0F;
            float turn = (float) Mth.clamp(2.0 + speed * 25.0, 2.0, 9.0);
            this.setYRot(Mth.approachDegrees(this.getYRot(), yaw, turn));
        }
        // pet turns with the hull, not straight at the target
        this.pet.setYRot(this.getYRot());
        this.pet.yBodyRot = this.getYRot();
        this.pet.setYHeadRot(this.getYRot());
    }

    private void release() {
        if (this.pet != null && !this.pet.isRemoved()) {
            this.pet.setInSittingPose(false);
            this.pet.setRafted(false);
        }
        this.discard();
    }
}
