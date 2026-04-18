package com.jeff.pets.rendering.custom.minecraft_earth.tropical_slime;

import com.jeff.pets.Utils;
import com.jeff.pets.mob.vanilla.hostile.ClientSlime;
import com.jeff.pets.rendering.PetRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.monster.slime.SlimeModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.SlimeOuterLayer;
import net.minecraft.client.renderer.entity.state.SlimeRenderState;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import static com.jeff.pets.Central.CONFIG;

public class TropicalSlimeRenderer extends PetRenderer<ClientSlime, SlimeRenderState, SlimeModel> {

    public static final ModelLayerLocation TROPICAL_SLIME_LOCATION = new ModelLayerLocation(Utils.withModNamespace("tropical_slime"), "main");

    public TropicalSlimeRenderer(EntityRendererProvider.Context context) {
        super(context, new SlimeModel(context.bakeLayer(TROPICAL_SLIME_LOCATION)), 0.75f);
        this.addLayer(new TropicalSlimeOuterLayer(this, context));
    }

    @Override
    public @NotNull Identifier getTextureLocation(@NotNull SlimeRenderState state) {
        return Utils.withModNamespace("textures/entity/minecraft_earth/slime/tropical_slime.png");
    }

    @Override
    public @NotNull SlimeRenderState createRenderState() {
        return new SlimeRenderState();
    }

    @Override
    protected void scale(SlimeRenderState slimeRenderState, @NotNull PoseStack poseStack) {
        int slimeScale = switch (CONFIG.slimeSkin) {
            case "small" -> 1;
            case "medium" -> 2;
            case "large" -> 4;
            case null, default -> 1;
        };
        float f = slimeRenderState.squish / ((float) slimeScale * 0.5F + 1.0F);
        float g = 1.0F / (f + 1.0F);
        poseStack.scale(g * (float) slimeScale, 1.0F / g * (float) slimeScale, g * (float) slimeScale);
    }
}
