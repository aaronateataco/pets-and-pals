package io.github.aaronateataco.petsandpals.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.aaronateataco.petsandpals.mob.PetRaft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.FallingBlockRenderer;
import net.minecraft.client.renderer.entity.state.FallingBlockRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.FallingBlockEntity;
import org.jetbrains.annotations.NotNull;

/**
 * Falling-block renderer with a twist: the raft turns with its hull physics and
 * grows to fit whoever's on deck.
 */
public class PetRaftRenderer extends FallingBlockRenderer {

    public static class State extends FallingBlockRenderState {
        public float scale = 1.0F;
        public float yaw;
    }

    public PetRaftRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public @NotNull FallingBlockRenderState createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(@NotNull FallingBlockEntity entity, @NotNull FallingBlockRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        if (entity instanceof PetRaft raft && state instanceof State raftState) {
            raftState.scale = raft.renderScale();
            raftState.yaw = Mth.rotLerp(partialTick, raft.yRotO, raft.getYRot());
        }
        // vanilla's own leash renderer already supports a sagging rope curve (see
        // LeashFeatureRenderer's slack handling), it's just never turned on here - no
        // need for a dedicated physics mod just to get a rope that isn't a straight
        // line. If LeadPhysics (or similar) is also installed, its own render hook
        // takes over from here same as it would for any other leashed entity.
        if (state.leashStates != null) {
            for (net.minecraft.client.renderer.entity.state.EntityRenderState.LeashState leashState : state.leashStates) {
                leashState.slack = true;
            }
        }
    }

    @Override
    public void submit(@NotNull FallingBlockRenderState state, @NotNull PoseStack poseStack,
                       @NotNull SubmitNodeCollector collector, @NotNull CameraRenderState camera) {
        if (state instanceof State raftState) {
            poseStack.pushPose();
            poseStack.mulPose(Axis.YP.rotationDegrees(-raftState.yaw));
            poseStack.scale(raftState.scale, 1.0F, raftState.scale);
            super.submit(state, poseStack, collector, camera);
            poseStack.popPose();
        } else {
            super.submit(state, poseStack, collector, camera);
        }
    }
}
