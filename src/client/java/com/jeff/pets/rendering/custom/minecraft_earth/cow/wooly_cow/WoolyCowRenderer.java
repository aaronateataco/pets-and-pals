package com.jeff.pets.rendering.custom.minecraft_earth.cow.wooly_cow;

import com.jeff.pets.Utils;
import com.jeff.pets.rendering.aprilfools.mooncow.LegacyCowModel;
import com.jeff.pets.rendering.custom.minecraft_earth.cow.AbstractEarthCowRenderer;
import com.jeff.pets.rendering.custom.minecraft_earth.cow.CowUtils;
import com.jeff.pets.rendering.custom.minecraft_earth.cow.CustomCowModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;

public class WoolyCowRenderer extends AbstractEarthCowRenderer<LegacyCowModel> {

    public static final ModelLayerLocation WOOLY_COW_LOCATION = new ModelLayerLocation(Utils.withModNamespace("wooly_cow"), "main");
    private static final Identifier TEXTURE = Utils.withModNamespace("textures/entity/minecraft_earth/cow/wooly_cow.png");

    public WoolyCowRenderer(EntityRendererProvider.Context context) {
        super(context, CowUtils.getInstance().createCowModel(context, WOOLY_COW_LOCATION), TEXTURE, WOOLY_COW_LOCATION);
    }
}
