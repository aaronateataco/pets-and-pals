package com.jeff.pets.rendering.custom.minecraft_earth.pig.spotted_pig;

import com.jeff.pets.rendering.custom.minecraft_earth.pig.AbstractEarthPigRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;

import static com.jeff.pets.PetsInitializer.MOD_ID;

public class SpottedPigRenderer extends AbstractEarthPigRenderer {

    public static final ModelLayerLocation SPOTTED_PIG_LOCATION = new ModelLayerLocation(Identifier.fromNamespaceAndPath(MOD_ID, "spotted_pig"), "main");
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(MOD_ID, "textures/entity/minecraft_earth/pig/spotted_pig.png");

    public SpottedPigRenderer(EntityRendererProvider.Context context) {
        super(context, SPOTTED_PIG_LOCATION, TEXTURE);
    }
}
