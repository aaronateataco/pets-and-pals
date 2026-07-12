package io.github.aaronateataco.petsandpals.mob.vanilla.passive;

import io.github.aaronateataco.petsandpals.mob.SlimeLikePet;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.Supplier;

/**
 * Backported pet: sulfur cube is a real vanilla mob on 26.2+ but doesn't exist in this
 * version's game, so it's rendered with the mod's own model/texture instead of reusing
 * vanilla assets (see the client-side ClientSulfurCubeRenderer).
 * <p>
 * The real mob's headline feature is absorbing blocks to change its physical properties
 * (speed, bounciness). That's not something a pet should do unsupervised (and "explosive"/
 * "hot" would just hurt the owner), so the 10 pet-safe archetypes are exposed as a jump/
 * speed flavor instead, picked with {@code /petskin <archetype>}.
 */
public class ClientSulfurCube extends SlimeLikePet {

    private static final Identifier SPEED_ID = Identifier.fromNamespaceAndPath("pets-and-pals", "sulfur_cube_archetype_speed");
    private static final Identifier JUMP_ID = Identifier.fromNamespaceAndPath("pets-and-pals", "sulfur_cube_archetype_jump");

    // speedMultiplier, jumpMultiplier - real archetypes, minus explosive/hot
    private static final Map<String, float[]> ARCHETYPES = Map.of(
            "regular", new float[]{1.0f, 1.0f},
            "bouncy", new float[]{1.0f, 1.6f},
            "fast_flat", new float[]{1.4f, 0.7f},
            "fast_sliding", new float[]{1.6f, 0.5f},
            "high_resistance", new float[]{0.6f, 0.4f},
            "light", new float[]{1.1f, 1.3f},
            "slow_bouncy", new float[]{0.7f, 1.7f},
            "slow_flat", new float[]{0.6f, 0.6f},
            "slow_sliding", new float[]{0.75f, 0.5f},
            "sticky", new float[]{0.5f, 0.3f}
    );

    /** Set from Central.onInitializeClient(), same pattern as speedMultiplier/soundVolume. */
    public static Supplier<String> archetype = () -> "regular";

    private String appliedArchetype;

    public ClientSulfurCube(EntityType<? extends @NotNull TamableAnimal> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public void tick() {
        super.tick();
        String current = archetype.get();
        if (!current.equals(this.appliedArchetype)) {
            this.appliedArchetype = current;
            this.applyArchetype(current);
        }
    }

    private void applyArchetype(String name) {
        float[] mult = ARCHETYPES.getOrDefault(name, ARCHETYPES.get("regular"));
        this.applyModifier(Attributes.MOVEMENT_SPEED, SPEED_ID, mult[0]);
        this.applyModifier(Attributes.JUMP_STRENGTH, JUMP_ID, mult[1]);
    }

    private void applyModifier(net.minecraft.core.Holder<Attribute> attribute, Identifier id, float multiplier) {
        AttributeInstance instance = this.getAttribute(attribute);
        if (instance == null) return;
        instance.removeModifier(id);
        if (multiplier != 1.0f) {
            instance.addOrUpdateTransientModifier(new AttributeModifier(
                    id, multiplier - 1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
    }

    @Override
    protected int stopDistance() {
        return 2;
    }

    @Override
    protected float heartHeight() {
        return 0.5f;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        // no vanilla SULFUR_CUBE_BOUNCE sound on this version; closest slime-family stand-in
        return SoundEvents.SLIME_SQUISH;
    }
}
