package com.jeff.pets.rendering.custom.minecraft_earth.sheep;

import com.jeff.pets.rendering.vanilla.sheep.ClientSheepFurModel;
import com.jeff.pets.rendering.vanilla.sheep.ClientSheepModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.animal.sheep.SheepFurModel;
import net.minecraft.client.model.animal.sheep.SheepModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.SheepRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public class CustomSheepWoolLayer<S extends SheepModel> extends RenderLayer<@NotNull SheepRenderState, @NotNull S> {
    private final Identifier SHEEP_WOOL_LOCATION;
    private final EntityModel<@NotNull SheepRenderState> adultModel;

    public CustomSheepWoolLayer(final RenderLayerParent<@NotNull SheepRenderState, @NotNull S> renderer, Identifier sheepWoolLocation, final EntityModelSet modelSet) {
        super(renderer);
        SHEEP_WOOL_LOCATION = sheepWoolLocation;
        this.adultModel = new ClientSheepFurModel(modelSet.bakeLayer(ModelLayers.SHEEP_WOOL));
    }

    public void submit(final @NotNull PoseStack poseStack, final @NotNull SubmitNodeCollector submitNodeCollector, final int lightCoords, final SheepRenderState state, final float yRot, final float xRot) {

            EntityModel<@NotNull SheepRenderState> model = this.adultModel;
            Identifier location = SHEEP_WOOL_LOCATION;
            if (state.isInvisible) {
                if (state.appearsGlowing()) {
                    submitNodeCollector.submitModel(model, state, poseStack, RenderTypes.outline(location), lightCoords, LivingEntityRenderer.getOverlayCoords(state, 0.0F), -16777216, (TextureAtlasSprite)null, state.outlineColor, (ModelFeatureRenderer.CrumblingOverlay)null);
                }
            } else {
                coloredCutoutModelCopyLayerRender(model, location, poseStack, submitNodeCollector, lightCoords, state, state.getWoolColor(), state.isBaby ? 1 : 0);
        }
    }
}
