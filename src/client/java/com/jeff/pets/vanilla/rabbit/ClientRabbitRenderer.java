package com.jeff.pets.vanilla.rabbit;

import com.jeff.pets.vanilla.passive.ClientRabbit;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.RabbitRenderState;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import static com.jeff.pets.Central.CONFIG;

public class ClientRabbitRenderer extends MobRenderer<@NotNull ClientRabbit, @NotNull RabbitRenderState, @NotNull ClientRabbitModel> {
    public static final ModelLayerLocation RABBIT_LOCATION = new ModelLayerLocation(Identifier.withDefaultNamespace("clientrabbit"), "main");
    public String rabbitTextureLocation;

    public ClientRabbitRenderer(EntityRendererProvider.Context context) {
        super(context, new ClientRabbitModel(context.bakeLayer(ModelLayers.RABBIT)), 0.3F);
    }

    @Override
    protected void scale(@NotNull RabbitRenderState livingEntityRenderState, @NotNull PoseStack poseStack) {
        if (CONFIG.isBaby) {
            poseStack.scale(0.5f, 0.5f, 0.5f);
        }
    }

    public static LayerDefinition createBaseRabbitLayer() {
        ClientRabbitModel.createBodyLayer(false);
        return LayerDefinition.create(new MeshDefinition(), 64, 32);
    }

    @Override
    public @NotNull Identifier getTextureLocation(RabbitRenderState rabbitRenderState) {
        switch (CONFIG.activePet) {
            case "brown" -> rabbitTextureLocation = "textures/entity/rabbit/brown.png";
            case "white" -> rabbitTextureLocation = "textures/entity/rabbit/white.png";
            case "black" -> rabbitTextureLocation = "textures/entity/rabbit/black.png";
            case "gold" -> rabbitTextureLocation = "textures/entity/rabbit/gold.png";
            case "salt" -> rabbitTextureLocation = "textures/entity/rabbit/salt.png";
            case "splotched" -> rabbitTextureLocation = "textures/entity/rabbit/white_splotched.png";
            case "killer" -> rabbitTextureLocation = "textures/entity/rabbit/caerbannog.png";
            case "toast" -> rabbitTextureLocation = "textures/entity/rabbit/toast.png";
            case null, default -> rabbitTextureLocation = "textures/entity/rabbit/brown.png";
        }

        return Identifier.withDefaultNamespace(rabbitTextureLocation);
    }

    @Override
    public RabbitRenderState createRenderState() {
        return new RabbitRenderState();
    }

    @Override
    public void extractRenderState(ClientRabbit rabbit, RabbitRenderState state, float f) {
        super.extractRenderState(rabbit, state, f);
        state.isUpsideDown = rabbit.getPlainTextName().equals("Grumm") || rabbit.getPlainTextName().equals("Dinnerbone");
    }
}
