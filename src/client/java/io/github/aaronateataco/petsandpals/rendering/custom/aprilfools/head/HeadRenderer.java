package io.github.aaronateataco.petsandpals.rendering.custom.aprilfools.head;

import io.github.aaronateataco.petsandpals.mob.custom.aprilfools.Head;
import io.github.aaronateataco.petsandpals.rendering.PetRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.component.ResolvableProfile;
import org.jetbrains.annotations.NotNull;

import static io.github.aaronateataco.petsandpals.Central.CONFIG;

public class HeadRenderer extends PetRenderer<@NotNull Head, @NotNull LivingEntityRenderState, @NotNull HeadModel> {
    public HeadRenderer(final EntityRendererProvider.Context context) {
        super(context, new HeadModel(context.bakeLayer(HeadModel.LAYER_LOCATION)), 0.3F);
    }

    @Override
    public LivingEntityRenderState createRenderState() {
        return new LivingEntityRenderState();
    }

    @Override
    public @NotNull Identifier getTextureLocation(final LivingEntityRenderState state) {
        return Minecraft.getInstance().playerSkinRenderCache().getOrDefault(ResolvableProfile.createUnresolved(CONFIG.headSkin)).playerSkin().body().texturePath();
    }
}
