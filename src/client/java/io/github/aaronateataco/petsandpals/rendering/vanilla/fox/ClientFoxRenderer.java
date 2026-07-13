package io.github.aaronateataco.petsandpals.rendering.vanilla.fox;

import io.github.aaronateataco.petsandpals.mob.vanilla.neutral.ClientFox;
import io.github.aaronateataco.petsandpals.rendering.PetRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.FoxRenderState;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static io.github.aaronateataco.petsandpals.Central.CONFIG;

public class ClientFoxRenderer extends PetRenderer<@NotNull ClientFox, @NotNull FoxRenderState, @NotNull ClientFoxModel> {
    public static final ModelLayerLocation FOX_LOCATION = new ModelLayerLocation(Identifier.withDefaultNamespace("clientfox"), "main");
    public String foxTexturePath;

    public ClientFoxRenderer(EntityRendererProvider.Context context) {
        super(context, new ClientFoxModel(context.bakeLayer(ModelLayers.FOX)), 0.75f);
    }

    @Override
    protected void scale(FoxRenderState state, @NotNull PoseStack poseStack) {
        if (CONFIG.isBaby) {
            poseStack.scale(0.5f, 0.5f, 0.5f);
        }
    }

    @Override
    public @NotNull Identifier getTextureLocation(FoxRenderState livingEntityRenderState) {
        // prefer the skin baked into this render state at extraction time over the
        // live CONFIG value - see ClientFoxRenderState's javadoc for why
        String skin = livingEntityRenderState instanceof ClientFoxRenderState cfrs && cfrs.skinOverride != null
                ? cfrs.skinOverride : CONFIG.foxSkin;
        if (Objects.equals(skin, "red")) {
            foxTexturePath = "textures/entity/fox/fox.png";
        } else if (Objects.equals(skin, "snow")) {
            foxTexturePath = "textures/entity/fox/fox_snow.png";
        } else {
            foxTexturePath = "textures/entity/fox/fox.png";
        }
        return Identifier.withDefaultNamespace(foxTexturePath);
    }

    @Override
    public FoxRenderState createRenderState() {
        return new ClientFoxRenderState();
    }

    @Override
    public void extractRenderState(ClientFox fox, FoxRenderState state, float f) {
        super.extractRenderState(fox, state, f);
        state.isSleeping = fox.isPassenger();
        if (state instanceof ClientFoxRenderState cfrs) {
            cfrs.skinOverride = CONFIG.foxSkin;
        }
    }
}
