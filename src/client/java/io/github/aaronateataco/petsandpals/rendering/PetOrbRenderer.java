package io.github.aaronateataco.petsandpals.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.github.aaronateataco.petsandpals.mob.PetOrb;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

/** Draws the ghost star: two crossed spinning nether-star planes, emissive. */
public class PetOrbRenderer extends EntityRenderer<@NotNull PetOrb, PetOrbRenderer.@NotNull PetOrbRenderState> {

    private static final Identifier TEXTURE = Identifier.withDefaultNamespace("textures/item/nether_star.png");
    private static final float HALF_SIZE = 0.17F;
    private static final int ALPHA = 225;
    private static final int FULL_BRIGHT = 0xF000F0;

    public PetOrbRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    public static class PetOrbRenderState extends EntityRenderState {
        public float spin;
        public float scale = 1.0F;
    }

    @Override
    public @NotNull PetOrbRenderState createRenderState() {
        return new PetOrbRenderState();
    }

    @Override
    public void extractRenderState(@NotNull PetOrb orb, @NotNull PetOrbRenderState state, float partialTick) {
        super.extractRenderState(orb, state, partialTick);
        state.spin = (orb.tickCount + partialTick) * 6.0F;
        state.scale = orb.renderScale(partialTick);
    }

    @Override
    public void submit(@NotNull PetOrbRenderState state, @NotNull PoseStack poseStack,
                       @NotNull SubmitNodeCollector collector, @NotNull CameraRenderState camera) {
        super.submit(state, poseStack, collector, camera);
        RenderType renderType = RenderTypes.entityTranslucentEmissive(TEXTURE);

        if (state.scale <= 0.0F) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.0F, 0.25F, 0.0F);
        poseStack.scale(state.scale, state.scale, state.scale);
        poseStack.mulPose(Axis.YP.rotationDegrees(state.spin));

        // vertical plane
        collector.submitCustomGeometry(poseStack, renderType, PetOrbRenderer::renderStarQuad);

        // horizontal plane
        poseStack.pushPose();
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(state.spin * 0.7F));
        collector.submitCustomGeometry(poseStack, renderType, PetOrbRenderer::renderStarQuad);
        poseStack.popPose();

        poseStack.popPose();
    }

    private static void renderStarQuad(PoseStack.Pose pose, VertexConsumer consumer) {
        // both faces so it's visible from behind
        quad(pose, consumer, false);
        quad(pose, consumer, true);
    }

    private static void quad(PoseStack.Pose pose, VertexConsumer consumer, boolean flipped) {
        float n = flipped ? -1.0F : 1.0F;
        float[][] corners = {
                {-HALF_SIZE, -HALF_SIZE, 0.0F, 1.0F},
                {HALF_SIZE, -HALF_SIZE, 1.0F, 1.0F},
                {HALF_SIZE, HALF_SIZE, 1.0F, 0.0F},
                {-HALF_SIZE, HALF_SIZE, 0.0F, 0.0F},
        };
        for (int i = 0; i < 4; i++) {
            float[] c = corners[flipped ? 3 - i : i];
            consumer.addVertex(pose, c[0], c[1], 0.0F)
                    .setColor(255, 255, 255, ALPHA)
                    .setUv(c[2], c[3])
                    .setUv1(0, 10)
                    .setLight(FULL_BRIGHT)
                    .setNormal(pose, 0.0F, 0.0F, n);
        }
    }
}
