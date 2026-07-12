package io.github.aaronateataco.petsandpals.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.aaronateataco.petsandpals.mob.PetRaft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.FallingBlockRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.FallingBlockRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.phys.Vec3;
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
            for (EntityRenderState.LeashState leashState : state.leashStates) {
                leashState.slack = true;
            }
        }
        // vanilla's default leash endpoints are both centered on their entities (the
        // boat has no notion of "stern", the raft none of "bow") - pin them to the
        // actual back-of-boat/front-of-raft points instead, using real-time positions
        // rather than trusting yaw (which eases in and can lag a sharp turn)
        if (entity instanceof PetRaft raft && state.leashStates != null && !state.leashStates.isEmpty()
                && raft.getLeashHolder() instanceof AbstractBoat boat) {
            EntityRenderState.LeashState leashState = state.leashStates.getFirst();
            Vec3 raftPos = raft.getPosition(partialTick);
            Vec3 boatPos = boat.getPosition(partialTick);
            Vec3 toBoat = new Vec3(boatPos.x - raftPos.x, 0.0, boatPos.z - raftPos.z);
            double toBoatLen = toBoat.length();
            Vec3 toBoatDir = toBoatLen > 1.0e-4 ? toBoat.scale(1.0 / toBoatLen) : new Vec3(0.0, 0.0, 1.0);
            double raftHalfLength = 0.4 * raft.renderScale();
            leashState.start = raftPos.add(toBoatDir.x * raftHalfLength, 0.12, toBoatDir.z * raftHalfLength);

            float boatYaw = Mth.rotLerp(partialTick, boat.yRotO, boat.getYRot()) * ((float) Math.PI / 180.0F);
            Vec3 sternDir = new Vec3(-Mth.sin(boatYaw), 0.0, Mth.cos(boatYaw));
            double sternDist = 0.64 * boat.getBbWidth();
            leashState.end = boatPos.add(sternDir.x * sternDist, 0.3, sternDir.z * sternDist);
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
