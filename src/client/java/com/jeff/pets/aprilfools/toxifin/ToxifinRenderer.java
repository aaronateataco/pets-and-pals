package com.jeff.pets.aprilfools.toxifin;

import com.jeff.pets.aprilfools.ToxifinSlab;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.GuardianRenderState;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import static com.jeff.pets.PetsInitializer.MOD_ID;

public class ToxifinRenderer extends MobRenderer<@NotNull ToxifinSlab, @NotNull GuardianRenderState, @NotNull ToxifinSlabModel> {

    public static final ModelLayerLocation TOXIFIN_LOCATION = new ModelLayerLocation(Identifier.fromNamespaceAndPath(MOD_ID, "toxifin_slab"), "main");

    public ToxifinRenderer(EntityRendererProvider.Context context) {
        super(context, new ToxifinSlabModel(context.bakeLayer(TOXIFIN_LOCATION)), 0.75f);
    }

    @Override
    public @NotNull Identifier getTextureLocation(GuardianRenderState livingEntityRenderState) {
        return Identifier.withDefaultNamespace("textures/entity/toxifin.png");
    }

    @Override
    public GuardianRenderState createRenderState() {
        return new GuardianRenderState();
    }

    @Override
    public void extractRenderState(ToxifinSlab slab, GuardianRenderState state, float f) {
        super.extractRenderState(slab, state, f);
        state.isUpsideDown = slab.getPlainTextName().equals("Grumm") || slab.getPlainTextName().equals("Dinnerbone");
    }
}
