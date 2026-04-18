package com.jeff.pets.rendering.custom.minecraft_earth.furnace_golem;

import com.jeff.pets.Utils;
import com.jeff.pets.mob.vanilla.neutral.ClientIronGolem;
import com.jeff.pets.rendering.PetRenderer;
import net.minecraft.client.model.animal.golem.IronGolemModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.IronGolemRenderState;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public class FurnaceGolemRenderer extends PetRenderer<ClientIronGolem, IronGolemRenderState, IronGolemModel> {

    public static final ModelLayerLocation FURNACE_GOLEM_LOCATION = new ModelLayerLocation(Utils.withModNamespace("furnace_golem"), "main");

    public FurnaceGolemRenderer(EntityRendererProvider.Context context) {
        super(context, new IronGolemModel(context.bakeLayer(ModelLayers.IRON_GOLEM)), 0.75f);
    }

    @Override
    public @NotNull Identifier getTextureLocation(@NotNull IronGolemRenderState state) {
        return Utils.withModNamespace("textures/entity/minecraft_earth/furnace_golem.png");
    }

    @Override
    public @NotNull IronGolemRenderState createRenderState() {
        return new IronGolemRenderState();
    }
}
