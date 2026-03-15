package com.jeff.pets.aprilfools.lovegolem;

import com.jeff.pets.aprilfools.LoveGolem;
import net.minecraft.client.model.animal.golem.IronGolemModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.IronGolemRenderState;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public class LoveGolemRenderer extends MobRenderer<@NotNull LoveGolem, @NotNull IronGolemRenderState, @NotNull IronGolemModel> {

    public static final ModelLayerLocation LOVE_GOLEM_LOCATION = new ModelLayerLocation(Identifier.withDefaultNamespace("lovegolem"), "main");

    public LoveGolemRenderer(EntityRendererProvider.Context context) {
        super(context, new IronGolemModel(context.bakeLayer(LOVE_GOLEM_LOCATION)), 0.75f);
    }

    @Override
    public @NotNull Identifier getTextureLocation(IronGolemRenderState livingEntityRenderState) {
        return Identifier.withDefaultNamespace("textures/entity/irongolem/love_golem.png");
    }

    @Override
    public IronGolemRenderState createRenderState() {
        return new IronGolemRenderState();
    }

    @Override
    public void extractRenderState(LoveGolem loveGolem, IronGolemRenderState state, float f) {
        super.extractRenderState(loveGolem, state, f);
        state.isUpsideDown = loveGolem.getPlainTextName().equals("Grumm") || loveGolem.getPlainTextName().equals("Dinnerbone");
    }
}
