package io.github.aaronateataco.petsandpals.mob;

import io.github.aaronateataco.petsandpals.PetsInitializer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

/**
 * Ghost block for the spawn animation: rises out of the ground, releases the pet, sinks
 * back. Extends FallingBlockEntity so the vanilla renderer draws it; never touches the world.
 */
public class PetDwelling extends FallingBlockEntity {

    private static final int RISE_END = 10;
    private static final int PET_EXIT = 16;
    private static final int SINK_START = 50;
    private static final int SINK_END = 90;

    private double baseY;
    private AbstractPet pet;
    private int age;
    private boolean petReleased = false;

    public PetDwelling(EntityType<? extends @NotNull FallingBlockEntity> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
        this.noPhysics = true;
    }

    /**
     * Creates a dwelling at the given grid position, starting fully sunk into the ground.
     * The pet should already be positioned at the dwelling (invisible); it is revealed
     * mid-animation by {@link #releasePet()}.
     */
    public static PetDwelling create(Level level, BlockPos pos, BlockState state, AbstractPet pet) {
        PetDwelling dwelling = new PetDwelling(PetsInitializer.PET_DWELLING, level);
        dwelling.blockState = state;
        dwelling.baseY = pos.getY();
        dwelling.pet = pet;
        dwelling.setPos(pos.getX() + 0.5, pos.getY() - 1.0, pos.getZ() + 0.5);
        return dwelling;
    }

    @Override
    public void tick() {
        this.setOldPosAndRot();
        this.age++;
        double x = this.getX();
        double z = this.getZ();

        if (this.age <= RISE_END) {
            // ease-out rise
            double t = this.age / (double) RISE_END;
            double ease = 1.0 - (1.0 - t) * (1.0 - t);
            this.setPos(x, this.baseY - 1.0 + ease, z);
            if (this.age % 3 == 0) {
                this.crumbleParticles(4);
            }
        } else if (this.age >= PET_EXIT && !this.petReleased) {
            this.releasePet();
        }

        if (this.age >= SINK_START && this.age <= SINK_END) {
            // ease-in sink
            double t = (this.age - SINK_START) / (double) (SINK_END - SINK_START);
            this.setPos(x, this.baseY - 1.3 * t * t, z);
            if (this.age % 4 == 0) {
                this.crumbleParticles(3);
            }
        } else if (this.age > SINK_END) {
            if (!this.petReleased) {
                this.releasePet();
            }
            this.discard();
        }
    }

    private void releasePet() {
        this.petReleased = true;
        if (this.pet == null || this.pet.isRemoved()) {
            return;
        }
        this.pet.setInvisible(false);
        float volume = (float) AbstractPet.soundVolume.getAsDouble();
        if (volume > 0.0f) {
            this.level().playLocalSound(this.pet, SoundEvents.BEEHIVE_EXIT, SoundSource.NEUTRAL, volume, 1.0f);
        }
        for (int i = 0; i < 8; i++) {
            this.level().addParticle(
                    ParticleTypes.POOF,
                    this.pet.getRandomX(0.8), this.pet.getRandomY(), this.pet.getRandomZ(0.8),
                    0.0, 0.02, 0.0
            );
        }
    }

    private void crumbleParticles(int count) {
        for (int i = 0; i < count; i++) {
            this.level().addParticle(
                    new BlockParticleOption(ParticleTypes.BLOCK, this.getBlockState()),
                    this.getX() + (this.random.nextDouble() - 0.5) * 1.1,
                    this.baseY + 0.06,
                    this.getZ() + (this.random.nextDouble() - 0.5) * 1.1,
                    0.0, 0.08, 0.0
            );
        }
    }
}
