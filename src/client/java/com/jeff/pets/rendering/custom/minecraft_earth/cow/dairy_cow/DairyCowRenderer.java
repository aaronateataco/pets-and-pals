package com.jeff.pets.rendering.custom.minecraft_earth.cow.dairy_cow;

import com.jeff.pets.Utils;
import com.jeff.pets.rendering.aprilfools.mooncow.LegacyCowModel;
import com.jeff.pets.rendering.custom.minecraft_earth.cow.AbstractEarthCowRenderer;
import com.jeff.pets.rendering.custom.minecraft_earth.cow.CowUtils;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;

public class DairyCowRenderer extends AbstractEarthCowRenderer<LegacyCowModel> {

    public static final ModelLayerLocation DAIRY_COW_LOCATION = new ModelLayerLocation(Utils.withModNamespace("dairy_cow"), "main");
    private static final Identifier TEXTURE = Utils.withModNamespace("textures/entity/minecraft_earth/cow/dairy_cow.png");

    public DairyCowRenderer(EntityRendererProvider.Context context) {
        super(context, CowUtils.getInstance().createCowModel(context, DAIRY_COW_LOCATION), TEXTURE, DAIRY_COW_LOCATION);
    }
}
