package com.jeff.pets.rendering.custom.minecraft_earth.pig.pale_pig;

import com.jeff.pets.rendering.custom.minecraft_earth.pig.AbstractEarthPigRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;

import static com.jeff.pets.PetsInitializer.MOD_ID;

public class PalePigRenderer extends AbstractEarthPigRenderer {

    public static final ModelLayerLocation PALE_PIG_LOCATION = new ModelLayerLocation(Identifier.fromNamespaceAndPath(MOD_ID, "pale_pig"), "main");
    private static final Identifier texture = Identifier.fromNamespaceAndPath(MOD_ID, "textures/entity/minecraft_earth/pig/pale_pig.png");

    public PalePigRenderer(EntityRendererProvider.Context context) {
        super(context, PALE_PIG_LOCATION, texture);
    }
}
