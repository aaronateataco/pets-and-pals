package io.github.aaronateataco.petsandpals.rendering.aprilfools.angryghast;

import io.github.aaronateataco.petsandpals.mob.aprilfools.AngryGhast;
import io.github.aaronateataco.petsandpals.rendering.PetRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.monster.ghast.GhastModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.GhastRenderState;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public class AngryGhastRenderer extends PetRenderer<@NotNull AngryGhast, @NotNull GhastRenderState, @NotNull GhastModel> {

    public static final ModelLayerLocation ANGRY_GHAST_LOCATION = new ModelLayerLocation(Identifier.withDefaultNamespace("angryghast"), "main");

    public AngryGhastRenderer(EntityRendererProvider.Context context) {
        super(context, new GhastModel(context.bakeLayer(ANGRY_GHAST_LOCATION)), 0.75f);
    }

    @Override
    public @NotNull Identifier getTextureLocation(GhastRenderState livingEntityRenderState) {
        return Identifier.withDefaultNamespace("textures/entity/angry_ghast/ghast_angry.png");
    }

    @Override
    public GhastRenderState createRenderState() {
        return new GhastRenderState();
    }
}
