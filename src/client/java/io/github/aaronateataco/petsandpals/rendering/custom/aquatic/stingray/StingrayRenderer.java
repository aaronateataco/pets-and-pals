package io.github.aaronateataco.petsandpals.rendering.custom.aquatic.stingray;

import io.github.aaronateataco.petsandpals.mob.custom.aquatic.Stingray;
import io.github.aaronateataco.petsandpals.rendering.PetRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import static io.github.aaronateataco.petsandpals.PetsInitializer.MOD_ID;

public class StingrayRenderer extends PetRenderer<Stingray, StingrayRenderState, StingrayModel> {

    public static final ModelLayerLocation STINGRAY_LOCATION = new ModelLayerLocation(Identifier.fromNamespaceAndPath(MOD_ID, "stingray"), "main");

    public StingrayRenderer(EntityRendererProvider.Context context) {
        super(context, new StingrayModel(context.bakeLayer(STINGRAY_LOCATION)), 0.75f);
    }

    @Override
    public @NotNull Identifier getTextureLocation(@NotNull StingrayRenderState state) {
        return Identifier.fromNamespaceAndPath(MOD_ID, "textures/entity/stingray/stingray.png");
    }

    @Override
    public @NotNull StingrayRenderState createRenderState() {
        return new StingrayRenderState();
    }

    @Override
    public void extractRenderState(Stingray stingray, StingrayRenderState state, float f) {
        super.extractRenderState(stingray, state, f);
        state.flapTime = stingray.flap + state.ageInTicks;
    }
}
