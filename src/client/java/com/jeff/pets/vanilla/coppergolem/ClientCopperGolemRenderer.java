package com.jeff.pets.vanilla.coppergolem;

import com.jeff.pets.vanilla.passive.ClientCopperGolem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.animal.golem.CopperGolemModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.CopperGolemRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.animal.golem.CopperGolem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.BlockItemStateProperties;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;

import static com.jeff.pets.Central.CONFIG;

public class ClientCopperGolemRenderer extends MobRenderer<@NotNull ClientCopperGolem, @NotNull CopperGolemRenderState, @NotNull CopperGolemModel> {
    public static ModelLayerLocation COPPER_GOLEM_LOCATION = new ModelLayerLocation(Identifier.withDefaultNamespace("clientcoppergolem"), "main");

    public String copperGolemTexturePath;

    public ClientCopperGolemRenderer(EntityRendererProvider.Context context) {
        super(context, new CopperGolemModel(context.bakeLayer(ModelLayers.COPPER_GOLEM)), 0.5F);
        this.addLayer(new RenderLayer<>(() -> model) {
            @Override
            public void submit(@NotNull PoseStack poseStack, @NotNull SubmitNodeCollector submitNodeCollector, int i, CopperGolemRenderState entityRenderState, float f, float g) {
                renderColoredCutoutModel(this.getParentModel(), Identifier.withDefaultNamespace("textures/entity/copper_golem/copper_golem_eyes.png"), poseStack, submitNodeCollector, entityRenderState.lightCoords, entityRenderState, -1, OverlayTexture.NO_OVERLAY);
            }
        });
        this.addLayer(new ItemInHandLayer(this));
        CopperGolemModel var10005 = this.model;
        Objects.requireNonNull(var10005);
        this.addLayer(new CustomHeadLayer(this, context.getModelSet(), context.getPlayerSkinRenderCache()));
    }

    @Override
    public @NotNull Identifier getTextureLocation(CopperGolemRenderState copperGolemRenderState) {
        if (Objects.equals(CONFIG.copperGolemSkin, "unoxidized")) {
            copperGolemTexturePath = "textures/entity/copper_golem/copper_golem.png";
        } else if (Objects.equals(CONFIG.copperGolemSkin, "exposed")) {
            copperGolemTexturePath = "textures/entity/copper_golem/exposed_copper_golem.png";
        } else if (Objects.equals(CONFIG.copperGolemSkin, "oxidized")) {
            copperGolemTexturePath = "textures/entity/copper_golem/oxidized_copper_golem.png";
        } else if (Objects.equals(CONFIG.copperGolemSkin, "weathered")) {
            copperGolemTexturePath = "textures/entity/copper_golem/weathered_copper_golem.png";
        }
        return Identifier.withDefaultNamespace(copperGolemTexturePath);
    }

    @Override
    public CopperGolemRenderState createRenderState() {
        return new CopperGolemRenderState();
    }

    @Override
    public void extractRenderState(ClientCopperGolem copperGolem, CopperGolemRenderState state, float f) {
        super.extractRenderState(copperGolem, state, f);
        state.blockOnAntenna = Optional.of(copperGolem.getItemBySlot(CopperGolem.EQUIPMENT_SLOT_ANTENNA)).flatMap((itemStack) -> {
            Item item = itemStack.getItem();
            if (item instanceof BlockItem blockItem) {
                BlockItemStateProperties blockItemStateProperties = itemStack.getOrDefault(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY);
                return Optional.of(blockItemStateProperties.apply(blockItem.getBlock().defaultBlockState()));
            } else {
                return Optional.empty();
            }
        });
        state.isUpsideDown = copperGolem.getPlainTextName().equals("Grumm") || copperGolem.getPlainTextName().equals("Dinnerbone");
    }
}

