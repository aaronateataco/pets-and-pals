package com.jeff.pets.aprilfools.nerdcreeper;

import com.jeff.pets.aprilfools.NerdCreeper;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.creeper.CreeperModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.CreeperRenderState;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public class NerdCreeperRenderer extends MobRenderer<@NotNull NerdCreeper, @NotNull CreeperRenderState, @NotNull CreeperModel> {

    public static final ModelLayerLocation NERD_CREEPER_LOCATION = new ModelLayerLocation(Identifier.withDefaultNamespace("nerdcreeper"), "main");

    public NerdCreeperRenderer(EntityRendererProvider.Context context) {
        super(context, new CreeperModel(context.bakeLayer(ModelLayers.CREEPER)), 0.75f);
        this.addLayer(new NerdCreeperNerdLayer(this, context));
    }

    @Override
    public @NotNull Identifier getTextureLocation(CreeperRenderState livingEntityRenderState) {
        return Identifier.withDefaultNamespace("textures/entity/creeper/creeper.png");
    }

    @Override
    public CreeperRenderState createRenderState() {
        return new CreeperRenderState();
    }
}
