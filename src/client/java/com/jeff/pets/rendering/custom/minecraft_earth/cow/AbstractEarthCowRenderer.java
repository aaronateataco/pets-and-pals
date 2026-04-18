package com.jeff.pets.rendering.custom.minecraft_earth.cow;

import com.jeff.pets.mob.custom.minecraft_earth.cow.MinecraftEarthCow;
import com.jeff.pets.rendering.PetRenderer;
import com.jeff.pets.rendering.aprilfools.mooncow.LegacyCowModel;
import com.jeff.pets.rendering.aprilfools.mooncow.MoonCowRenderState;
import com.jeff.pets.rendering.vanilla.cow.ClientCowModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.CowRenderState;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import static com.jeff.pets.Central.CONFIG;

public abstract class AbstractEarthCowRenderer<T extends ClientCowModel> extends PetRenderer<MinecraftEarthCow, MoonCowRenderState, T> {

    private final ModelLayerLocation location;
    private final Identifier texture;

    public AbstractEarthCowRenderer(EntityRendererProvider.Context context, T model, Identifier identifier, ModelLayerLocation location) {
        super(context, model, 0.75f);
        this.location = location;
        this.texture = identifier;
    }

    @Override
    public @NotNull Identifier getTextureLocation(@NotNull MoonCowRenderState state) {
        return texture;
    }

    @Override
    public @NotNull MoonCowRenderState createRenderState() {
        return new MoonCowRenderState();
    }

    @Override
    protected void scale(MoonCowRenderState state, @NotNull PoseStack poseStack) {
        if (CONFIG.isBaby) {
            poseStack.scale(0.5f, 0.5f, 0.5f);
        }
    }
}
