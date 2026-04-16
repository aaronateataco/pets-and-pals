package com.jeff.pets.rendering.custom.minecraft_earth.pig;

import com.jeff.pets.mob.custom.minecraft_earth.pig.MinecraftEarthPig;
import com.jeff.pets.rendering.PetRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.PigRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Mob;
import org.jetbrains.annotations.NotNull;

import static com.jeff.pets.Central.CONFIG;

public class AbstractEarthPigRenderer extends PetRenderer<MinecraftEarthPig, PigRenderState, EarthPigModel> {

    private final Identifier texture;

    public AbstractEarthPigRenderer(EntityRendererProvider.Context context, ModelLayerLocation location, Identifier texture) {
        super(context, new EarthPigModel(context.bakeLayer(location)), 0.75f);
        this.texture = texture;
    }

    @Override
    public @NotNull Identifier getTextureLocation(@NotNull PigRenderState state) {
        return this.texture;
    }

    @Override
    protected void scale(@NotNull PigRenderState livingEntityRenderState, @NotNull PoseStack poseStack) {
        if (CONFIG.isBaby) {
            poseStack.scale(0.5f, 0.5f, 0.5f);
        }
    }

    @Override
    public @NotNull PigRenderState createRenderState() {
        return new PigRenderState();
    }
}
