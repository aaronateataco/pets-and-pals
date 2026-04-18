package com.jeff.pets.rendering.custom.minecraft_earth.dyed_cat;

import com.jeff.pets.Utils;
import com.jeff.pets.mob.vanilla.passive.ClientCat;
import com.jeff.pets.rendering.PetRenderer;
import com.jeff.pets.rendering.vanilla.cat.ClientCatModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.CatRenderState;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public class DyedCatRenderer extends PetRenderer<ClientCat, CatRenderState, ClientCatModel> {

    public static final ModelLayerLocation DYED_CAT_LOCATION = new ModelLayerLocation(Utils.withModNamespace("dyed_cat"), "main");

    public DyedCatRenderer(EntityRendererProvider.Context context) {
        super(context, new ClientCatModel(context.bakeLayer(ModelLayers.CAT)), 0.75f);
    }

    @Override
    public @NotNull Identifier getTextureLocation(@NotNull CatRenderState state) {
        return Utils.withModNamespace("textures/entity/minecraft_earth/dyed_cat.png");
    }

    @Override
    public @NotNull CatRenderState createRenderState() {
        return new CatRenderState();
    }
}
