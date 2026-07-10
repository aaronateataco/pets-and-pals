package io.github.aaronateataco.petsandpals.mob;

import io.github.aaronateataco.petsandpals.mob.ai.PetFollowOwnerGoal;
import io.github.aaronateataco.petsandpals.mob.ai.PetLookAtPlayerGoal;
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
import net.minecraft.util.Mth;
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
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MaceItem;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
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
     * The user-adjustable pet sound volume ("Pet Volume" in the config screen, 0 = muted).
     * Applied to every sound the pets play; wired to the config in {@code Central}.
     */
    public static DoubleSupplier soundVolume = () -> 1.0;

    /**
     * Whether the game is currently in first-person view; wired to the client camera in
     * {@code Central}. Used by the sprint run-alongside behavior, which is first-person only.
     */
    public static java.util.function.BooleanSupplier firstPersonView = () -> true;

    /**
     * Spawns a client-side entity into the current ClientLevel; wired in {@code Central}
     * because this main-source class can't reference client classes directly.
     */
    public static java.util.function.Consumer<net.minecraft.world.entity.Entity> clientEntitySpawner = e -> {};

    /**
     * @deprecated Only used by the bundled custom mobs' legacy tick logic; the goal-driven
     * movement classes no longer touch it.
     */
    @Deprecated
    protected int waitingTime = 0;

    private boolean perched = false;
    private int combatPerchTimer = 0;
    private int ownerSprintTicks = 0;
    private int outOfViewTicks = 0;
    private int stuckScore = 0;
    private double lastTrackedDistanceSqr = 0.0;

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
        this.goalSelector.addGoal(3, new PetLookAtPlayerGoal(this, 8.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
    }

    @Override
    public void tick() {
        // Fortnite-style combat tuck: when the owner readies a weapon or other players /
        // hostiles are close, the pet automatically glides to its perch behind the owner's
        // shoulder so it never blocks the crosshair - and hops back down once things calm.
        if (this.usesGoalMovement() && this.level().isClientSide() && this.isAlive() && !this.isPassenger()) {
            // Sprint tracking lives here (not in the follow goal) so it survives the goal
            // stopping/restarting - otherwise the run-alongside timer kept resetting.
            LivingEntity sprintOwner = this.getOwner();
            this.ownerSprintTicks = (sprintOwner != null && sprintOwner.isSprinting())
                    ? this.ownerSprintTicks + 1 : 0;
            if (this.ownerSprintTicks == 21) {
                io.github.aaronateataco.petsandpals.PetsInitializer.LOGGER.info(
                        "[Pets&Pals] sprint threshold reached (firstPerson={})",
                        firstPersonView.getAsBoolean());
            }
            if (this.tickCount % 10 == 0 && this.getOwner() instanceof Player ownerPlayer
                    && this.ownerInCombat(ownerPlayer)) {
                this.combatPerchTimer = 70;
            }
            boolean shouldPerch = this.combatPerchTimer > 0;
            if (this.combatPerchTimer > 0) this.combatPerchTimer--;
            if (shouldPerch != this.perched) {
                this.setPerched(shouldPerch);
            }

            // Centralized never-lose-your-pet system. Lives here (not in the follow goal)
            // so goal stops/restarts can't wipe its tracking. Golden rule: the pet NEVER
            // repositions while the player is facing its location - even occluded, if it's
            // in your view field it stays put and keeps walking. The moment you look away,
            // a lost/stuck/far pet quietly moves to a spot inside your view instead.
            if (!this.perched) {
                LivingEntity trackedOwner = this.getOwner();
                if (trackedOwner != null && trackedOwner.level() == this.level()) {
                    double distSqr = this.distanceToSqr(trackedOwner);
                    if (this.ownerInViewCone()) {
                        this.outOfViewTicks = 0;
                        this.stuckScore = 0;
                    } else {
                        double farRing = this.stopDistance() + 2.0;
                        if (distSqr > farRing * farRing) this.outOfViewTicks++;
                        else this.outOfViewTicks = 0;
                        if (this.tickCount % 20 == 0) {
                            if (distSqr > 64.0 && distSqr > this.lastTrackedDistanceSqr - 4.0) this.stuckScore++;
                            else this.stuckScore = 0;
                            this.lastTrackedDistanceSqr = distSqr;
                        }
                        if (distSqr > 24.0 * 24.0 || this.outOfViewTicks >= 50 || this.stuckScore >= 3) {
                            this.repositionToOwner();
                            this.outOfViewTicks = 0;
                            this.stuckScore = 0;
                        }
                    }
                }
            }
        }

        // Vanilla runs the whole mob AI (goals, navigation, move/look/jump controls) in
        // serverAiStep, which is hard-gated behind !level().isClientSide() - so for these
        // client-only pets we drive the same machinery ourselves. Runs before super.tick()
        // so travel() consumes the freshly computed movement inputs this same tick.
        if (this.usesGoalMovement() && this.level().isClientSide() && this.isAlive()
                && !this.isPassenger() && !this.perched) {
            this.getSensing().tick();
            this.goalSelector.tick();
            this.getNavigation().tick();
            this.getMoveControl().tick();
            this.getLookControl().tick();
            this.getJumpControl().tick();
        }

        super.tick();
        if (!this.usesGoalMovement()) return;

        if (this.perched) {
            this.tickPerched();
        }

        float volume = (float) soundVolume.getAsDouble();
        if (volume > 0.0f && this.random.nextInt(60 * 20) == 0 && this.getAmbientSound() != null) {
            this.level().playLocalSound(this, this.getAmbientSound(), SoundSource.NEUTRAL, volume, 1.0f);
        }
    }

    /**
     * Whether the pet is in "perched" mode (toggled by shift + right-click): instead of
     * being rigidly mounted on the player, it floats just behind and above the owner's
     * shoulder and glides after them with a slight lag - backbling-style, so it reads as
     * a companion hovering along rather than an accessory bolted to the head.
     */
    public boolean isPerched() {
        return this.perched;
    }

    public void setPerched(boolean perched) {
        this.perched = perched;
        this.noPhysics = perched;
        this.setNoGravity(perched);
        this.getNavigation().stop();
        if (!perched) {
            this.setDeltaMovement(Vec3.ZERO);
        }
    }

    /**
     * How far above the owner's feet this pet aims while following. Zero for ground pets;
     * flying pets hover near the owner's head instead of hugging the ground.
     */
    public float followYOffset() {
        return 0.0F;
    }

    /** How many consecutive ticks the owner has been sprinting. */
    public int ownerSprintTicks() {
        return this.ownerSprintTicks;
    }

    /**
     * Whether the owner is facing the pet's location - a generous 75-degree half-angle
     * horizontal cone around the head yaw, deliberately ignoring line of sight: if the
     * player is looking toward the pet (even through a wall or leaves), it must never
     * blip; they might be able to see the spot.
     */
    public boolean ownerInViewCone() {
        LivingEntity owner = this.getOwner();
        if (owner == null) return false;
        Vec3 view = Vec3.directionFromRotation(0.0F, owner.getYHeadRot());
        Vec3 toPet = this.position().subtract(owner.getEyePosition());
        Vec3 flat = new Vec3(toPet.x, 0.0, toPet.z);
        if (flat.lengthSqr() < 1.0e-4) return true;
        return new Vec3(view.x, 0.0, view.z).normalize().dot(flat.normalize()) > 0.2588;
    }

    /** Repositions to an exact spot if the pet fits there, with transition effects. */
    public boolean tryRepositionTo(double x, double y, double z) {
        if (!this.fitsAt(this.level(), x, y, z)) return false;
        this.transitionEffects();
        this.getNavigation().stop();
        this.finishReposition(x, y, z);
        return true;
    }

    /**
     * Repositions the pet to a safe spot inside the owner's field of view, with a small
     * particle+chime effect at both ends so it reads as intentional, never a raw blip.
     * Used whenever the pet genuinely can't follow (stuck, far behind, out of sight).
     */
    public void repositionToOwner() {
        LivingEntity owner = this.getOwner();
        if (owner == null || !this.level().isClientSide() || owner.level() != this.level()) {
            return;
        }
        this.transitionEffects();
        this.getNavigation().stop();

        Level level = this.level();
        for (int attempt = 0; attempt < 12; attempt++) {
            float yaw = owner.getYHeadRot() + (this.random.nextFloat() * 80.0F - 40.0F);
            Vec3 direction = Vec3.directionFromRotation(0.0F, yaw);
            double distance = 2.5 + this.random.nextDouble() * 2.0;
            double x = owner.getX() + direction.x * distance;
            double z = owner.getZ() + direction.z * distance;

            if (this.followYOffset() > 0.0F) {
                double y = owner.getY() + this.followYOffset();
                if (this.fitsAt(level, x, y, z)) {
                    this.finishReposition(x, y, z);
                    return;
                }
            } else {
                for (int dy = 2; dy >= -3; dy--) {
                    net.minecraft.core.BlockPos pos = net.minecraft.core.BlockPos.containing(x, owner.getY() + dy, z);
                    net.minecraft.core.BlockPos below = pos.below();
                    if (!level.getBlockState(below).isFaceSturdy(level, below, net.minecraft.core.Direction.UP)) {
                        continue;
                    }
                    if (this.fitsAt(level, x, pos.getY(), z)) {
                        this.finishReposition(x, pos.getY(), z);
                        return;
                    }
                }
            }
        }
        this.tryToTeleportToOwner();
        this.transitionEffects();
    }

    private void finishReposition(double x, double y, double z) {
        this.snapTo(x, y, z, this.getYRot(), this.getXRot());
        this.transitionEffects();
    }

    private void transitionEffects() {
        for (int i = 0; i < 6; i++) {
            this.level().addParticle(
                    net.minecraft.core.particles.ParticleTypes.END_ROD,
                    this.getRandomX(0.7), this.getRandomY(), this.getRandomZ(0.7),
                    0.0, 0.02, 0.0
            );
        }
        float volume = (float) soundVolume.getAsDouble();
        if (volume > 0.0f) {
            this.level().playLocalSound(this, net.minecraft.sounds.SoundEvents.AMETHYST_BLOCK_CHIME,
                    SoundSource.NEUTRAL, volume * 0.7f, 1.4f);
        }
    }

    /** Whether the pet's hitbox fits (no collisions) at the given position. */
    public boolean canFitAt(double x, double y, double z) {
        return this.fitsAt(this.level(), x, y, z);
    }

    private boolean fitsAt(Level level, double x, double y, double z) {
        net.minecraft.world.phys.AABB box = this.getBoundingBox().move(
                x - this.getX(), y - this.getY(), z - this.getZ());
        return level.noCollision(this, box);
    }

    /**
     * Whether the owner looks like they're in (or near) a fight: holding a weapon, another
     * player within 16 blocks, or a hostile mob within 12.
     */
    private boolean ownerInCombat(Player owner) {
        // No combat tuck when the owner can't actually fight or be hurt - adventure-mode
        // lobbies (Hypixel etc.), creative, spectator, invulnerable. Otherwise the pet
        // would perch permanently the moment other players are around.
        if (owner.isSpectator() || owner.getAbilities().invulnerable || !owner.getAbilities().mayBuild) {
            return false;
        }
        ItemStack held = owner.getMainHandItem();
        Item item = held.getItem();
        if (held.is(ItemTags.SWORDS) || held.is(ItemTags.AXES)
                || item instanceof ProjectileWeaponItem
                || item instanceof TridentItem
                || item instanceof MaceItem) {
            return true;
        }
        for (Player other : this.level().players()) {
            if (other != owner && !other.isSpectator() && other.distanceToSqr(owner) < 16.0 * 16.0) {
                return true;
            }
        }
        return !this.level().getEntitiesOfClass(Monster.class, owner.getBoundingBox().inflate(12.0)).isEmpty();
    }

    /**
     * The "home" block this pet emerges from in the spawn animation (a bee nest for the
     * bee). Null (the default) skips the animation and spawns the pet normally.
     */
    public @Nullable BlockState spawnDwellingBlock() {
        return null;
    }

    private void tickPerched() {
        LivingEntity owner = this.getOwner();
        if (owner == null || !owner.isAlive() || owner.isRemoved()) {
            this.setPerched(false);
            return;
        }

        // Anchor just behind and above the owner's shoulder, following the BODY yaw (not the
        // head) so glancing around doesn't swing the pet, plus a gentle floating bob.
        float rad = owner.yBodyRot * ((float) Math.PI / 180.0F);
        Vec3 back = new Vec3(Mth.sin(rad), 0.0, -Mth.cos(rad));
        Vec3 right = new Vec3(-Mth.cos(rad), 0.0, -Mth.sin(rad));
        double bob = Math.sin(this.tickCount * 0.12) * 0.06;
        double up = owner.getBbHeight() + 0.35 + bob;
        double backDistance = 0.55 + this.getBbWidth() * 0.4;
        Vec3 anchor = owner.position()
                .add(back.scale(backDistance))
                .add(right.scale(0.3))
                .add(0.0, up, 0.0);

        Vec3 delta = anchor.subtract(this.position());
        double distance = delta.length();
        if (distance > 6.0) {
            // Owner teleported/respawned - snap instead of gliding across the world.
            this.snapTo(anchor.x, anchor.y, anchor.z, owner.yBodyRot, 0.0F);
        } else {
            // Distance-scaled lerp: trails lazily when close, hurries when the owner sprints.
            double k = Mth.clamp(0.10 + distance * 0.18, 0.10, 0.55);
            Vec3 next = this.position().add(delta.scale(k));
            this.setPos(next.x, next.y, next.z);
        }

        this.setDeltaMovement(Vec3.ZERO);
        this.fallDistance = 0;
        this.setYRot(owner.yBodyRot);
        this.yBodyRot = owner.yBodyRot;
        this.setYHeadRot(owner.yBodyRot);
        this.setXRot(0.0F);
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

        // Legacy pickup for the bespoke custom mobs (duck, racoon, ...). Goal-driven pets
        // perch automatically during combat instead (see ownerInCombat), so shift+click
        // does nothing special for them.
        if (this.isTame() && itemStack.isEmpty() && player.isShiftKeyDown() && !this.usesGoalMovement()) {
            if (!this.isPassenger()) {
                this.startRiding(player);
                this.lookAt(player, 1f, 1f);
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
