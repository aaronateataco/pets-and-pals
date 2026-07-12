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

    // default Leashable.getLeashOffset() anchors at getEyeHeight() - inherited from
    // FallingBlockEntity's normal ~1-block hitbox, nowhere near the raft's actual thin
    // deck. That's the rope floating up over the raft instead of meeting it at hull
    // level. Anchor it right at the deck surface instead.
    @Override
    public @NotNull Vec3 getLeashOffset() {
        return new Vec3(0.0, 0.1, 0.0);
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

        // spring toward a target recomputed fresh off the boat's own transform every
        // tick - not simulated independently with accumulated velocity, so it can't
        // drift or desync the way the old rope model did, but still has some physical
        // give/chase to it instead of teleport-following exactly frame to frame
        float rad = boat.getYRot() * ((float) Math.PI / 180.0F);
        Vec3 back = new Vec3(-Mth.sin(rad), 0.0, Mth.cos(rad));
        double attachDistance = ROPE_LENGTH + (this.scale - 1.0) * 0.6;
        Vec3 target = boat.position().subtract(back.scale(attachDistance));
        double bob = 0.02 * Math.sin(this.tickCount * 0.09);
        // boat.getY() sits at the bottom of the boat's own hitbox, which rides lower
        // than the actual water surface (the boat's renderer floats its hull visually
        // above that point) - copying it straight onto the raft's block-bottom origin
        // sank the raft noticeably below the boat's own waterline. Anchor to the real
        // water surface instead, same as the ferry raft already does.
        double surface = this.waterSurfaceY(target.x, boat.getY(), target.z);
        double baseY = surface == Double.MIN_VALUE ? boat.getY() : surface - 0.01;
        Vec3 targetPos = new Vec3(target.x, baseY + bob + this.riseIn(), target.z);
        Vec3 nextPos = this.position().lerp(targetPos, 0.55);
        this.velocity = nextPos.subtract(this.position());
        this.setPos(nextPos.x, nextPos.y, nextPos.z);
        // hull leans into its motion a little, then settles to match the boat's
        // heading - eased either way, so a sharp turn doesn't snap it instantly
        float targetYaw = this.velocity.horizontalDistanceSqr() > 1.0e-5
                ? (float) Math.toDegrees(Mth.atan2(this.velocity.z, this.velocity.x)) - 90.0F
                : boat.getYRot();
        this.setYRot(Mth.approachDegrees(this.getYRot(), targetYaw, 14.0F));

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

        // let go once the owner themself is on dry land and the raft's caught up -
        // used to release the moment the raft's OWN next step found any dry patch,
        // which could be a mid-lake island or the near bank of a second river rather
        // than where the owner actually was. That dumped the pet mid-crossing, it
        // walked a few steps, hit water again, and got re-rafted - the "swims then
        // rafts then swims" loop. Owner-grounded is the only signal that actually
        // means the crossing is done.
        if (!owner.isInWater() && distance < 4.0) {
            // don't let go until the raft itself has actually reached dry ground too
            // (or is basically at the owner's feet) - releasing while it's still over
            // open water stranded the pet mid-lake with no swim goal to get it out,
            // which showed up as the stuck/out-of-view rescue logic teleporting it
            // around erratically trying to recover
            double raftSurface = this.waterSurfaceY(this.getX(), this.getY(), this.getZ());
            if (raftSurface == Double.MIN_VALUE || distance < 1.5) {
                this.release();
                return;
            }
        }

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
            // no water under the raft's own path (mid-lake island, rocks) - hold in
            // place and wait rather than beaching somewhere that isn't actually
            // where the owner is; if the raft itself is stranded on dry ground, let
            // go here since there's nothing left to ferry across
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
