package com.jeff.pets.aprilfools.mooncow;

import com.jeff.pets.aprilfools.MoonCow;
import net.minecraft.client.model.animal.cow.CowModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.CowRenderState;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public class MoonCowRenderer extends MobRenderer<@NotNull MoonCow, @NotNull CowRenderState, @NotNull CowModel> {

    public static final ModelLayerLocation MOON_COW_LOCATION = new ModelLayerLocation(Identifier.withDefaultNamespace("mooncow"), "main");

    public MoonCowRenderer(EntityRendererProvider.Context context) {
        super(context, new LegacyCowModel(context.bakeLayer(MOON_COW_LOCATION)), 0.75f);
        this.addLayer(new MoonCowHelmetLayer(this, context.getBlockRenderDispatcher()));
    }

    @Override
    public @NotNull Identifier getTextureLocation(CowRenderState livingEntityRenderState) {
        return Identifier.withDefaultNamespace("textures/entity/cow/moon_cow.png");
    }

    @Override
    public CowRenderState createRenderState() {
        return new CowRenderState();
    }

    @Override
    public void extractRenderState(MoonCow cow, CowRenderState state, float f) {
        super.extractRenderState(cow, state, f);
        state.isUpsideDown = cow.getPlainTextName().equals("Grumm") || cow.getPlainTextName().equals("Dinnerbone");
    }
}
