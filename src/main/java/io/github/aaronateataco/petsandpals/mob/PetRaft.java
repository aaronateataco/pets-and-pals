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
public class PetRaft extends FallingBlockEntity {

    private AbstractPet pet;
    private Vec3 velocity = Vec3.ZERO;
    private int idleTimer = 40;
    private int idleAction = 0; // 0 stand, 1 sit, 2 look left, 3 look right


    public PetRaft(EntityType<? extends @NotNull FallingBlockEntity> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
        this.noPhysics = true;
    }

    public static PetRaft create(Level level, AbstractPet pet, AbstractBoat boat) {
        PetRaft raft = new PetRaft(PetsInitializer.PET_RAFT, level);
        raft.blockState = PetsInitializer.PET_RAFT_BLOCK.defaultBlockState();
        raft.pet = pet;
        Vec3 side = sideAnchor(boat);
        raft.setPos(side.x, side.y, side.z);
        return raft;
    }

    private static Vec3 sideAnchor(AbstractBoat boat) {
        float rad = boat.getYRot() * ((float) Math.PI / 180.0F);
        Vec3 right = new Vec3(-Mth.cos(rad), 0.0, -Mth.sin(rad));
        return boat.position().add(right.scale(2.3));
    }

    @Override
    public void tick() {
        this.setOldPosAndRot();
        if (this.pet == null || this.pet.isRemoved() || !this.pet.isRafted()) {
            this.release();
            return;
        }
        LivingEntity owner = this.pet.getOwner();
        if (owner == null || !owner.isAlive() || owner.level() != this.level()
                || !(owner.getVehicle() instanceof AbstractBoat boat)) {
            this.release();
            return;
        }

        // towed-feel physics: spring toward the anchor with damping so the raft
        // swings wide in turns and settles, instead of gliding on rails
        Vec3 anchor = sideAnchor(boat).add(0.0, 0.03 * Math.sin(this.tickCount * 0.09), 0.0);
        // sit on top of the water: boats float sunken, the raft base is a flat box
        anchor = new Vec3(anchor.x, boat.getY() + 0.45, anchor.z);
        Vec3 delta = anchor.subtract(this.position());
        if (delta.length() > 12.0) {
            this.snapTo(anchor.x, anchor.y, anchor.z, 0.0F, 0.0F);
            this.velocity = Vec3.ZERO;
        } else {
            this.velocity = this.velocity.add(delta.scale(0.06)).scale(0.80);
            Vec3 next = this.position().add(this.velocity);
            this.setPos(next.x, next.y, next.z);
        }

        // pet rides the deck
        this.pet.setPos(this.getX(), this.getY() + 0.19, this.getZ());
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

    private void release() {
        if (this.pet != null && !this.pet.isRemoved()) {
            this.pet.setInSittingPose(false);
            this.pet.setRafted(false);
        }
        this.discard();
    }
}
