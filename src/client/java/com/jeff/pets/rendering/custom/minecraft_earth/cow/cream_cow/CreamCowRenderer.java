package com.jeff.pets.rendering.custom.minecraft_earth.cow.cream_cow;

import com.jeff.pets.Utils;
import com.jeff.pets.rendering.aprilfools.mooncow.LegacyCowModel;
import com.jeff.pets.rendering.custom.minecraft_earth.cow.AbstractEarthCowRenderer;
import com.jeff.pets.rendering.custom.minecraft_earth.cow.CowUtils;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;

public class CreamCowRenderer extends AbstractEarthCowRenderer<LegacyCowModel> {

    public static final ModelLayerLocation CREAM_COW_LOCATION = new ModelLayerLocation(Utils.withModNamespace("cream_cow"), "main");
    private static final Identifier TEXTURE = Utils.withModNamespace("textures/entity/minecraft_earth/cow/cream_cow.png");

    public CreamCowRenderer(EntityRendererProvider.Context context) {
        super(context, CowUtils.getInstance().createCowModel(context, CREAM_COW_LOCATION), TEXTURE, CREAM_COW_LOCATION);
    }
}
