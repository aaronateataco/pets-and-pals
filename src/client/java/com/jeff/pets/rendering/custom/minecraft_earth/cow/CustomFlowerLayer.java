package com.jeff.pets.rendering.custom.minecraft_earth.cow;

import com.jeff.pets.rendering.aprilfools.mooncow.LegacyCowModel;
import com.jeff.pets.rendering.aprilfools.mooncow.MoonCowRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import org.jetbrains.annotations.NotNull;

public class CustomFlowerLayer extends RenderLayer<@NotNull MoonCowRenderState, @NotNull LegacyCowModel> {
    public CustomFlowerLayer(RenderLayerParent<@NotNull MoonCowRenderState, @NotNull LegacyCowModel> renderer) {
        super(renderer);
    }

    @Override
    public void submit(PoseStack poseStack, @NotNull SubmitNodeCollector submitNodeCollector, int lightCoords, MoonCowRenderState state, float yRot, float xRot) {
        int overlayCoords = LivingEntityRenderer.getOverlayCoords(state, 0.0F);
        poseStack.pushPose();
        poseStack.translate(0.2F, -0.35F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(-48.0F));
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.translate(-0.5F, -0.5F, -0.5F);
        this.submit(poseStack, submitNodeCollector, lightCoords, state.outlineColor, state.flower, overlayCoords);
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.translate(0.2F, -0.35F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(42.0F));
        poseStack.translate(0.1F, 0.0F, -0.6F);
        poseStack.mulPose(Axis.YP.rotationDegrees(-48.0F));
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.translate(-0.5F, -0.5F, -0.5F);
        this.submit(poseStack, submitNodeCollector, lightCoords, state.outlineColor, state.flower, overlayCoords);
        poseStack.popPose();
        poseStack.pushPose();
        this.getParentModel().root().getChild("head").translateAndRotate(poseStack);
        poseStack.translate(0.0F, -0.7F, -0.2F);
        poseStack.mulPose(Axis.YP.rotationDegrees(-78.0F));
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.translate(-0.5F, -0.5F, -0.5F);
        this.submit(poseStack, submitNodeCollector, lightCoords, state.outlineColor, state.flower, overlayCoords);
        poseStack.popPose();
    }

    private void submit(final PoseStack poseStack, final SubmitNodeCollector submitNodeCollector, final int lightCoords, final int outlineColor, final BlockModelRenderState mushroomModel, final int overlayCoords) {
        mushroomModel.submit(poseStack, submitNodeCollector, lightCoords, overlayCoords, outlineColor);
    }
}
