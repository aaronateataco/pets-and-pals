package com.jeff.pets.rendering.custom.minecraft_earth.sheep.rainbow_sheep;

import com.jeff.pets.mob.custom.minecraft_earth.sheep.MinecraftEarthSheep;
import com.jeff.pets.rendering.custom.minecraft_earth.sheep.AbstractEarthSheepRenderer;
import com.jeff.pets.rendering.custom.minecraft_earth.sheep.CustomSheepWoolLayer;
import com.jeff.pets.rendering.custom.minecraft_earth.sheep.EarthSheepModel;
import com.jeff.pets.rendering.vanilla.sheep.ClientSheepModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.SheepRenderState;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import static com.jeff.pets.PetsInitializer.MOD_ID;

public class RainbowSheepRenderer extends AbstractEarthSheepRenderer<@NotNull MinecraftEarthSheep, @NotNull EarthSheepModel> {

    public static final ModelLayerLocation RAINBOW_SHEEP_LOCATION = new ModelLayerLocation(Identifier.fromNamespaceAndPath(MOD_ID, "rainbow_sheep"), "main");
    final Identifier RAINBOW_SHEEP_WOOL_LOCATION = Identifier.fromNamespaceAndPath(MOD_ID, "textures/entity/minecraft_earth/sheep/wool_layer/rainbow_sheep_wool.png");

    public RainbowSheepRenderer(EntityRendererProvider.Context context) {
        super(context, new EarthSheepModel(context.bakeLayer(RAINBOW_SHEEP_LOCATION)), 0.75f);
        this.addLayer(new CustomSheepWoolLayer<>(this, RAINBOW_SHEEP_WOOL_LOCATION, context.getModelSet()));
    }

    @Override
    public @NotNull Identifier getTextureLocation(SheepRenderState state) {
        return Identifier.fromNamespaceAndPath(MOD_ID, "textures/entity/minecraft_earth/sheep/rainbow_sheep.png");
    }

    @Override
    public SheepRenderState createRenderState() {
        return new SheepRenderState();
    }
}
