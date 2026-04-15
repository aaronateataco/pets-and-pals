package com.jeff.pets.rendering.custom.minecraft_earth.sheep.flecked_sheep;

import com.jeff.pets.mob.custom.minecraft_earth.sheep.FleckedSheep;
import com.jeff.pets.rendering.custom.minecraft_earth.sheep.AbstractEarthSheepRenderer;
import com.jeff.pets.rendering.custom.minecraft_earth.sheep.CustomSheepWoolLayer;
import com.jeff.pets.rendering.custom.minecraft_earth.sheep.EarthSheepModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.SheepRenderState;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import static com.jeff.pets.PetsInitializer.FLECKED_SHEEP;
import static com.jeff.pets.PetsInitializer.MOD_ID;

public class FleckedSheepRenderer extends AbstractEarthSheepRenderer<FleckedSheep, EarthSheepModel> {

    public static final ModelLayerLocation FLECKED_SHEEP_LOCATION = new ModelLayerLocation(Identifier.fromNamespaceAndPath(MOD_ID, "flecked_sheep"), "main");
    static final Identifier FLECKED_SHEEP_WOOL_LOCATION = Identifier.fromNamespaceAndPath(MOD_ID, "textures/entity/minecraft_earth/sheep/wool_layer/flecked_sheep_wool.png");

    public FleckedSheepRenderer(EntityRendererProvider.Context context) {
        super(context, new EarthSheepModel(context.bakeLayer(FLECKED_SHEEP_LOCATION)), 0.75f);
        this.addLayer(new CustomSheepWoolLayer<>(this, FLECKED_SHEEP_WOOL_LOCATION, context.getModelSet()));
    }

    @Override
    public @NotNull Identifier getTextureLocation(@NotNull SheepRenderState state) {
        return Identifier.fromNamespaceAndPath(MOD_ID, "textures/entity/minecraft_earth/sheep/flecked_sheep.png");
    }

    @Override
    public @NotNull SheepRenderState createRenderState() {
        return new SheepRenderState();
    }
}
