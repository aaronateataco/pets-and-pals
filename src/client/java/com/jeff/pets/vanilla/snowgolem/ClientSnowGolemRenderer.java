package com.jeff.pets.vanilla.snowgolem;

import com.jeff.pets.vanilla.passive.ClientSnowGolem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.animal.golem.SnowGolemModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.layers.BlockDecorationLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.layers.SnowGolemHeadLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.SnowGolemRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Display;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.jeff.pets.Central.CONFIG;


public class ClientSnowGolemRenderer extends MobRenderer<@NotNull ClientSnowGolem, @NotNull SnowGolemRenderState, @NotNull SnowGolemModel> {
    public static final ModelLayerLocation SNOW_GOLEM = new ModelLayerLocation(Identifier.withDefaultNamespace("clientsnowgolem"), "main");

    private final BlockModelResolver resolver;

    public ClientSnowGolemRenderer(EntityRendererProvider.Context context) {
        super(context, new SnowGolemModel(context.bakeLayer(ModelLayers.SNOW_GOLEM)), 0.5F);
        this.resolver = context.getBlockModelResolver();
        this.addLayer(new SnowGolemHeadLayer(this));
    }

    @Override
    public @NotNull Identifier getTextureLocation(SnowGolemRenderState snowGolemRenderState) {
        return Identifier.withDefaultNamespace("textures/entity/snow_golem/snow_golem.png");
    }

    @Override
    public SnowGolemRenderState createRenderState() {
        return new SnowGolemRenderState();
    }

    @Override
    public void extractRenderState(ClientSnowGolem snowGolem, SnowGolemRenderState state, float f) {
        super.extractRenderState(snowGolem, state, f);
        if (CONFIG.snowGolemSkin.equals("pumpkin_on")) {
            this.resolver.update(state.headBlock, Blocks.CARVED_PUMPKIN.defaultBlockState(), BlockDisplayContext.create());
        } else {
            state.headBlock.clear();
        }
        state.isUpsideDown = snowGolem.getPlainTextName().equals("Grumm") || snowGolem.getPlainTextName().equals("Dinnerbone");
    }
}
