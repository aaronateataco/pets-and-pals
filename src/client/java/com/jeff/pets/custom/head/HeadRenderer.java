package com.jeff.pets.custom.head;

import com.jeff.pets.custom.Head;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public class HeadRenderer extends MobRenderer<@NotNull Head, @NotNull HeadRenderState, @NotNull HeadModel> {
    public HeadRenderer(final EntityRendererProvider.Context context) {
        super(context, new HeadModel(context.bakeLayer(HeadModel.LAYER_LOCATION)), 0.3F);
    }

    public HeadRenderState createRenderState() {
        return new HeadRenderState();
    }

    @Override
    public @NotNull Identifier getTextureLocation(final HeadRenderState state) {
        return Identifier.withDefaultNamespace("playerskin.png");
    }

    @Override
    public void extractRenderState(Head head, HeadRenderState state, float f) {
        super.extractRenderState(head, state, f);
        state.isUpsideDown = head.getPlainTextName().equals("Grumm") || head.getPlainTextName().equals("Dinnerbone");
    }
}
