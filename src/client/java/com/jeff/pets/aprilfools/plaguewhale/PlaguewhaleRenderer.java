package com.jeff.pets.aprilfools.plaguewhale;

import com.jeff.pets.aprilfools.PlaguewhaleSlab;
import com.jeff.pets.aprilfools.toxifin.ToxifinSlabModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.GuardianRenderState;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public class PlaguewhaleRenderer extends MobRenderer<@NotNull PlaguewhaleSlab, @NotNull GuardianRenderState, @NotNull ToxifinSlabModel> {

    public static final ModelLayerLocation PLAGUEWHALE_LOCATION = new ModelLayerLocation(Identifier.withDefaultNamespace("plaguewhale_slab"), "main");

    public PlaguewhaleRenderer(EntityRendererProvider.Context context) {
        super(context, new ToxifinSlabModel(context.bakeLayer(PLAGUEWHALE_LOCATION)), 0.75f);
        this.scale(new GuardianRenderState(), new PoseStack());
    }

    @Override
    protected void scale(GuardianRenderState state, PoseStack poseStack) {
        poseStack.scale(2.35f, 2.35f, 2.35f);
    }

    @Override
    public @NotNull Identifier getTextureLocation(GuardianRenderState livingEntityRenderState) {
        return Identifier.withDefaultNamespace("textures/entity/plaguewhale.png");
    }

    @Override
    public GuardianRenderState createRenderState() {
        return new GuardianRenderState();
    }

    @Override
    public void extractRenderState(PlaguewhaleSlab slab, GuardianRenderState state, float f) {
        super.extractRenderState(slab, state, f);
        state.isUpsideDown = slab.getPlainTextName().equals("Grumm") || slab.getPlainTextName().equals("Dinnerbone");
    }
}
