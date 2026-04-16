package com.jeff.pets;

import com.jeff.pets.mob.AbstractPet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;

import static com.jeff.pets.Central.CONFIG;
import static com.jeff.pets.PetsInitializer.MOD_ID;

public class Utils {
    protected static void summonPet(AbstractPet entity, String entityName) {

        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        ClientLevel world = minecraft.level;

        Vec3 lookAngle = player.getLookAngle();

        double x = player.getX() - lookAngle.x * (double) 0.5F;
        double y = player.getY() + (double) 0.5F;
        double z = player.getZ() - lookAngle.z * (double) 0.5F;

        entity.setPos(x, y, z);
        entity.setCustomName(Component.literal(entityName));
        world.addEntity(entity);
        entity.tame(player);
        Central.summonedEntity.add(entity);
    }

    protected static boolean checkName(String activePet, AbstractPet entity, String petName) {
        return Objects.equals(CONFIG.activePet, activePet) && entity != null && !entity.getPlainTextName().equals(petName);
    }

    protected static boolean checkTeleport(String activePet, AbstractPet entity) {
        return Objects.equals(CONFIG.activePet, activePet) && entity != null;
    }

    protected static String checkNullName(String s) {
        return s == null ? "" : s;
    }

    //the CONFIG.xSkin goes in the first param, and the default skin value goes in the second param
    protected static String checkNullSkin(String skin, String defaultSkin) {
        return skin == null ? defaultSkin : skin;
    }

    public static Identifier withModNamespace(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
    protected static void despawnEntity(Entity e) {
        if (e != null) {
            e.discard();
        }
    }
    protected static void setActivePet(Entity e, String s) {
        Central.summonedEntity.clear();
        Central.summonedEntity.add(e);
        CONFIG.activePet = s;
    }
}
