package com.jeff.pets.vanilla.goat;

import com.jeff.pets.vanilla.neutral.ClientGoat;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.GoatRenderState;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public class ClientGoatRenderer extends MobRenderer<@NotNull ClientGoat, @NotNull GoatRenderState, @NotNull ClientGoatModel> {

    public static final ModelLayerLocation GOAT_LOCATION = new ModelLayerLocation(Identifier.withDefaultNamespace("clientgoat"), "main");

    public ClientGoatRenderer(EntityRendererProvider.Context context) {
        super(context, new ClientGoatModel(context.bakeLayer(ModelLayers.GOAT)), 0.75f);
    }

    @Override
    protected void scale(@NotNull GoatRenderState livingEntityRenderState, @NotNull PoseStack poseStack) {
        poseStack.scale(0.5F, 0.5F, 0.5F);
    }

    @Override
    public @NotNull Identifier getTextureLocation(GoatRenderState livingEntityRenderState) {
        return Identifier.withDefaultNamespace("textures/entity/goat/goat.png");
    }

    @Override
    public GoatRenderState createRenderState() {
        return new GoatRenderState();
    }

    @Override
    public void extractRenderState(ClientGoat goat, GoatRenderState state, float f) {
        super.extractRenderState(goat, state, f);
        state.isUpsideDown = goat.getPlainTextName().equals("Grumm") || goat.getPlainTextName().equals("Dinnerbone");
    }
}
