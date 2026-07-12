package io.github.aaronateataco.petsandpals.rendering.vanilla.parrot;

import io.github.aaronateataco.petsandpals.mob.vanilla.passive.ClientParrot;
import io.github.aaronateataco.petsandpals.rendering.PetRenderer;
import net.minecraft.client.model.animal.parrot.ParrotModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ParrotRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static io.github.aaronateataco.petsandpals.Central.CONFIG;

public class ClientParrotRenderer extends PetRenderer<@NotNull ClientParrot, @NotNull ParrotRenderState, @NotNull ParrotModel> {
    public static final ModelLayerLocation PARROT_LOCATION = new ModelLayerLocation(Identifier.withDefaultNamespace("clientparrot"), "main");

    String parrotTexturePath;

    public ClientParrotRenderer(EntityRendererProvider.Context context) {
        super(context, new ParrotModel(context.bakeLayer(ModelLayers.PARROT)), 0.3F);
    }

    public @NotNull Identifier getTextureLocation(ParrotRenderState parrotRenderState) {
        if (Objects.equals(CONFIG.parrotSkin, "red")) {
            parrotTexturePath = "textures/entity/parrot/parrot_red_blue.png";
        } else if (Objects.equals(CONFIG.parrotSkin, "blue")) {
            parrotTexturePath = "textures/entity/parrot/parrot_blue.png";
        } else if (Objects.equals(CONFIG.parrotSkin, "green")) {
            parrotTexturePath = "textures/entity/parrot/parrot_green.png";
        } else if (Objects.equals(CONFIG.parrotSkin, "cyan")) {
            // "cyan" is the name used everywhere else (the /petskin command, Advanced
            // Settings, ParrotSkins enum) for vanilla's 5th parrot color - it was
            // never actually vanilla's own name for it (vanilla calls this variant
            // "yellow", texture parrot_yellow_blue.png), but every other file agrees
            // on "cyan" so this renderer was the one out of step, silently doing
            // nothing when that skin was selected
            parrotTexturePath = "textures/entity/parrot/parrot_yellow_blue.png";
        } else if (Objects.equals(CONFIG.parrotSkin, "gray")) {
            // the real file is spelled the British way ("grey"), unlike every other
            // vanilla "gray" texture in the game - this was pointing at a file that
            // doesn't exist
            parrotTexturePath = "textures/entity/parrot/parrot_grey.png";
        } else {
            parrotTexturePath = "textures/entity/parrot/parrot_red_blue.png";
        }
        return Identifier.withDefaultNamespace(parrotTexturePath);

    }

    public ParrotRenderState createRenderState() {
        return new ParrotRenderState();
    }

    @Override
    public void extractRenderState(ClientParrot parrot, ParrotRenderState state, float f) {
        super.extractRenderState(parrot, state, f);
        float flap = Mth.lerp(f, parrot.oFlap, parrot.flap);
        float flapSpeed = Mth.lerp(f, parrot.oFlapSpeed, parrot.flapSpeed);
        state.flapAngle = (Mth.sin(flap) + 1.0F) * flapSpeed;
    }
}
