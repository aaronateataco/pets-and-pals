package io.github.aaronateataco.petsandpals.mob;

import io.github.aaronateataco.petsandpals.PetsInitializer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

/**
 * The pet's ghost form: a floating nether star that glides through anything back to the
 * owner and turns back into the pet once there's room. Used when the pet can't follow.
 */
public class PetOrb extends Entity {

    private AbstractPet pet;

    public PetOrb(EntityType<? extends @NotNull PetOrb> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    public static PetOrb create(Level level, AbstractPet pet) {
        PetOrb orb = new PetOrb(PetsInitializer.PET_ORB, level);
        orb.pet = pet;
        orb.setPos(pet.getX(), pet.getY() + pet.getBbHeight() * 0.5, pet.getZ());
        return orb;
    }

    @Override
    public void tick() {
        this.setOldPosAndRot();
        if (this.pet == null || this.pet.isRemoved() || !this.pet.isOrbMode()) {
            this.discard();
            return;
        }
        LivingEntity owner = this.pet.getOwner();
        if (owner == null || !owner.isAlive() || owner.isRemoved() || owner.level() != this.level()) {
            // owner gone, drop the pet here
            this.materializeAt(this.position());
            return;
        }

        // floats at the owner's hip normally, or in the run-alongside spot while sprinting
        double bob = Math.sin(this.tickCount * 0.15) * 0.08;
        Vec3 anchor;
        if (owner.isSprinting() && AbstractPet.firstPersonView.getAsBoolean()) {
            Vec3 forward = Vec3.directionFromRotation(0.0F, owner.getYHeadRot());
            forward = new Vec3(forward.x, 0.0, forward.z).normalize();
            Vec3 fRight = new Vec3(-forward.z, 0.0, forward.x);
            anchor = owner.position().add(forward.scale(2.6)).add(fRight.scale(1.2))
                    .add(0.0, 1.2 + bob, 0.0);
        } else {
            float rad = owner.yBodyRot * ((float) Math.PI / 180.0F);
            Vec3 right = new Vec3(-Mth.cos(rad), 0.0, -Mth.sin(rad));
            anchor = owner.position().add(right.scale(0.9)).add(0.0, 1.1 + bob, 0.0);
        }

        Vec3 delta = anchor.subtract(this.position());
        double distance = delta.length();
        if (distance > 24.0) {
            this.snapTo(anchor.x, anchor.y, anchor.z, 0.0F, 0.0F);
        } else {
            double k = Mth.clamp(0.08 + distance * 0.1, 0.08, 0.5);
            Vec3 next = this.position().add(delta.scale(k));
            this.setPos(next.x, next.y, next.z);
        }

        // the orb carries the pet's position while in ghost form
        this.pet.setPos(this.getX(), this.getY() - this.pet.getBbHeight() * 0.5, this.getZ());
        this.pet.setDeltaMovement(Vec3.ZERO);

        // sparkle trail so the little star is easy to spot
        if (this.tickCount % 4 == 0) {
            this.level().addParticle(ParticleTypes.END_ROD,
                    this.getX(), this.getY() + 0.25, this.getZ(), 0.0, 0.0, 0.0);
        }

        // near the owner: look for a spot to reform
        if (this.tickCount > 15 && this.tickCount % 8 == 0 && distance < 5.0) {
            Vec3 spot = this.findMaterializeSpot(owner);
            if (spot != null) {
                this.materializeAt(spot);
            }
        }
    }

    private void materializeAt(Vec3 spot) {
        if (this.pet != null && !this.pet.isRemoved()) {
            this.pet.exitOrbMode(spot);
            float volume = (float) AbstractPet.soundVolume.getAsDouble();
            if (volume > 0.0f) {
                this.level().playLocalSound(this.pet, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.NEUTRAL, volume, 1.2f);
            }
            for (int i = 0; i < 10; i++) {
                this.level().addParticle(
                        ParticleTypes.END_ROD,
                        this.pet.getRandomX(0.7), this.pet.getRandomY(), this.pet.getRandomZ(0.7),
                        0.0, 0.02, 0.0
                );
            }
        }
        this.discard();
    }

    /** Hover space for flyers, standable ground for walkers. */
    private Vec3 findMaterializeSpot(LivingEntity owner) {
        Level level = this.level();
        for (int attempt = 0; attempt < 8; attempt++) {
            float yaw = owner.getYHeadRot() + (this.random.nextFloat() * 80.0F - 40.0F);
            Vec3 direction = Vec3.directionFromRotation(0.0F, yaw);
            double distance = 2.0 + this.random.nextDouble() * 2.0;
            double x = owner.getX() + direction.x * distance;
            double z = owner.getZ() + direction.z * distance;

            if (this.pet.followYOffset() > 0.0F) {
                double y = owner.getY() + this.pet.followYOffset();
                if (this.petFitsAt(level, x, y, z)) {
                    return new Vec3(x, y, z);
                }
            } else {
                for (int dy = 2; dy >= -3; dy--) {
                    BlockPos pos = BlockPos.containing(x, owner.getY() + dy, z);
                    BlockPos below = pos.below();
                    if (!level.getBlockState(below).isFaceSturdy(level, below, Direction.UP)) {
                        continue;
                    }
                    if (this.petFitsAt(level, x, pos.getY(), z)) {
                        return new Vec3(x, pos.getY(), z);
                    }
                }
            }
        }
        return null;
    }

    private boolean petFitsAt(Level level, double x, double y, double z) {
        AABB box = this.pet.getBoundingBox().move(
                x - this.pet.getX(),
                y - this.pet.getY(),
                z - this.pet.getZ()
        );
        return level.noCollision(this.pet, box);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
    }

    @Override
    protected void readAdditionalSaveData(@NotNull ValueInput input) {
    }

    @Override
    protected void addAdditionalSaveData(@NotNull ValueOutput output) {
    }

    @Override
    public boolean hurtServer(@NotNull ServerLevel level, @NotNull DamageSource source, float amount) {
        return false;
    }
}
