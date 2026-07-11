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

    /** "Pet Speed" config multiplier, wired up in Central (this class can't see the client config). */
    public static DoubleSupplier speedMultiplier = () -> 1.0;

    /** "Pet Volume" config value, 0 = muted. */
    public static DoubleSupplier soundVolume = () -> 1.0;

    /** True while in first person, wired up in Central. */
    public static java.util.function.BooleanSupplier firstPersonView = () -> true;

    /** Raft wood style index (see PetRaftBlock.WOODS), wired up in Central. */
    public static java.util.function.IntSupplier raftStyle = () -> 1;

    /** Adds an entity to the client level, wired up in Central. */
    public static java.util.function.Consumer<net.minecraft.world.entity.Entity> clientEntitySpawner = e -> {};

    /** @deprecated only the legacy custom mob tick logic uses this. */
    @Deprecated
    protected int waitingTime = 0;

    private boolean perched = false;
    private int combatPerchTimer = 0;
    private int ownerSprintTicks = 0;
    private int sprintGraceTicks = 0;
    private boolean orbMode = false;
    private boolean rafted = false;
    private int outOfViewTicks = 0;
    private int stuckScore = 0;
    private double lastTrackedDistanceSqr = 0.0;

    protected AbstractPet(EntityType<? extends @NotNull TamableAnimal> type, Level level) {
        super(type, level);
        this.setSpeed(0.5f);
        this.copyVanillaAttributes(type);
        // Mob only calls registerGoals() on ServerLevel, so do it here. Not via the
        // overridable registerGoals() - some custom mobs override that with server-only
        // goals (BreedGoal casts to ServerLevel) that crash on the client.
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

    /** Copies movement speeds from the same-named vanilla mob, if one exists. */
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

    /** True for goal/navigation-driven pets; custom mobs with their own tick logic return false. */
    protected boolean usesGoalMovement() {
        return false;
    }

    /** Client-side pets need this true or vanilla skips all their AI. */
    @Override
    public boolean isEffectiveAi() {
        return this.usesGoalMovement() || super.isEffectiveAi();
    }

    /** Lets travel() run for this client-only entity (no server packets are coming). */
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
        // combat tuck: perch behind the shoulder while the owner is fighting
        if (this.usesGoalMovement() && this.level().isClientSide() && this.isAlive() && !this.isPassenger()
                && !this.orbMode && !this.rafted) {
            // sprint tracking lives here so goal restarts can't reset it; one-tick sprint
            // flickers (wall bumps, entity contact) get a short grace instead of a reset
            LivingEntity sprintOwner = this.getOwner();
            if (sprintOwner != null && sprintOwner.isSprinting()) {
                this.ownerSprintTicks++;
                this.sprintGraceTicks = 8;
            } else if (this.sprintGraceTicks > 0) {
                this.sprintGraceTicks--;
            } else {
                this.ownerSprintTicks = 0;
            }
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

            // ferry: a land pet swimming after a distant owner gets a raft popped in
            if (!this.perched && !this.rafted && !this.orbMode && this.followYOffset() <= 0.0F
                    && this.isInWater() && this.getOwner() instanceof Player swimOwner
                    && swimOwner.level() == this.level() && this.distanceToSqr(swimOwner) > 16.0) {
                this.setRafted(true);
                this.transitionEffects();
                clientEntitySpawner.accept(PetRaft.createFerry(this.level(), this));
            }

            // pet raft: when the owner boards a boat, a raft floats alongside for the pet
            if (!this.perched && !this.rafted && !this.orbMode
                    && this.getOwner() instanceof Player boatOwner
                    && boatOwner.getVehicle() instanceof net.minecraft.world.entity.vehicle.boat.AbstractBoat boat
                    && boatOwner.level() == this.level()) {
                this.setRafted(true);
                clientEntitySpawner.accept(PetRaft.create(this.level(), this, boat));
            }

            // keep-the-pet-close tracking. Rule: never reposition while the player is
            // facing the pet's location, even if it's occluded.
            if (!this.perched) {
                LivingEntity trackedOwner = this.getOwner();
                if (trackedOwner != null && trackedOwner.level() == this.level()) {
                    // flyer ceiling: sink back down if too far above the owner
                    if (this.followYOffset() > 0.0F && this.getY() > trackedOwner.getY() + 3.5) {
                        this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.04, 0.0));
                    }
                    double distSqr = this.distanceToSqr(trackedOwner);
                    if (distSqr > 20.0 * 20.0) {
                        // hard leash - past this the pet is a speck and risks chunk unload
                        this.repositionToOwner();
                        this.outOfViewTicks = 0;
                        this.stuckScore = 0;
                    } else if (this.ownerInViewCone()) {
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
                        if (this.outOfViewTicks >= 40 || this.stuckScore >= 3) {
                            this.repositionToOwner();
                            this.outOfViewTicks = 0;
                            this.stuckScore = 0;
                        }
                    }
                }
            }
        }

        // serverAiStep is server-only in vanilla, so drive the AI ourselves.
        // Runs before super.tick() so travel() sees fresh inputs this tick.
        if (this.usesGoalMovement() && this.level().isClientSide() && this.isAlive()
                && !this.isPassenger() && !this.perched && !this.orbMode && !this.rafted) {
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

    /** Perched = floating behind the owner's shoulder during combat. */
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

    /** Follow height above the owner's feet; 0 for ground pets. */
    public float followYOffset() {
        return 0.0F;
    }

    /** How many consecutive ticks the owner has been sprinting. */
    public int ownerSprintTicks() {
        return this.ownerSprintTicks;
    }

    /** True while standing on the boat-side raft (see PetRaft). */
    public boolean isRafted() {
        return this.rafted;
    }

    public void setRafted(boolean rafted) {
        this.rafted = rafted;
        this.getNavigation().stop();
        if (!rafted) {
            this.setDeltaMovement(Vec3.ZERO);
        }
    }

    /** Ghost form: used when there's nowhere valid to reposition (see PetOrb). */
    public boolean isOrbMode() {
        return this.orbMode;
    }

    public void enterOrbMode() {
        if (this.orbMode || this.perched || !this.level().isClientSide() || this.isRemoved()) {
            return;
        }
        this.orbMode = true;
        this.setInvisible(true);
        this.noPhysics = true;
        this.setNoGravity(true);
        this.getNavigation().stop();
        this.setDeltaMovement(Vec3.ZERO);
        this.transitionEffects();
        clientEntitySpawner.accept(PetOrb.create(this.level(), this));
    }

    /** Called by the orb when it found a valid spot: become the pet again there. */
    public void exitOrbMode(Vec3 spot) {
        this.orbMode = false;
        this.setInvisible(false);
        this.noPhysics = false;
        this.setNoGravity(false);
        this.setDeltaMovement(Vec3.ZERO);
        this.snapTo(spot.x, spot.y, spot.z, this.getYRot(), 0.0F);
    }

    /** Is the owner facing the pet's location? (75 degree cone, ignores line of sight on purpose.) */
    public boolean ownerInViewCone() {
        LivingEntity owner = this.getOwner();
        if (owner == null) return false;
        Vec3 view = Vec3.directionFromRotation(0.0F, owner.getYHeadRot());
        Vec3 toPet = this.position().subtract(owner.getEyePosition());
        Vec3 flat = new Vec3(toPet.x, 0.0, toPet.z);
        if (flat.lengthSqr() < 1.0e-4) return true;
        return new Vec3(view.x, 0.0, view.z).normalize().dot(flat.normalize()) > 0.2588;
    }

    /** Reposition to an exact spot if the pet fits there. */
    public boolean tryRepositionTo(double x, double y, double z) {
        if (!this.fitsAt(this.level(), x, y, z)) return false;
        this.transitionEffects();
        this.getNavigation().stop();
        this.finishReposition(x, y, z);
        return true;
    }

    /** Move the pet to a safe spot inside the owner's view, with a small effect. */
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
                    if (!level.getFluidState(pos).isEmpty()) {
                        continue; // never underwater
                    }
                    if (this.fitsAt(level, x, pos.getY(), z)) {
                        this.finishReposition(x, pos.getY(), z);
                        return;
                    }
                }
            }
        }
        // nowhere to go - ghost form
        this.enterOrbMode();
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

    /** Weapon in hand, players within 16 blocks, or hostiles within 12. */
    private boolean ownerInCombat(Player owner) {
        // skip when the owner can't fight anyway (adventure lobbies, creative, spectator)
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

    /** Block this pet emerges from in the spawn animation; null = no animation. */
    public @Nullable BlockState spawnDwellingBlock() {
        String path = BuiltInRegistries.ENTITY_TYPE.getKey(this.getType()).getPath();
        net.minecraft.world.level.block.Block block = switch (path) {
            case "clientbee" -> net.minecraft.world.level.block.Blocks.BEE_NEST;
            case "clientfox" -> net.minecraft.world.level.block.Blocks.SWEET_BERRY_BUSH;
            case "clientchicken", "diamond_chicken" -> net.minecraft.world.level.block.Blocks.HAY_BLOCK;
            case "clientrabbit" -> net.minecraft.world.level.block.Blocks.GRASS_BLOCK;
            case "clientslime" -> net.minecraft.world.level.block.Blocks.SLIME_BLOCK;
            case "clientmagmacube", "clientblaze" -> net.minecraft.world.level.block.Blocks.MAGMA_BLOCK;
            case "clientzombie", "clienthusk", "clientdrowned", "clientzombievillager" ->
                    net.minecraft.world.level.block.Blocks.COARSE_DIRT;
            case "clientskeleton", "clientstray", "clientbogged", "clientparched",
                 "clientwitherskeleton", "clientwolf" -> net.minecraft.world.level.block.Blocks.BONE_BLOCK;
            case "clientspider", "clientcavespider" -> net.minecraft.world.level.block.Blocks.COBWEB;
            case "clientenderman", "clientendermite" -> net.minecraft.world.level.block.Blocks.END_STONE;
            case "clientallay", "clientvex" -> net.minecraft.world.level.block.Blocks.AMETHYST_BLOCK;
            case "clientcat" -> net.minecraft.world.level.block.Blocks.HAY_BLOCK;
            case "clientfrog", "clienttadpole" -> net.minecraft.world.level.block.Blocks.MUD;
            case "clientmooshroom" -> net.minecraft.world.level.block.Blocks.RED_MUSHROOM_BLOCK;
            case "clientaxolotl" -> net.minecraft.world.level.block.Blocks.CLAY;
            case "clientguardian", "clientelderguardian" -> net.minecraft.world.level.block.Blocks.PRISMARINE;
            case "clientsniffer" -> net.minecraft.world.level.block.Blocks.MOSS_BLOCK;
            case "clientturtle" -> net.minecraft.world.level.block.Blocks.SAND;
            default -> null;
        };
        return block == null ? null : block.defaultBlockState();
    }

    private void tickPerched() {
        LivingEntity owner = this.getOwner();
        if (owner == null || !owner.isAlive() || owner.isRemoved()) {
            this.setPerched(false);
            return;
        }

        // anchor behind the shoulder, tracking body yaw so head turns don't swing it
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
            // owner teleported, just snap
            this.snapTo(anchor.x, anchor.y, anchor.z, owner.yBodyRot, 0.0F);
        } else {
            // distance-scaled lerp so it trails lazily and catches up when needed
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

    /** No knockback - the server doesn't know this entity exists. */
    @Override
    public void knockback(double strength, double x, double z, net.minecraft.world.damagesource.DamageSource source, float power) {
    }

    @Override
    public void knockback(double strength, double x, double z, net.minecraft.world.damagesource.DamageSource source, float power, boolean flag) {
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

        // legacy shift+click pickup for the custom mobs; goal pets perch automatically
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

    /** @deprecated no-op, kept because the custom mobs still call it. */
    @Deprecated
    public void wander() {
    }
}
