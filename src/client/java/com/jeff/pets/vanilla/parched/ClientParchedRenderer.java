package com.jeff.pets.vanilla.parched;

import com.jeff.pets.vanilla.hostile.ClientParched;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.skeleton.SkeletonModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.SkeletonRenderState;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public class ClientParchedRenderer extends MobRenderer<@NotNull ClientParched, @NotNull SkeletonRenderState, @NotNull SkeletonModel<@NotNull SkeletonRenderState>> {

    public static final ModelLayerLocation PARCHED_LOCATION = new ModelLayerLocation(Identifier.withDefaultNamespace("clientparched"), "main");

    public ClientParchedRenderer(EntityRendererProvider.Context context) {
        super(context, new SkeletonModel<>(context.bakeLayer(ModelLayers.PARCHED)), 0.75f);
    }

    @Override
    public @NotNull Identifier getTextureLocation(SkeletonRenderState livingEntityRenderState) {
        return Identifier.withDefaultNamespace("textures/entity/skeleton/parched.png");
    }

    @Override
    public SkeletonRenderState createRenderState() {
        return new SkeletonRenderState();
    }

    @Override
    public void extractRenderState(ClientParched parched, SkeletonRenderState state, float f) {
        super.extractRenderState(parched, state, f);
        state.isUpsideDown = parched.getPlainTextName().equals("Grumm") || parched.getPlainTextName().equals("Dinnerbone");
    }
}
