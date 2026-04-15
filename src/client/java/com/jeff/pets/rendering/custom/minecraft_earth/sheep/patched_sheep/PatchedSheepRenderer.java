package com.jeff.pets.rendering.custom.minecraft_earth.sheep.patched_sheep;

import com.jeff.pets.mob.custom.minecraft_earth.sheep.PatchedSheep;
import com.jeff.pets.rendering.custom.minecraft_earth.sheep.AbstractEarthSheepRenderer;
import com.jeff.pets.rendering.custom.minecraft_earth.sheep.CustomSheepWoolLayer;
import com.jeff.pets.rendering.custom.minecraft_earth.sheep.EarthSheepModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.SheepRenderState;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import static com.jeff.pets.PetsInitializer.MOD_ID;

public class PatchedSheepRenderer extends AbstractEarthSheepRenderer<PatchedSheep, EarthSheepModel> {

    public static final ModelLayerLocation PATCHED_SHEEP_LOCATION = new ModelLayerLocation(Identifier.fromNamespaceAndPath(MOD_ID, "patched_sheep"), "main");
    static final Identifier PATCHED_SHEEP_WOOL_LOCATION = Identifier.fromNamespaceAndPath(MOD_ID, "textures/entity/minecraft_earth/sheep/wool_layer/patched_sheep_wool.png");

    public PatchedSheepRenderer(EntityRendererProvider.Context context) {
        super(context, new EarthSheepModel(context.bakeLayer(PATCHED_SHEEP_LOCATION)), 0.75f);
        this.addLayer(new CustomSheepWoolLayer<>(this, PATCHED_SHEEP_WOOL_LOCATION, context.getModelSet()));
    }

    @Override
    public @NotNull Identifier getTextureLocation(@NotNull SheepRenderState state) {
        return Identifier.fromNamespaceAndPath(MOD_ID, "textures/entity/minecraft_earth/sheep/patched_sheep.png");
    }

    @Override
    public @NotNull SheepRenderState createRenderState() {
        return new SheepRenderState();
    }
}
