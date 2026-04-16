package com.jeff.pets.rendering.custom.minecraft_earth.sheep;

import com.jeff.pets.rendering.PetRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.SheepRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import org.jetbrains.annotations.NotNull;

import static com.jeff.pets.Central.CONFIG;

public abstract class AbstractEarthSheepRenderer<H extends Mob, I extends EntityModel<@NotNull SheepRenderState>> extends PetRenderer<@NotNull H, @NotNull SheepRenderState, @NotNull I> {
    public AbstractEarthSheepRenderer(EntityRendererProvider.Context context, I model, float shadow) {
        super(context, model, shadow);
    }
    @Override
    protected void scale(@NotNull SheepRenderState livingEntityRenderState, @NotNull PoseStack poseStack) {
        if (CONFIG.isBaby) {
            poseStack.scale(0.5f, 0.5f, 0.5f);
        }
    }
    @Override
    public void extractRenderState(H entity, SheepRenderState state, float f) {
        super.extractRenderState(entity, state, f);
        state.isSheared = false;
    }
}
