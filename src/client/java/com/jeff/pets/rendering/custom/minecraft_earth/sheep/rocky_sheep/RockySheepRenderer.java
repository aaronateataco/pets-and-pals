package com.jeff.pets.rendering.custom.minecraft_earth.sheep.rocky_sheep;

import com.jeff.pets.mob.custom.minecraft_earth.sheep.RockySheep;
import com.jeff.pets.rendering.custom.minecraft_earth.sheep.AbstractEarthSheepRenderer;
import com.jeff.pets.rendering.custom.minecraft_earth.sheep.CustomSheepWoolLayer;
import com.jeff.pets.rendering.custom.minecraft_earth.sheep.EarthSheepModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.SheepRenderState;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import static com.jeff.pets.PetsInitializer.MOD_ID;

public class RockySheepRenderer extends AbstractEarthSheepRenderer<RockySheep, EarthSheepModel> {

    public static final ModelLayerLocation ROCKY_SHEEP_LOCATION = new ModelLayerLocation(Identifier.fromNamespaceAndPath(MOD_ID, "rocky_sheep"), "main");
    static final Identifier ROCKY_SHEEP_WOOL_LOCATION = Identifier.fromNamespaceAndPath(MOD_ID, "textures/entity/minecraft_earth/sheep/wool_layer/rocky_sheep_wool.png");

    public RockySheepRenderer(EntityRendererProvider.Context context) {
        super(context, new EarthSheepModel(context.bakeLayer(ROCKY_SHEEP_LOCATION)), 0.75f);
        this.addLayer(new CustomSheepWoolLayer<>(this, ROCKY_SHEEP_WOOL_LOCATION, context.getModelSet()));
    }

    @Override
    public @NotNull Identifier getTextureLocation(@NotNull SheepRenderState state) {
        return Identifier.fromNamespaceAndPath(MOD_ID, "textures/entity/minecraft_earth/sheep/rocky_sheep.png");
    }

    @Override
    public @NotNull SheepRenderState createRenderState() {
        return new SheepRenderState();
    }
}
