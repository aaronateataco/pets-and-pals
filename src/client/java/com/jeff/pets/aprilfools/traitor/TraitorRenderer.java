package com.jeff.pets.aprilfools.traitor;

import com.jeff.pets.aprilfools.Traitor;
import com.jeff.pets.vanilla.evoker.ClientEvokerModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.EvokerRenderState;
import net.minecraft.client.renderer.entity.state.IllagerRenderState;
import net.minecraft.client.renderer.entity.state.IllusionerRenderState;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public class TraitorRenderer extends MobRenderer<@NotNull Traitor, @NotNull EvokerRenderState, @NotNull ClientEvokerModel> {

    public static final ModelLayerLocation TRAITOR_LOCATION = new ModelLayerLocation(Identifier.withDefaultNamespace("traitor"), "main");

    public TraitorRenderer(EntityRendererProvider.Context context) {
        super(context, new ClientEvokerModel(context.bakeLayer(ModelLayers.PILLAGER)), 0.75f);
        this.addLayer(new TraitorBiomeLayer(this));
    }

    @Override
    public @NotNull Identifier getTextureLocation(EvokerRenderState livingEntityRenderState) {
        return Identifier.withDefaultNamespace("textures/entity/illager/pillager.png");
    }

    @Override
    public EvokerRenderState createRenderState() {
        return new EvokerRenderState();
    }
    @Override
    public void extractRenderState(Traitor traitor, EvokerRenderState state, float f) {
        super.extractRenderState(traitor, state, f);
        state.isUpsideDown = traitor.getPlainTextName().equals("Grumm") || traitor.getPlainTextName().equals("Dinnerbone");
    }
}
