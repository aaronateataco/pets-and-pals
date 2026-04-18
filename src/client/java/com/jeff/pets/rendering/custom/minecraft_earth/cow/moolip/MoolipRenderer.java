package com.jeff.pets.rendering.custom.minecraft_earth.cow.moolip;

import com.jeff.pets.PetsInitializer;
import com.jeff.pets.Utils;
import com.jeff.pets.mob.custom.minecraft_earth.cow.MinecraftEarthCow;
import com.jeff.pets.rendering.aprilfools.mooncow.LegacyCowModel;
import com.jeff.pets.rendering.aprilfools.mooncow.MoonCowRenderState;
import com.jeff.pets.rendering.custom.minecraft_earth.cow.AbstractEarthCowRenderer;
import com.jeff.pets.rendering.custom.minecraft_earth.cow.CowUtils;
import com.jeff.pets.rendering.custom.minecraft_earth.cow.CustomFlowerLayer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Blocks;

public class MoolipRenderer extends AbstractEarthCowRenderer<LegacyCowModel> {

    public static final ModelLayerLocation MOOLIP_LOCATION = new ModelLayerLocation(Utils.withModNamespace("moolip"), "main");
    private static final Identifier TEXTURE = Utils.withModNamespace("textures/entity/minecraft_earth/cow/moolip.png");
    private final BlockModelResolver resolver;

    public MoolipRenderer(EntityRendererProvider.Context context) {
        super(context, CowUtils.getInstance().createCowModel(context, MOOLIP_LOCATION), TEXTURE, MOOLIP_LOCATION);
        this.resolver = context.getBlockModelResolver();
        this.addLayer(new CustomFlowerLayer(this));
    }
    @Override
    public void extractRenderState(MinecraftEarthCow cow, MoonCowRenderState state, float f) {
        super.extractRenderState(cow, state, f);
        this.resolver.update(state.flower, PetsInitializer.PINK_DAISY.defaultBlockState(), BlockDisplayContext.create());
    }
}
