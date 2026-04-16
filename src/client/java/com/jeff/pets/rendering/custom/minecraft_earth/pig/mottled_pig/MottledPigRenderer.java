package com.jeff.pets.rendering.custom.minecraft_earth.pig.mottled_pig;

import com.jeff.pets.Utils;
import com.jeff.pets.rendering.PetRenderer;
import com.jeff.pets.rendering.custom.minecraft_earth.pig.AbstractEarthPigRenderer;
import com.jeff.pets.rendering.custom.minecraft_earth.pig.EarthPigModel;
import com.jeff.pets.rendering.vanilla.pig.ClientPigModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.PigRenderState;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import static com.jeff.pets.PetsInitializer.MOD_ID;

public class MottledPigRenderer extends AbstractEarthPigRenderer {

    public static final ModelLayerLocation MOTTLED_PIG_LOCATION = new ModelLayerLocation(Identifier.fromNamespaceAndPath(MOD_ID, "mottled_pig"), "main");

    private static final Identifier TEXTURE = Utils.withModNamespace("textures/entity/minecraft_earth/pig/mottled_pig.png");
    public MottledPigRenderer(EntityRendererProvider.Context context) {
        super(context, MOTTLED_PIG_LOCATION, TEXTURE);
    }
}
