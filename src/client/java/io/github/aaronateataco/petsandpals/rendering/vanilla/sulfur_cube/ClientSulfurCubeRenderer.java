package io.github.aaronateataco.petsandpals.rendering.vanilla.sulfur_cube;

import io.github.aaronateataco.petsandpals.PetsInitializer;
import io.github.aaronateataco.petsandpals.mob.vanilla.passive.ClientSulfurCube;
import io.github.aaronateataco.petsandpals.rendering.PetRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.SlimeRenderState;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

/**
 * There's no real sulfur cube renderer on this version's client jar, so this rebuilds it
 * from the real vanilla geometry and textures (see {@link PetSulfurCubeModel}) instead of
 * approximating with an unrelated model. Scale math below is copied from vanilla's
 * SulfurCubeRenderer/AbstractCubeMobRenderer (decompiled), minus the parts this pet
 * doesn't need: baby model swap, TNT fuse swell, swallowed-block display.
 */
public class ClientSulfurCubeRenderer extends PetRenderer<@NotNull ClientSulfurCube, @NotNull SlimeRenderState, @NotNull PetSulfurCubeModel> {

    public static final ModelLayerLocation SULFUR_CUBE_OUTER_LOCATION =
            new ModelLayerLocation(Identifier.fromNamespaceAndPath(PetsInitializer.MOD_ID, "sulfur_cube"), "outer");
    public static final ModelLayerLocation SULFUR_CUBE_INNER_LOCATION =
            new ModelLayerLocation(Identifier.fromNamespaceAndPath(PetsInitializer.MOD_ID, "sulfur_cube"), "inner");
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(PetsInitializer.MOD_ID, "textures/entity/sulfur_cube/sulfur_cube_outer.png");

    public ClientSulfurCubeRenderer(EntityRendererProvider.Context context) {
        super(context, new PetSulfurCubeModel(context.bakeLayer(SULFUR_CUBE_OUTER_LOCATION)), 0.4f);
        this.addLayer(new SulfurCubeInnerRenderLayer(this, context));
    }

    @Override
    protected void scale(SlimeRenderState state, @NotNull PoseStack poseStack) {
        // downscaleSlightly(): anti z-fighting nudge between outer shell and inner core
        poseStack.scale(0.999f, 0.999f, 0.999f);
        poseStack.translate(0.0F, 0.001F, 0.0F);
        // applySizeAndSquish(): size fixed at 1 (no size variants for the pet version)
        float squishFactor = state.squish / 1.5F;
        float w = 1.0F / (squishFactor + 1.0F);
        poseStack.scale(w, 1.0F / w, w);
        // the model's 18-unit cube needs this downscale + lift to sit right in the
        // entity's bounding box, same constants vanilla uses for the adult cube
        poseStack.scale(0.5F, 0.5F, 0.5F);
        poseStack.translate(0.0F, 0.98F - 1.0F / 16.0F, 0.0F);
    }

    @Override
    public @NotNull Identifier getTextureLocation(SlimeRenderState state) {
        return TEXTURE;
    }

    @Override
    public SlimeRenderState createRenderState() {
        return new SlimeRenderState();
    }
}
