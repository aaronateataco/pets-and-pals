package com.jeff.pets.aprilfools.mooncow;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.animal.cow.CowModel;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.CowRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class MoonCowHelmetLayer extends RenderLayer<@NotNull CowRenderState, @NotNull CowModel> {
    private final BlockRenderDispatcher blockRenderer;

    public MoonCowHelmetLayer(RenderLayerParent<@NotNull CowRenderState, @NotNull CowModel> renderLayerParent, BlockRenderDispatcher blockRenderDispatcher) {
        super(renderLayerParent);
        this.blockRenderer = blockRenderDispatcher;
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int i, CowRenderState entityRenderState, float f, float g) {
        poseStack.pushPose();
        this.getParentModel().getHead().translateAndRotate(poseStack);
        poseStack.translate(0.0F, 0F, -0.25F);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.scale(0.625F, -0.625F, -0.625F);
        BlockState blockState = Blocks.GLASS.defaultBlockState();
        BlockStateModel blockStateModel = this.blockRenderer.getBlockModel(blockState);
        int j = LivingEntityRenderer.getOverlayCoords(entityRenderState, 0.0F);
        poseStack.translate(-0.5F, -0.5F, -0.5F);
        RenderType renderType = entityRenderState.appearsGlowing() && entityRenderState.isInvisible ? RenderTypes.outline(TextureAtlas.LOCATION_BLOCKS) : ItemBlockRenderTypes.getRenderType(blockState);
        submitNodeCollector.submitBlockModel(poseStack, renderType, blockStateModel, 0.0F, 0.0F, 0.0F, i, j, entityRenderState.outlineColor);
        poseStack.popPose();
    }
}
