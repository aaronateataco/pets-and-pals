package com.jeff.pets.vanilla.cow;

import com.jeff.pets.vanilla.passive.ClientCow;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.CowRenderState;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import static com.jeff.pets.Central.CONFIG;

public class ClientCowRenderer extends MobRenderer<@NotNull ClientCow, @NotNull CowRenderState, @NotNull ClientCowModel> {
    public static ModelLayerLocation COW_LOCATION = new ModelLayerLocation(Identifier.withDefaultNamespace("clientcow"), "main");
    String cowTexturePath;

    public ClientCowRenderer(EntityRendererProvider.Context context) {
        super(context, new ClientCowModel(context.bakeLayer(ModelLayers.COW)), 0.7F);
    }

    @Override
    public @NotNull Identifier getTextureLocation(CowRenderState cowRenderState) {
        switch (CONFIG.cowSkin) {
            case "temperate" -> cowTexturePath = "textures/entity/cow/temperate_cow.png";
            case "warm" -> cowTexturePath = "textures/entity/cow/warm_cow.png";
            case "cold" -> cowTexturePath = "textures/entity/cow/cold_cow.png";
            case null, default -> cowTexturePath = "textures/entity/cow/temperate_cow.png";
        }
        return Identifier.withDefaultNamespace(cowTexturePath);
    }

    @Override
    protected void scale(CowRenderState state, @NotNull PoseStack poseStack) {
        if (CONFIG.isBaby) {
            poseStack.scale(0.5f, 0.5f, 0.5f);
        }
    }

    @Override
    public CowRenderState createRenderState() {
        return new CowRenderState();
    }

    @Override
    public void extractRenderState(ClientCow cow, CowRenderState state, float f) {
        super.extractRenderState(cow, state, f);
        state.isUpsideDown = cow.getPlainTextName().equals("Grumm") || cow.getPlainTextName().equals("Dinnerbone");
    }
}
