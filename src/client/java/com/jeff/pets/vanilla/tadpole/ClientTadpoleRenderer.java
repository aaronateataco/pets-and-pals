package com.jeff.pets.vanilla.tadpole;

import com.jeff.pets.vanilla.passive.ClientTadpole;
import net.minecraft.client.model.animal.frog.TadpoleModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public class ClientTadpoleRenderer extends MobRenderer<@NotNull ClientTadpole, @NotNull LivingEntityRenderState, @NotNull TadpoleModel> {
    public static final ModelLayerLocation TADPOLE_LOCATION = new ModelLayerLocation(Identifier.withDefaultNamespace("clienttadpole"), "main");

    public ClientTadpoleRenderer(EntityRendererProvider.Context context) {
        super(context, new TadpoleModel(context.bakeLayer(ModelLayers.TADPOLE)), 0.75F);
    }

    @Override
    public @NotNull Identifier getTextureLocation(LivingEntityRenderState livingEntityRenderState) {
        return Identifier.withDefaultNamespace("textures/entity/tadpole/tadpole.png");
    }

    @Override
    public LivingEntityRenderState createRenderState() {
        return new LivingEntityRenderState();
    }

    @Override
    public void extractRenderState(ClientTadpole tadpole, LivingEntityRenderState state, float f) {
        super.extractRenderState(tadpole, state, f);
        state.isUpsideDown = tadpole.getPlainTextName().equals("Grumm") || tadpole.getPlainTextName().equals("Dinnerbone");
    }
}
