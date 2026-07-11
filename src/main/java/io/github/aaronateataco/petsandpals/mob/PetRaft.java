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

        // trail the boat with a light lerp, tiny bob so it reads as floating
        Vec3 anchor = sideAnchor(boat).add(0.0, 0.02 * Math.sin(this.tickCount * 0.1), 0.0);
        Vec3 delta = anchor.subtract(this.position());
        double distance = delta.length();
        if (distance > 12.0) {
            this.snapTo(anchor.x, anchor.y, anchor.z, 0.0F, 0.0F);
        } else {
            double k = Mth.clamp(0.12 + distance * 0.15, 0.12, 0.6);
            Vec3 next = this.position().add(delta.scale(k));
            this.setPos(next.x, next.y, next.z);
        }

        // pet rides the deck
        this.pet.setPos(this.getX(), this.getY() + 0.19, this.getZ());
        this.pet.setDeltaMovement(Vec3.ZERO);
        this.pet.fallDistance = 0;
        this.pet.setYRot(boat.getYRot());
        this.pet.yBodyRot = boat.getYRot();
        this.pet.setYHeadRot(boat.getYRot());
    }

    private void release() {
        if (this.pet != null && !this.pet.isRemoved()) {
            this.pet.setRafted(false);
        }
        this.discard();
    }
}
