package io.github.aaronateataco.petsandpals.rendering.vanilla.witch;

import io.github.aaronateataco.petsandpals.mob.vanilla.hostile.ClientWitch;
import io.github.aaronateataco.petsandpals.rendering.PetRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.witch.WitchModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.WitchRenderState;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public class ClientWitchRenderer extends PetRenderer<@NotNull ClientWitch, @NotNull WitchRenderState, @NotNull WitchModel> {

    public static final ModelLayerLocation WITCH_LOCATION = new ModelLayerLocation(Identifier.withDefaultNamespace("clientwitch"), "main");

    public ClientWitchRenderer(EntityRendererProvider.Context context) {
        super(context, new WitchModel(context.bakeLayer(ModelLayers.WITCH)), 0.75f);
    }

    @Override
    public @NotNull Identifier getTextureLocation(WitchRenderState livingEntityRenderState) {
        return Identifier.withDefaultNamespace("textures/entity/witch/witch.png");
    }

    @Override
    public WitchRenderState createRenderState() {
        return new WitchRenderState();
    }
}
