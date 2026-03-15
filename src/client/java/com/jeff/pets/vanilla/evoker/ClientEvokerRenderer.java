package com.jeff.pets.vanilla.evoker;

import com.jeff.pets.vanilla.hostile.ClientEvoker;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.EvokerRenderState;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

public class ClientEvokerRenderer extends MobRenderer<@NotNull ClientEvoker, @NotNull EvokerRenderState, @NotNull ClientEvokerModel> {
    public static final ModelLayerLocation EVOKER_LOCATION = new ModelLayerLocation(Identifier.withDefaultNamespace("clientevoker"), "main");

    public ClientEvokerRenderer(EntityRendererProvider.Context context) {
        super(context, new ClientEvokerModel(context.bakeLayer(ModelLayers.EVOKER)), 0.75f);
    }

    @Override
    public @NotNull Identifier getTextureLocation(EvokerRenderState livingEntityRenderState) {
        return Identifier.withDefaultNamespace("textures/entity/illager/evoker.png");
    }

    @Override
    public EvokerRenderState createRenderState() {
        return new EvokerRenderState();
    }

    @Override
    public void extractRenderState(ClientEvoker evoker, EvokerRenderState state, float f) {
        super.extractRenderState(evoker, state, f);
        state.isUpsideDown = evoker.getPlainTextName().equals("Grumm") || evoker.getPlainTextName().equals("Dinnerbone");
    }
}
