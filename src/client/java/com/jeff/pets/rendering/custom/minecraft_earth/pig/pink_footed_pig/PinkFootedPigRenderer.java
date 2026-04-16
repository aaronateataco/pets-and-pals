package com.jeff.pets.rendering.custom.minecraft_earth.pig.pink_footed_pig;

import com.jeff.pets.rendering.custom.minecraft_earth.pig.AbstractEarthPigRenderer;
import com.sun.jna.platform.win32.WinBase;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;

import static com.jeff.pets.PetsInitializer.MOD_ID;

public class PinkFootedPigRenderer extends AbstractEarthPigRenderer {

    public static final ModelLayerLocation PINK_FOOTED_PIG_LOCATION = new ModelLayerLocation(Identifier.fromNamespaceAndPath(MOD_ID, "pink_footed_pig"), "main");
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(MOD_ID, "textures/entity/minecraft_earth/pig/pink_footed_pig.png");

    public PinkFootedPigRenderer(EntityRendererProvider.Context context) {
        super(context, PINK_FOOTED_PIG_LOCATION, TEXTURE);
    }
}
