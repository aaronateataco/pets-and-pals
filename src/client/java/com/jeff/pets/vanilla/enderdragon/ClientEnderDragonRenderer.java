package com.jeff.pets.vanilla.enderdragon;

import com.jeff.pets.PetsInitializer;
import com.jeff.pets.vanilla.boss.ClientEnderDragon;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.dragon.EnderDragonModel;
import net.minecraft.client.renderer.entity.EnderDragonRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.EnderDragonRenderState;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import static com.jeff.pets.Central.CONFIG;


public class ClientEnderDragonRenderer extends MobRenderer<@NotNull ClientEnderDragon, @NotNull ClientEnderDragonRenderState, @NotNull ClientEnderDragonModel> {

    public static final ModelLayerLocation ENDER_DRAGON_LOCATION = new ModelLayerLocation(Identifier.withDefaultNamespace("clientenderdragon"), "main");

    public ClientEnderDragonRenderer(EntityRendererProvider.Context context) {
        super(context, new ClientEnderDragonModel(context.bakeLayer(ModelLayers.ENDER_DRAGON)), 0.75f);
    }

    @Override
    protected void scale(@NotNull ClientEnderDragonRenderState livingEntityRenderState, @NotNull PoseStack poseStack) {
        if (CONFIG.isBaby) {
            poseStack.scale(0.25f, 0.25f, 0.25f);
            PetsInitializer.enderDragonHitboxHeight = 3;
            PetsInitializer.enderDragonHitboxWidth = 3;
        } else {
            PetsInitializer.enderDragonHitboxWidth = 16;
            PetsInitializer.enderDragonHitboxHeight = 8;
        }
    }

    @Override
    public @NotNull Identifier getTextureLocation(ClientEnderDragonRenderState livingEntityRenderState) {
        return Identifier.withDefaultNamespace("textures/entity/enderdragon/dragon.png");
    }

    @Override
    public ClientEnderDragonRenderState createRenderState() {
        return new ClientEnderDragonRenderState();
    }

    @Override
    public void extractRenderState(ClientEnderDragon dragon, ClientEnderDragonRenderState state, float f) {
        super.extractRenderState(dragon, state, f);
        state.flapTime = dragon.getId() + state.ageInTicks / 8;
        state.isUpsideDown = dragon.getPlainTextName().equals("Grumm") || dragon.getPlainTextName().equals("Dinnerbone");
    }
}
