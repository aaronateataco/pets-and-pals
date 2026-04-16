package com.jeff.pets.rendering.custom.minecraft_earth.pig.muddy_pig;

import com.jeff.pets.mob.custom.minecraft_earth.pig.MinecraftEarthPig;
import com.jeff.pets.rendering.PetRenderer;
import com.jeff.pets.rendering.custom.minecraft_earth.pig.AbstractEarthPigRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.PigRenderState;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import static com.jeff.pets.Central.CONFIG;
import static com.jeff.pets.PetsInitializer.MOD_ID;

public class MuddyPigRenderer extends PetRenderer<MinecraftEarthPig, PigRenderState, MuddyPigModel> {

    public static final ModelLayerLocation MUDDY_PIG_LOCATION = new ModelLayerLocation(Identifier.fromNamespaceAndPath(MOD_ID, "muddy_pig"), "main");

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(MOD_ID, "textures/entity/minecraft_earth/pig/muddy_pig.png");

    public MuddyPigRenderer(EntityRendererProvider.Context context) {
        super(context, new MuddyPigModel(context.bakeLayer(MUDDY_PIG_LOCATION)), 0.75f);
    }

    @Override
    public @NotNull PigRenderState createRenderState() {
        return new PigRenderState();
    }

    @Override
    public @NotNull Identifier getTextureLocation(@NotNull PigRenderState state) {
        return TEXTURE;
    }

    @Override
    protected void scale(@NotNull PigRenderState livingEntityRenderState, @NotNull PoseStack poseStack) {
        if (CONFIG.isBaby) {
            poseStack.scale(0.5f, 0.5f, 0.5f);
        }
    }
}
