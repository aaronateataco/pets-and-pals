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
    private net.minecraft.world.entity.Leashable.LeashData leashData;
    private int idleTimer = 40;
    private int idleAction = 0; // 0 stand, 1 sit, 2 look left, 3 look right


    public PetRaft(EntityType<? extends @NotNull FallingBlockEntity> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
        this.noPhysics = true;
    }

    public static PetRaft create(Level level, AbstractPet pet, AbstractBoat boat) {
        PetRaft raft = new PetRaft(PetsInitializer.PET_RAFT, level);
        raft.blockState = PetsInitializer.PET_RAFT_BLOCK.defaultBlockState()
                .setValue(PetRaftBlock.STYLE, styleFor(boat));
        raft.pet = pet;
        Vec3 side = sideAnchor(boat);
        raft.setPos(side.x, side.y, side.z);
        raft.setLeashedTo(boat, false);
        return raft;
    }

    @Override
    public net.minecraft.world.entity.Leashable.LeashData getLeashData() {
        return this.leashData;
    }

    @Override
    public void setLeashData(net.minecraft.world.entity.Leashable.@NotNull LeashData leashData) {
        this.leashData = leashData;
    }

    private static final double ROPE_LENGTH = 2.8;

    /** Ferry: pops in when a land pet must cross water to reach you - no boat involved. */
    public static PetRaft createFerry(Level level, AbstractPet pet) {
        PetRaft raft = new PetRaft(PetsInitializer.PET_RAFT, level);
        raft.blockState = PetsInitializer.PET_RAFT_BLOCK.defaultBlockState()
                .setValue(PetRaftBlock.STYLE, Math.floorMod(AbstractPet.raftStyle.getAsInt(), PetRaftBlock.WOODS.length));
        raft.pet = pet;
        raft.ferry = true;
        raft.setPos(pet.getX(), pet.getY(), pet.getZ());
        return raft;
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
        if (ropeDistance > 14.0) {
            Vec3 reset = sideAnchor(boat);
            double y0 = this.waterSurfaceY(reset.x, boat.getY(), reset.z);
            this.snapTo(reset.x, y0 - 0.01, reset.z, 0.0F, 0.0F);
            this.velocity = Vec3.ZERO;
        } else {
            if (ropeDistance > ROPE_LENGTH) {
                this.velocity = this.velocity.add(toBoat.scale((ropeDistance - ROPE_LENGTH) * 0.12 / ropeDistance));
            }
            this.velocity = this.velocity.scale(0.86);
            double nextX = this.getX() + this.velocity.x;
            double nextZ = this.getZ() + this.velocity.z;
            // locked to water: never slides onto land - if the next column has no water,
            // stay put and bleed the motion off
            double surface = this.waterSurfaceY(nextX, boat.getY(), nextZ);
            if (surface == Double.MIN_VALUE) {
                this.velocity = this.velocity.scale(0.3);
            } else {
                double bob = 0.02 * Math.sin(this.tickCount * 0.09);
                this.setPos(nextX, surface - 0.01 + bob, nextZ);
            }
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
        float baseYaw = boat.getYRot();
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
        double step = Math.min(0.28, distance);
        double nextX = this.getX() + toOwner.x / Math.max(0.001, distance) * step;
        double nextZ = this.getZ() + toOwner.z / Math.max(0.001, distance) * step;
        double surface = this.waterSurfaceY(nextX, this.getY(), nextZ);
        if (surface == Double.MIN_VALUE || distance < 2.0) {
            this.release();
            return;
        }
        this.setPos(nextX, surface - 0.01 + 0.02 * Math.sin(this.tickCount * 0.09), nextZ);
        this.pet.setPos(this.getX(), this.getY() + 0.07, this.getZ());
        this.pet.setDeltaMovement(Vec3.ZERO);
        this.pet.fallDistance = 0;
        float yaw = (float) (Math.toDegrees(Mth.atan2(toOwner.z, toOwner.x))) - 90.0F;
        this.pet.setYRot(yaw);
        this.pet.yBodyRot = yaw;
        this.pet.setYHeadRot(yaw);
    }

    private void release() {
        if (this.pet != null && !this.pet.isRemoved()) {
            this.pet.setInSittingPose(false);
            this.pet.setRafted(false);
        }
        this.discard();
    }
}
