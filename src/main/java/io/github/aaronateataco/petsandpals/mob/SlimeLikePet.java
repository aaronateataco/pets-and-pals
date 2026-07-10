package io.github.aaronateataco.petsandpals.mob;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

/**
 * Abstract class representing slimes or any animal that bounces up and down repeatedly.
 * <p>
 * Movement uses the same goal/navigation system as {@link GroundPet}, but with a hop-based
 * {@link MoveControl} modeled on the vanilla slime's: the pet only gains ground while
 * jumping, giving the characteristic squish-hop instead of a slide.
 */
public abstract class SlimeLikePet extends AbstractPet {

    public SlimeLikePet(EntityType<? extends @NotNull TamableAnimal> entityType, Level level) {
        super(entityType, level);
        this.moveControl = new HopMoveControl(this);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return AbstractPet.createAttributes().add(Attributes.JUMP_STRENGTH, 0.42f);
    }

    @Override
    protected boolean usesGoalMovement() {
        return true;
    }

    /**
     * Modeled on the vanilla {@code Slime.SlimeMoveControl}: while path-following, the pet
     * turns toward the next path point, waits a short random delay on the ground, then hops.
     */
    static class HopMoveControl extends MoveControl {
        private final SlimeLikePet pet;
        private int jumpDelay;

        HopMoveControl(SlimeLikePet pet) {
            super(pet);
            this.pet = pet;
        }

        @Override
        public void tick() {
            if (this.operation != Operation.MOVE_TO) {
                this.pet.setZza(0.0F);
                return;
            }

            double dx = this.wantedX - this.pet.getX();
            double dz = this.wantedZ - this.pet.getZ();
            if (dx * dx + dz * dz < 0.25) {
                this.operation = Operation.WAIT;
                this.pet.setZza(0.0F);
                return;
            }

            float targetYRot = (float) (Mth.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0F;
            this.pet.setYRot(this.rotlerp(this.pet.getYRot(), targetYRot, 90.0F));
            this.pet.yHeadRot = this.pet.getYRot();
            this.pet.yBodyRot = this.pet.getYRot();

            float speed = (float) (this.speedModifier * this.pet.getAttributeValue(Attributes.MOVEMENT_SPEED));
            if (this.pet.onGround()) {
                if (--this.jumpDelay <= 0) {
                    this.jumpDelay = this.pet.getRandom().nextInt(20) + 10;
                    this.pet.getJumpControl().jump();
                    this.pet.setSpeed(speed);
                } else {
                    this.pet.setSpeed(0.0F);
                    this.pet.setZza(0.0F);
                }
            } else {
                this.pet.setSpeed(speed);
            }
        }
    }
}
