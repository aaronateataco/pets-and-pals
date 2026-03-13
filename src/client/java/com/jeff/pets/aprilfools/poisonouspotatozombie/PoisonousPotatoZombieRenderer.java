package com.jeff.pets.aprilfools.poisonouspotatozombie;

import com.jeff.pets.aprilfools.PoisonousPotatoZombie;
import com.jeff.pets.vanilla.zombie.ClientZombieModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.zombie.ZombieModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import static com.jeff.pets.Pet.CONFIG;

public class PoisonousPotatoZombieRenderer extends MobRenderer<@NotNull PoisonousPotatoZombie, @NotNull ZombieRenderState, @NotNull ClientZombieModel> {

    public static final ModelLayerLocation POISONOUS_POTATO_ZOMBIE_LOCATION = new ModelLayerLocation(Identifier.withDefaultNamespace("poisonosupotatozombie"), "main");

    public PoisonousPotatoZombieRenderer(EntityRendererProvider.Context context) {
        super(context, new ClientZombieModel(context.bakeLayer(ModelLayers.ZOMBIE)), 0.75f);
    }

    @Override
    protected void scale(@NotNull ZombieRenderState livingEntityRenderState, @NotNull PoseStack poseStack) {
        if (CONFIG.isBaby) {
            poseStack.scale(0.5f, 0.5f, 0.5f);
        }
    }

    @Override
    public @NotNull Identifier getTextureLocation(ZombieRenderState livingEntityRenderState) {
        return Identifier.withDefaultNamespace("textures/entity/zombie/poisonous_potato_zombie.png");
    }

    @Override
    public ZombieRenderState createRenderState() {
        return new ZombieRenderState();
    }
}
