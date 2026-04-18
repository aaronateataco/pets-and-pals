package com.jeff.pets.rendering.custom.minecraft_earth.cow.pinto_cow;

import com.jeff.pets.Utils;
import com.jeff.pets.rendering.custom.minecraft_earth.cow.AbstractEarthCowRenderer;
import com.jeff.pets.rendering.custom.minecraft_earth.cow.CowUtils;
import com.jeff.pets.rendering.custom.minecraft_earth.cow.CustomCowModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.resources.Identifier;

public class PintoCowRenderer extends AbstractEarthCowRenderer<CustomCowModel> {

    public static final ModelLayerLocation PINTO_COW_LOCATION = new ModelLayerLocation(Utils.withModNamespace("pinto_cow"), "main");
    private static final Identifier TEXTURE = Utils.withModNamespace("textures/entity/minecraft_earth/cow/pinto_cow.png");

    public PintoCowRenderer(EntityRendererProvider.Context context) {
        super(context, CowUtils.getInstance().createCustomCowModel(context, PINTO_COW_LOCATION), TEXTURE, PINTO_COW_LOCATION);
    }
}
