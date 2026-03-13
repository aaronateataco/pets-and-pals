package com.jeff.pets.vanilla.drowned;

import com.jeff.pets.vanilla.hostile.ClientDrowned;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.monster.zombie.DrownedModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.DrownedOuterLayer;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import static com.jeff.pets.Pet.CONFIG;

public class ClientDrownedRenderer extends MobRenderer<@NotNull ClientDrowned, @NotNull ZombieRenderState, @NotNull ClientDrownedModel> {

    public static final ModelLayerLocation DROWNED_LOCATION = new ModelLayerLocation(Identifier.withDefaultNamespace("clientdrowned"), "main");

    public ClientDrownedRenderer(EntityRendererProvider.Context context) {
        super(context, new ClientDrownedModel(context.bakeLayer(ModelLayers.DROWNED)), 0.75f);
        this.addLayer(new ClientDrownedOuterLayer(this, context));
    }

    public static LayerDefinition createBaseDrownedLayer() {
        ClientDrownedModel.createBodyLayer(CubeDeformation.NONE);
        return LayerDefinition.create(new MeshDefinition(), 64, 64);
    }

    protected void scale(ZombieRenderState state, @NotNull PoseStack poseStack) {
        if (CONFIG.isBaby) {
            poseStack.scale(0.5f, 0.5f, 0.5f);
        }
    }


    @Override
    public @NotNull Identifier getTextureLocation(ZombieRenderState livingEntityRenderState) {
        return Identifier.withDefaultNamespace("textures/entity/zombie/drowned.png");
    }

    @Override
    public ZombieRenderState createRenderState() {
        return new ZombieRenderState();
    }
    @Override
    public void extractRenderState(ClientDrowned drowned, ZombieRenderState state, float f) {
        super.extractRenderState(drowned, state, f);
        state.isBaby = CONFIG.isBaby;
    }
}
