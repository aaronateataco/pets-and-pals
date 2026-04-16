package com.jeff.pets.rendering.custom.minecraft_earth.pig.piebald_pig;

import com.jeff.pets.rendering.custom.minecraft_earth.pig.AbstractEarthPigRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;

import static com.jeff.pets.PetsInitializer.MOD_ID;

public class PiebaldPigRenderer extends AbstractEarthPigRenderer {

    public static final ModelLayerLocation PIEBALD_PIG_LOCATION = new ModelLayerLocation(Identifier.fromNamespaceAndPath(MOD_ID, "piebald_pig"), "main");
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(MOD_ID, "textures/entity/minecraft_earth/pig/piebald_pig.png");

    public PiebaldPigRenderer(EntityRendererProvider.Context context) {
        super(context, PIEBALD_PIG_LOCATION, TEXTURE);
    }
}
