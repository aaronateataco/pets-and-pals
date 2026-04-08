package com.jeff.pets.rendering.aprilfools.mooncow;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.animal.cow.CowModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MoonCowHelmetLayer extends RenderLayer<@NotNull MoonCowRenderState, @NotNull LegacyCowModel> {

    private final BlockModelResolver resolver;
    BlockModelRenderState state = new BlockModelRenderState();
    public MoonCowHelmetLayer(RenderLayerParent<MoonCowRenderState, LegacyCowModel> renderLayerParent, EntityRendererProvider.Context context) {
        super(renderLayerParent);
        this.resolver = context.getBlockModelResolver();
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int i, MoonCowRenderState entityRenderState, float f, float g) {
        poseStack.pushPose();
        poseStack.translate(0.0F, 0F, -0.25F);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.scale(0.625F, -0.625F, -0.625F);
        BlockState blockState = Blocks.GLASS.defaultBlockState();
        int j = LivingEntityRenderer.getOverlayCoords(entityRenderState, 0.0F);
        poseStack.translate(-0.5F, -0.5F, -0.5F);
        submitNodeCollector.submitBlockModel(poseStack, RenderTypes.solidMovingBlock(), List.of(), state.tintLayers().toIntArray(), entityRenderState.lightCoords, LivingEntityRenderer.getOverlayCoords(entityRenderState, f), entityRenderState.outlineColor);
        resolver.update(state, Blocks.GLASS.defaultBlockState(), BlockDisplayContext.create());
        poseStack.popPose();
    }
}
