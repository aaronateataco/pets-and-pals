package com.jeff.pets.rendering.custom.minecraft_earth.jolly_llama;

import com.jeff.pets.Utils;
import com.jeff.pets.mob.vanilla.neutral.ClientLlama;
import com.jeff.pets.rendering.PetRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LlamaRenderState;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public class JollyLlamaRenderer extends PetRenderer<ClientLlama, LlamaRenderState, JollyLlamaModel> {

    public static final ModelLayerLocation JOLLY_LLAMA_LOCATION = new ModelLayerLocation(Utils.withModNamespace("jolly_llama"), "main");

    public JollyLlamaRenderer(EntityRendererProvider.Context context) {
        super(context, new JollyLlamaModel(context.bakeLayer(JOLLY_LLAMA_LOCATION)), 0.75f);
    }

    @Override
    public @NotNull Identifier getTextureLocation(@NotNull LlamaRenderState state) {
        return Utils.withModNamespace("textures/entity/minecraft_earth/jolly_llama.png");
    }

    @Override
    public @NotNull LlamaRenderState createRenderState() {
        return new LlamaRenderState();
    }
}
