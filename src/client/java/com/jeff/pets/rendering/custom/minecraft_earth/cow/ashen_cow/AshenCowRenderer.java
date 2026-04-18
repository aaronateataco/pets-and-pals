package com.jeff.pets.rendering.custom.minecraft_earth.cow.ashen_cow;

import com.jeff.pets.Utils;
import com.jeff.pets.rendering.custom.minecraft_earth.cow.AbstractEarthCowRenderer;
import com.jeff.pets.rendering.custom.minecraft_earth.cow.CowUtils;
import com.jeff.pets.rendering.custom.minecraft_earth.cow.CustomCowModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;

public class AshenCowRenderer extends AbstractEarthCowRenderer<CustomCowModel> {

    public static final ModelLayerLocation ASHEN_COW_LOCATION = new ModelLayerLocation(Utils.withModNamespace("ashen_cow"), "main");
    private static final Identifier TEXTURE = Utils.withModNamespace("textures/entity/minecraft_earth/cow/ashen_cow.png");

    public AshenCowRenderer(EntityRendererProvider.Context context) {
        super(context, CowUtils.getInstance().createCustomCowModel(context, ASHEN_COW_LOCATION), TEXTURE, ASHEN_COW_LOCATION);
    }
}
