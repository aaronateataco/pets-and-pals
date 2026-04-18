package com.jeff.pets.rendering.custom.minecraft_earth.cow.albino_cow;

import com.jeff.pets.Utils;
import com.jeff.pets.rendering.custom.minecraft_earth.cow.AbstractEarthCowRenderer;
import com.jeff.pets.rendering.custom.minecraft_earth.cow.CowUtils;
import com.jeff.pets.rendering.custom.minecraft_earth.cow.CustomCowModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;

public class AlbinoCowRenderer extends AbstractEarthCowRenderer<CustomCowModel> {

    public static final ModelLayerLocation ALBINO_COW_LOCATION = new ModelLayerLocation(Utils.withModNamespace("albino_cow"), "main");
    private static final Identifier TEXTURE = Utils.withModNamespace("textures/entity/minecraft_earth/cow/albino_cow.png");

    public AlbinoCowRenderer(EntityRendererProvider.Context context) {
        super(context, CowUtils.getInstance().createCustomCowModel(context, ALBINO_COW_LOCATION), TEXTURE, ALBINO_COW_LOCATION);
    }
}
