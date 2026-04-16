package com.jeff.pets.rendering.custom.minecraft_earth.pig.sooty_pig;

import com.jeff.pets.rendering.custom.minecraft_earth.pig.AbstractEarthPigRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;

import static com.jeff.pets.PetsInitializer.MOD_ID;

public class SootyPigRenderer extends AbstractEarthPigRenderer {

    public static final ModelLayerLocation SOOTY_PIG_LOCATION = new ModelLayerLocation(Identifier.fromNamespaceAndPath(MOD_ID, "sooty_pig"), "main");
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(MOD_ID, "textures/entity/minecraft_earth/pig/sooty_pig.png");

    public SootyPigRenderer(EntityRendererProvider.Context context) {
        super(context, SOOTY_PIG_LOCATION, TEXTURE);
    }
}
