package io.github.aaronateataco.petsandpals.mob;

import io.github.aaronateataco.petsandpals.mob.ai.PetFollowOwnerGoal;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

import java.util.function.DoubleSupplier;

/**
 * Abstract class that extends {@link TamableAnimal}, providing multiple utilities
 * so that each class doesn't have to define the same logic. <p> When creating a custom entity,
 * always extend either {@link GroundPet}, {@link FlyingPet}, or {@link SlimeLikePet},
 * unless adding custom movement logic,
 * as this file does not contain custom movement logic for the entities.
 * <p> When creating a custom mob, always extend this class, as using the built-in
 * {@link GroundPet} or {@link FlyingPet} logic breaks them when acting as normal mobs.
 *
 * @see FlyingPet
 * @see GroundPet
 */
public abstract class AbstractPet extends TamableAnimal {

    /**
     * The user-adjustable pet speed multiplier ("Pet Speed" in the config screen).
     * Wired to the config in {@code Central}; kept as a supplier because this class
     * lives in the main source set and cannot reference the client config directly.
     */
    public static DoubleSupplier speedMultiplier = () -> 1.0;

    /**
     * @deprecated Only used by the bundled custom mobs' legacy tick logic; the goal-driven
     * movement classes no longer touch it.
     */
    @Deprecated
    protected int waitingTime = 0;

    protected AbstractPet(EntityType<? extends @NotNull TamableAnimal> type, Level level) {
        super(type, level);
        this.setSpeed(0.5f);
        this.copyVanillaAttributes(type);
        // Mob's constructor only calls registerGoals() when the level is a ServerLevel,
        // and pets only ever exist in the ClientLevel - so register goals ourselves.
        // Deliberately NOT the overridable registerGoals(): several bundled custom mobs
        // override it with server-only goals (e.g. BreedGoal casts to ServerLevel) that
        // were dead code before and crash if constructed on the client.
        if (level != null && level.isClientSide() && this.usesGoalMovement()) {
            this.registerDefaultPetGoals();
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes()
                .add(Attributes.MAX_HEALTH, 8.0F)
                .add(Attributes.MOVEMENT_SPEED, 0.25F)
                .add(Attributes.FLYING_SPEED, 0.4F);
    }

    /**
     * Pets that copy a vanilla mob (pets-and-pals:wolf, pets-and-pals:cat, ...) should move at
     * that mob's real speed. Looks up the same-named vanilla entity type and copies its
     * movement-relevant attribute base values; pets without a vanilla counterpart (duck, racoon,
     * april fools mobs, ...) keep the defaults from {@link #createAttributes()}.
     */
    @SuppressWarnings("unchecked")
    private void copyVanillaAttributes(EntityType<?> type) {
        Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(type);
        EntityType<?> vanillaType = BuiltInRegistries.ENTITY_TYPE
                .getOptional(Identifier.withDefaultNamespace(id.getPath()))
                .orElse(null);
        if (vanillaType == null || vanillaType == type) return;
        AttributeSupplier vanilla;
        try {
            vanilla = DefaultAttributes.getSupplier((EntityType<? extends LivingEntity>) vanillaType);
        } catch (Exception e) {
            return;
        }
        this.copyAttribute(vanilla, Attributes.MOVEMENT_SPEED);
        this.copyAttribute(vanilla, Attributes.FLYING_SPEED);
        this.copyAttribute(vanilla, Attributes.STEP_HEIGHT);
    }

    private void copyAttribute(AttributeSupplier vanilla, Holder<Attribute> attribute) {
        if (!vanilla.hasAttribute(attribute)) return;
        AttributeInstance instance = this.getAttribute(attribute);
        if (instance != null) {
            instance.setBaseValue(vanilla.getBaseValue(attribute));
        }
    }

    /**
     * Whether this pet's movement is driven by the vanilla goal/navigation system
     * ({@link GroundPet}, {@link FlyingPet}, {@link SlimeLikePet}). Custom mobs that
     * define their own bespoke tick logic return {@code false} and are left alone.
     */
    protected boolean usesGoalMovement() {
        return false;
    }

    /**
     * Pets are client-side only, so vanilla would normally skip all mob AI (goals,
     * navigation, move/look/jump controls run in {@code serverAiStep}). Returning
     * {@code true} here is what makes real pathfinding possible on the client.
     */
    @Override
    public boolean isEffectiveAi() {
        return this.usesGoalMovement() || super.isEffectiveAi();
    }

    /**
     * Makes {@code canSimulateMovement()} return true, so {@code LivingEntity.aiStep}
     * actually runs {@code travel()} (real physics from the move/jump controls) for this
     * client-side entity instead of waiting for server position packets that never come.
     */
    @Override
    protected boolean isLocalClientAuthoritative() {
        return this.usesGoalMovement() || super.isLocalClientAuthoritative();
    }

    private void registerDefaultPetGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(2, new PetFollowOwnerGoal(this, 1.0, this.stopDistance() + 2.0f, this.stopDistance()));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
    }

    @Override
    public void tick() {
        // Vanilla runs the whole mob AI (goals, navigation, move/look/jump controls) in
        // serverAiStep, which is hard-gated behind !level().isClientSide() - so for these
        // client-only pets we drive the same machinery ourselves. Runs before super.tick()
        // so travel() consumes the freshly computed movement inputs this same tick.
        if (this.usesGoalMovement() && this.level().isClientSide() && this.isAlive() && !this.isPassenger()) {
            this.getSensing().tick();
            this.goalSelector.tick();
            this.getNavigation().tick();
            this.getMoveControl().tick();
            this.getLookControl().tick();
            this.getJumpControl().tick();
        }

        super.tick();
        if (!this.usesGoalMovement()) return;

        LivingEntity owner = this.getOwner();
        if (owner != null && owner.hasPassenger(this)) {
            if (owner.isCrouching() && owner.isJumping()) {
                this.stopRiding();
                this.setOrderedToSit(false);
            } else {
                this.setOrderedToSit(true);
            }
        }

        if (this.random.nextInt(60 * 20) == 0 && this.getAmbientSound() != null) {
            this.level().playLocalSound(this, this.getAmbientSound(), SoundSource.NEUTRAL, 1.0f, 1.0f);
        }
    }

    /**
     * Clicking a pet must never shove it around: damage is server-side and the server
     * doesn't know this entity exists, so any client-side hit reaction is pure noise.
     */
    @Override
    public void knockback(double strength, double x, double z) {
    }

    /**
     * This method is used to determine when the entity stops moving, relative to the player.
     * For example, if we defined this:
     * <pre>{@code protected abstract int stopDistance() {
     *     return 2;
     * }}</pre>
     * The entity would stop moving towards the player when it is two blocks away from the player.
     */
    protected abstract int stopDistance();

    /**
     * Used to define how high the hearts that appear when you right-click on a pet will be.
     * Zero means it would appear right in the middle of the entity's hitbox.
     */
    protected abstract float heartHeight();

    /**
     * Used to define the entity's default ambient sound. For the uses of these last three
     * methods, please refer to {@link FlyingPet} and {@link GroundPet}.
     */
    protected abstract SoundEvent getAmbientSound();

    /**
     * Custom interactions.
     * - Right clicking on a pet with an empty hand will let make hearts appear above it.
     * - Shifting and right clicking on a pet with an empty hand will pick it up.
     *
     * @return It's super method
     */
    @Override
    public @NotNull InteractionResult mobInteract(@NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (this.isTame() && itemStack.isEmpty() && !player.isShiftKeyDown()) {
            this.level().addParticle(
                    ParticleTypes.HEART,
                    this.getX(),
                    this.getY() + this.heartHeight(),
                    this.getZ(),
                    5, 5, 5
            );
            return InteractionResult.SUCCESS;
        }

        if (this.isTame() && itemStack.isEmpty() && player.isShiftKeyDown()) {
            if (!this.isPassenger()) {
                this.startRiding(player);
                this.lookAt(player, 1f, 1f);
                return InteractionResult.SUCCESS;
            } else {
                this.stopRiding();
                this.setOrderedToSit(false);
            }
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    /**
     * Custom method required for making the mob work on servers.
     * <p> Calls: It's super method, if the level is not client-sided.
     */
    @Override
    public void onSyncedDataUpdated(@NotNull EntityDataAccessor<?> key) {
        if (!this.level().isClientSide()) {
            super.onSyncedDataUpdated(key);
        }
    }

    /**
     * IMPORTANT: Allows the entity to exist on servers, if only in the {@code ClientLevel}.
     * Never, under any circumstances, remove this method.
     */
    @Override
    public @NotNull Packet<@NotNull ClientGamePacketListener> getAddEntityPacket(@NotNull ServerEntity serverEntity) {
        if (this.level().isClientSide()) {
            return new ClientboundAddEntityPacket(this, serverEntity);
        } else {
            return super.getAddEntityPacket(serverEntity);
        }
    }

    /**
     * Calls the previous abstract method so other classes extending this one don't have to.
     *
     * @return False, as breeding and taming is not needed for any {@code Client-} mobs.
     * Make sure to override this when using a custom-made mob.
     */
    @Override
    public boolean isFood(@NotNull ItemStack itemStack) {
        return false;
    }

    /**
     * These entities are not used in server worlds - as such, there is no reason to
     * return anything. However, this must be overridden when creating a custom entity.
     *
     * @return {@code null}
     */
    @Override
    public @Nullable AgeableMob getBreedOffspring(@NotNull ServerLevel serverLevel, @NotNull AgeableMob ageableMob) {
        return null;
    }

    /**
     * Easier way to call {@link TamableAnimal#setCustomName} that takes a String rather than a {@link Component}
     */
    public void setName(String string) {
        this.setCustomName(Component.literal(string));
    }

    /**
     * @deprecated Pets no longer wander away from an idle owner; movement is handled by the
     * goal system ({@link PetFollowOwnerGoal}). Kept as a no-op because the bundled custom
     * mobs (duck, racoon, koi, ...) still call it from their bespoke tick logic.
     */
    @Deprecated
    public void wander() {
    }
}
