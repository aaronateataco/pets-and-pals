package com.jeff.pets.aprilfools.mooncow;

import com.jeff.pets.aprilfools.MoonCow;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.animal.cow.CowModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

/**Alright I give up. I'll try and add the glass helmet on the moon cow back if and when Minecraft
 * makes it easier to do, since this is rediculous.
 */
public class MoonCowRenderer extends MobRenderer<@NotNull MoonCow, @NotNull MoonCowRenderState, @NotNull CowModel> {

    public static final ModelLayerLocation MOON_COW_LOCATION = new ModelLayerLocation(Identifier.withDefaultNamespace("mooncow"), "main");
    private final BlockModelResolver resolver;
    PoseStack poseStack = new PoseStack();

    public MoonCowRenderer(EntityRendererProvider.Context context) {
        super(context, new LegacyCowModel(context.bakeLayer(MOON_COW_LOCATION)), 0.75f);
        this.resolver = context.getBlockModelResolver();
        this.addLayer(new MoonCowHelmetLayer(this, context));
    }

    @Override
    public @NotNull Identifier getTextureLocation(MoonCowRenderState livingEntityRenderState) {
        return Identifier.withDefaultNamespace("textures/entity/cow/moon_cow.png");
    }

    @Override
    public MoonCowRenderState createRenderState() {
        return new MoonCowRenderState();
    }

    @Override
    public void extractRenderState(MoonCow cow, MoonCowRenderState state, float f) {
        super.extractRenderState(cow, state, f);
        state.blockOnHead.submit(poseStack, Minecraft.getInstance().gameRenderer.getSubmitNodeStorage(), state.lightCoords, LivingEntityRenderer.getOverlayCoords(state, 0), state.outlineColor);
        resolver.update(state.blockOnHead, Blocks.GLASS.defaultBlockState(), BlockDisplayContext.create());
        state.isUpsideDown = cow.getPlainTextName().equals("Grumm") || cow.getPlainTextName().equals("Dinnerbone");
    }
}
