package io.github.aaronateataco.petsandpals.rendering.aprilfools.redstonebug;

import io.github.aaronateataco.petsandpals.mob.aprilfools.RedstoneBug;
import io.github.aaronateataco.petsandpals.rendering.PetRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.silverfish.SilverfishModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public class RedstoneBugRenderer extends PetRenderer<@NotNull RedstoneBug, @NotNull LivingEntityRenderState, @NotNull SilverfishModel> {

    public static final ModelLayerLocation REDSTONE_BUG_LOCATION = new ModelLayerLocation(Identifier.withDefaultNamespace("redstone_bug"), "main");

    public RedstoneBugRenderer(EntityRendererProvider.Context context) {
        super(context, new SilverfishModel(context.bakeLayer(ModelLayers.SILVERFISH)), 0.75f);
    }

    @Override
    public @NotNull Identifier getTextureLocation(LivingEntityRenderState livingEntityRenderState) {
        return Identifier.withDefaultNamespace("textures/entity/redstone_bug.png");
    }

    @Override
    public LivingEntityRenderState createRenderState() {
        return new LivingEntityRenderState();
    }
}
