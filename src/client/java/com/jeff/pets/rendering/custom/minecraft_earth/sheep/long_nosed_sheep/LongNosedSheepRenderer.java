package com.jeff.pets.rendering.custom.minecraft_earth.sheep.long_nosed_sheep;

import com.jeff.pets.mob.custom.minecraft_earth.sheep.MinecraftEarthSheep;
import com.jeff.pets.rendering.custom.minecraft_earth.sheep.AbstractEarthSheepRenderer;
import com.jeff.pets.rendering.custom.minecraft_earth.sheep.CustomSheepWoolLayer;
import com.jeff.pets.rendering.custom.minecraft_earth.sheep.EarthSheepModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.SheepRenderState;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import static com.jeff.pets.PetsInitializer.MOD_ID;

public class LongNosedSheepRenderer extends AbstractEarthSheepRenderer<MinecraftEarthSheep, EarthSheepModel> {

    public static final ModelLayerLocation LONG_NOSED_SHEEP_LOCATION = new ModelLayerLocation(Identifier.fromNamespaceAndPath(MOD_ID, "long_nosed_sheep"), "main");
    static final Identifier LONG_NOSED_SHEEP_WOOL_LOCATION = Identifier.fromNamespaceAndPath(MOD_ID, "textures/entity/minecraft_earth/sheep/wool_layer/long_nosed_sheep_wool.png");

    public LongNosedSheepRenderer(EntityRendererProvider.Context context) {
        super(context, new EarthSheepModel(context.bakeLayer(LONG_NOSED_SHEEP_LOCATION)), 0.75f);
        this.addLayer(new CustomSheepWoolLayer<>(this, LONG_NOSED_SHEEP_WOOL_LOCATION, context.getModelSet()));
    }

    @Override
    public @NotNull Identifier getTextureLocation(@NotNull SheepRenderState state) {
        return Identifier.fromNamespaceAndPath(MOD_ID, "textures/entity/minecraft_earth/sheep/long_nosed_sheep.png");
    }

    @Override
    public @NotNull SheepRenderState createRenderState() {
        return new SheepRenderState();
    }
}
