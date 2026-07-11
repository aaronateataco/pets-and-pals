package io.github.aaronateataco.petsandpals;

import io.github.aaronateataco.petsandpals.mob.AbstractPet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import io.github.aaronateataco.petsandpals.mob.PetDwelling;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static io.github.aaronateataco.petsandpals.Central.CONFIG;
import static io.github.aaronateataco.petsandpals.PetsInitializer.MOD_ID;

/**
 * A utility class used mainly in {@link Central} and misc rendering classes. Contains various
 * shortcuts and utilities for spawning, despawning, and avoiding {@code NullPointerExceptions},
 * as well as a shortcut to {@link Identifier#fromNamespaceAndPath}.
 *
 * @author downloadableduck
 * @see io.github.aaronateataco.petsandpals.Central
 * @since 0.8.0 (Minecraft Earth Mob Pack)
 */
public class Utils {

    /**
     * Used to summon a pet.
     *
     * @param entity     the entity to be summoned.
     * @param entityName the name to set the custom entity to. (Technically not required since
     *                   we use a method to refresh the names in {@link Central}, but still.
     * @return If the {@code entity}, {@code player}, or {@code world} is {@code null}
     */
    public static void summonPet(AbstractPet entity, String entityName) {

        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        ClientLevel world = minecraft.level;

        if (entity == null || world == null || player == null) return;

        Vec3 lookAngle = player.getLookAngle();

        double x = player.getX() - lookAngle.x * (double) 0.5F;
        double y = player.getY() + (double) 0.5F;
        double z = player.getZ() - lookAngle.z * (double) 0.5F;

        entity.setCustomName(Component.literal(entityName));

        // Pets with a "dwelling" block (e.g. bee -> bee nest) get the spawn animation:
        // a grid-aligned ghost block rises in front of the camera, the pet emerges from
        // it, and the block sinks back into the ground. Falls back to a plain spawn if
        // no clean grid spot exists (mid-air, tight spaces, ...).
        BlockState dwellingBlock = entity.spawnDwellingBlock();
        BlockPos frontPos = findDwellingPos(player, world);
        if (dwellingBlock != null && frontPos != null) {
            entity.setPos(frontPos.getX() + 0.5, frontPos.getY(), frontPos.getZ() + 0.5);
            entity.setInvisible(true);
            world.addEntity(entity);
            world.addEntity(PetDwelling.create(world, frontPos, dwellingBlock, entity));
        } else if (frontPos != null) {
            // Every pet spawns in front of the camera, not underfoot/behind.
            entity.setPos(frontPos.getX() + 0.5, frontPos.getY(), frontPos.getZ() + 0.5);
            world.addEntity(entity);
        } else {
            entity.setPos(x, y, z);
            world.addEntity(entity);
        }
        entity.tame(player);
        Central.summonedEntity.add(entity);
    }

    /**
     * Finds a grid-aligned spot ~3.5 blocks ahead of where the player is looking with
     * solid ground below and room for a block, for the spawn-animation dwelling.
     */
    private static BlockPos findDwellingPos(Player player, ClientLevel world) {
        Vec3 look = player.getLookAngle();
        Vec3 flat = new Vec3(look.x, 0.0, look.z);
        if (flat.lengthSqr() < 1.0e-4) {
            flat = Vec3.directionFromRotation(0.0F, player.getYRot());
        }
        flat = flat.normalize();
        double reach = 3.5;
        BlockPos base = BlockPos.containing(
                player.getX() + flat.x * reach,
                player.getY() + 0.5,
                player.getZ() + flat.z * reach);
        for (int dy : new int[]{0, -1, 1, -2, 2, -3}) {
            BlockPos pos = base.above(dy);
            BlockPos below = pos.below();
            if (!world.getBlockState(below).isFaceSturdy(world, below, Direction.UP)) continue;
            if (!world.getFluidState(pos).isEmpty()) continue; // never underwater
            if (!world.getBlockState(pos).getCollisionShape(world, pos).isEmpty()) continue;
            if (!world.getBlockState(pos.above()).getCollisionShape(world, pos.above()).isEmpty()) continue;
            return pos;
        }
        return null;
    }

    /**
     * The method used in
     * {@link Central#refreshPetNames()}. Checks whether the entities' name is equal to the name
     * in the config, and assigns it the correct name if not.. Additionally, this method provides a layer of safety that ensures that Minecraft will not
     * throw a {@code NullPointerException} if {@code entity} is {@code null}.
     *
     * @param activePet The {@code activePet} value that matches {@code entity}
     * @param entity    The pet of which the name is checked.
     * @param petName   The {@code CONFIG.x} name that matches {@code entity} that the entities'
     *                  current name is checked off of. */
    public static void checkName(String activePet, AbstractPet entity, String petName) {
        if (Objects.equals(CONFIG.activePet, activePet) && entity != null && !entity.getPlainTextName().equals(petName)) {
            entity.setName(petName);
        }
    }

    /**
     * Used to teleport a pet if the pet is valid and not {@code null}.
     *
     * @param activePet The {@code String} that represents a possible active {@code pet}.
     * @param entity    The entity to teleport if {@code activePet} matches the currently active pet.
     * @return True if the {@code activePet} inputted is the current active pet, and the
     * {@code entity} is not {@code null}.
     */
    public static boolean checkTeleport(String activePet, AbstractPet entity) {
        return Objects.equals(CONFIG.activePet, activePet) && entity != null;
    }

    /**
     * Checks if the string is {@code null} and sets it to an empty - but not {@code null} String - if it is.
     *
     * @param s The String to check if it is {@code null}.
     * @return If the string is {@code null}, the new empty String is returned. If the string is not {@code null},
     * simply returns {@code s}.
     */
    public static String checkNullString(String s) {
        return s == null ? "" : s;
    }

    /**
     * Similar to the checkNullString method above, but takes a default value instead of
     * setting the String to an empty String. Used to set the {@code CONFIG.xSkin} values
     * to a default value and avoid {@code NullPointerExceptions}.
     *
     * @param s           The string to check if it {@code null}.
     * @param defaultSkin The default value to set {@code s} to if {@code s} is {@code null}.
     * @return If the string is {@code null}, {@code defaultSkin} is returned. If the string is not {@code null},
     * simply returns {@code s}.
     */
    public static String checkNullString(String s, String defaultSkin) {
        return s == null ? defaultSkin : s;
    }

    /**
     * Used as a shortcut to {@link Identifier#fromNamespaceAndPath}, and sets the parameter
     * {@code namespace} with {@link PetsInitializer#MOD_ID}.
     *
     * @param path The String that goes in the {@code path} parameter.
     * @return {@link Identifier#fromNamespaceAndPath}, with the parameter {@code namespace} set to
     * {@link PetsInitializer#MOD_ID} and the parameter {@code path} set to the user's input
     */
    public static Identifier withModNamespace(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    /**
     * Despawns the inputted pet if {@code e} is not {@code null}.
     *
     * @param e The entity to despawn
     */
    public static void despawnEntity(Entity e) {
        if (e != null) {
            e.discard();
        }
    }

    /**
     * Sets the active pet in {@link PetsConfig#activePet} as well as in {@link Central#summonedEntity}
     *
     * @param e The entity to be summoned and added to {@link Central#summonedEntity}
     * @param s The value to set {@link PetsConfig#activePet} to that matches {@code e}
     */
    public static void setActivePet(Entity e, String s) {
        Central.summonedEntity.clear();
        Central.summonedEntity.add(e);
        CONFIG.activePet = s;
    }

    public static ModelLayerLocation createModelLayer(String string) {
        return new ModelLayerLocation(withModNamespace(string), "main");
    }

    public static Block getBlockFromString(String string) {
        try {
            Field[] fields = Blocks.class.getDeclaredFields();

            for (Field field : fields) {
                if (!Block.class.isAssignableFrom(field.getType())) continue;
                if (Objects.equals(string, field.getName().toLowerCase())) {
                    return (Block) field.get(null);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Blocks.AIR;
    }

    public static List<String> getAllBlocks() {
        ArrayList<String> list = new ArrayList<>();
        try {
            Field[] fields = Blocks.class.getDeclaredFields();

            for (Field field : fields) {
                if (!Block.class.isAssignableFrom(field.getType())) continue;
                list.add(field.getName().replace("_", " ").toLowerCase());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
