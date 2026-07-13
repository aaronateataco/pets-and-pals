package io.github.aaronateataco.petsandpals.rendering.vanilla.bee;

import io.github.aaronateataco.petsandpals.mob.vanilla.neutral.ClientBee;
import io.github.aaronateataco.petsandpals.rendering.PetRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.animal.bee.AdultBeeModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.BeeRenderState;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static io.github.aaronateataco.petsandpals.Central.CONFIG;

public class ClientBeeRenderer extends PetRenderer<@NotNull ClientBee, @NotNull BeeRenderState, @NotNull AdultBeeModel> {
    public static final ModelLayerLocation BEE_LOCATION = new ModelLayerLocation(Identifier.withDefaultNamespace("clientbee"), "main");
    public String beeTexturePath;

    public ClientBeeRenderer(EntityRendererProvider.Context context) {
        super(context, new AdultBeeModel(context.bakeLayer(ModelLayers.BEE)), 0.4f);
    }

    @Override
    protected void scale(BeeRenderState state, @NotNull PoseStack poseStack) {
        if (CONFIG.isBaby) {
            poseStack.scale(0.5f, 0.5f, 0.5f);
        }
    }

    @Override
    public @NotNull Identifier getTextureLocation(BeeRenderState beeRenderState) {
        if (Objects.equals(CONFIG.beeSkin, "happy")) {
            beeTexturePath = "textures/entity/bee/bee.png";
        } else if (Objects.equals(CONFIG.beeSkin, "angry")) {
            beeTexturePath = "textures/entity/bee/bee_angry.png";
        } else {
            // an unrecognized/corrupted config value left this null before, which
            // crashed Identifier.withDefaultNamespace(null) instead of just falling
            // back to a default texture like every switch-based renderer already does
            beeTexturePath = "textures/entity/bee/bee.png";
        }
        return Identifier.withDefaultNamespace(beeTexturePath);
    }

    @Override
    public BeeRenderState createRenderState() {
        return new BeeRenderState();
    }

    @Override
    public void extractRenderState(ClientBee bee, BeeRenderState state, float partialTick) {
        super.extractRenderState(bee, state, partialTick);
        // vanilla's own BeeRenderer.extractRenderState() populates these five fields;
        // this renderer never extended that class (it extends the shared PetRenderer
        // instead), so they were always left at their Java defaults - isOnGround in
        // particular defaulting to false permanently pins AdultBeeModel/BeeModel's
        // setupAnim() onto its mid-flight branch (fixed-angle tucked legs, wing-flap
        // math driven by an ageInTicks that never advances since a preview bee is
        // never ticked), which is what actually produced the wrong, static pose - not
        // just a missing animation.
        //
        // bee.onGround() alone isn't enough though: MenagerieScreen/AdoptionScreen
        // render preview bees that are constructed fresh and never added to a level,
        // so they never receive a real tick (ClientLevel.tickEntity is what
        // increments tickCount - see ClientLevel.java) - onGround() on such an entity
        // permanently reads its Java default (false), reproducing the exact same
        // stuck-flying bug the first fix was meant to solve, just for every preview
        // thumbnail instead of the in-world pet. tickCount == 0 is the signal that
        // this bee has never actually been ticked, so treat it as grounded/idle -
        // a real flying pet bee (ticked every frame once summoned) is unaffected.
        state.isOnGround = bee.tickCount == 0
                || (bee.onGround() && bee.getDeltaMovement().lengthSqr() < 1.0E-7);
        state.hasStinger = true;
        state.isAngry = false;
        state.hasNectar = false;
        state.rollAmount = 0.0f;
    }
}
