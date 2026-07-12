package io.github.aaronateataco.petsandpals.rendering.vanilla.sulfur_cube;

import io.github.aaronateataco.petsandpals.PetsInitializer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.SlimeRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

/**
 * The glowing core visible through the translucent shell - simplified from vanilla's
 * SulfurCubeInnerLayer (no swallowed-block display, no baby model swap; this pet doesn't
 * need either).
 */
public class SulfurCubeInnerRenderLayer extends RenderLayer<@NotNull SlimeRenderState, @NotNull PetSulfurCubeModel> {

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(PetsInitializer.MOD_ID, "textures/entity/sulfur_cube/sulfur_cube_inner.png");

    private final PetSulfurCubeModel innerModel;

    public SulfurCubeInnerRenderLayer(RenderLayerParent<@NotNull SlimeRenderState, @NotNull PetSulfurCubeModel> renderLayerParent, EntityRendererProvider.Context context) {
        super(renderLayerParent);
        this.innerModel = new PetSulfurCubeModel(context.bakeLayer(ClientSulfurCubeRenderer.SULFUR_CUBE_INNER_LOCATION));
    }

    @Override
    public void submit(@NotNull PoseStack poseStack, @NotNull SubmitNodeCollector submitNodeCollector, int lightCoords, SlimeRenderState state, float yRot, float xRot) {
        if (state.isInvisible) return;
        int overlayCoords = LivingEntityRenderer.getOverlayCoords(state, 0.0F);
        submitNodeCollector.order(-1).submitModel(this.innerModel, state, poseStack,
                RenderTypes.entityTranslucent(TEXTURE), lightCoords, overlayCoords, -1, null, state.outlineColor, null);
    }
}
