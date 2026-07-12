package io.github.aaronateataco.petsandpals.rendering.vanilla.tropicalfish;

import io.github.aaronateataco.petsandpals.mob.vanilla.passive.ClientTropicalFish;
import io.github.aaronateataco.petsandpals.rendering.PetRenderer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.animal.fish.TropicalFishLargeModel;
import net.minecraft.client.model.animal.fish.TropicalFishSmallModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.TropicalFishPatternLayer;
import net.minecraft.client.renderer.entity.state.TropicalFishRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.animal.fish.TropicalFish;
import net.minecraft.world.item.DyeColor;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static io.github.aaronateataco.petsandpals.Central.CONFIG;

/**
 * Reuses vanilla's own dual-model, two-layer tropical fish rendering (small/large
 * body plus a separately-tinted pattern overlay) wholesale rather than rebuilding
 * it - this is purely client-side, so the variant is derived fresh from the
 * config's skin name every frame instead of needing synced entity data.
 */
public class ClientTropicalFishRenderer extends PetRenderer<@NotNull ClientTropicalFish, @NotNull TropicalFishRenderState, @NotNull EntityModel<TropicalFishRenderState>> {

    private static final Identifier SMALL_TEXTURE = Identifier.withDefaultNamespace("textures/entity/fish/tropical_a.png");
    private static final Identifier LARGE_TEXTURE = Identifier.withDefaultNamespace("textures/entity/fish/tropical_b.png");

    // the 10 named skins, matched against the real predefined variants vanilla
    // itself spawns tropical fish as (verified against the wiki's variant table -
    // Pattern, body color, pattern color)
    private static final Map<String, TropicalFish.Variant> VARIANTS = Map.ofEntries(
            Map.entry("cichlid", new TropicalFish.Variant(TropicalFish.Pattern.SUNSTREAK, DyeColor.BLUE, DyeColor.GRAY)),
            Map.entry("clownfish", new TropicalFish.Variant(TropicalFish.Pattern.KOB, DyeColor.ORANGE, DyeColor.WHITE)),
            Map.entry("cotton_candy_betta", new TropicalFish.Variant(TropicalFish.Pattern.SPOTTY, DyeColor.PINK, DyeColor.LIGHT_BLUE)),
            Map.entry("goatfish", new TropicalFish.Variant(TropicalFish.Pattern.SPOTTY, DyeColor.WHITE, DyeColor.YELLOW)),
            Map.entry("parrotfish", new TropicalFish.Variant(TropicalFish.Pattern.DASHER, DyeColor.CYAN, DyeColor.PINK)),
            Map.entry("queen_angelfish", new TropicalFish.Variant(TropicalFish.Pattern.BRINELY, DyeColor.LIME, DyeColor.LIGHT_BLUE)),
            Map.entry("red_lipped_blenny", new TropicalFish.Variant(TropicalFish.Pattern.SNOOPER, DyeColor.GRAY, DyeColor.RED)),
            Map.entry("tomato_clownfish", new TropicalFish.Variant(TropicalFish.Pattern.KOB, DyeColor.RED, DyeColor.WHITE)),
            Map.entry("triggerfish", new TropicalFish.Variant(TropicalFish.Pattern.SUNSTREAK, DyeColor.GRAY, DyeColor.WHITE)),
            Map.entry("yellowtail_parrotfish", new TropicalFish.Variant(TropicalFish.Pattern.DASHER, DyeColor.CYAN, DyeColor.YELLOW))
    );
    private static final TropicalFish.Variant DEFAULT_VARIANT = VARIANTS.get("clownfish");

    private final EntityModel<TropicalFishRenderState> smallModel;
    private final EntityModel<TropicalFishRenderState> largeModel;

    public ClientTropicalFishRenderer(EntityRendererProvider.Context context) {
        super(context, new TropicalFishSmallModel(context.bakeLayer(ModelLayers.TROPICAL_FISH_SMALL)), 0.15f);
        this.smallModel = this.getModel();
        this.largeModel = new TropicalFishLargeModel(context.bakeLayer(ModelLayers.TROPICAL_FISH_LARGE));
        this.addLayer(new TropicalFishPatternLayer(this, context.getModelSet()));
    }

    private static TropicalFish.Variant currentVariant() {
        return VARIANTS.getOrDefault(CONFIG.tropicalFishSkin, DEFAULT_VARIANT);
    }

    @Override
    public @NotNull Identifier getTextureLocation(TropicalFishRenderState state) {
        return state.pattern.base() == TropicalFish.Base.LARGE ? LARGE_TEXTURE : SMALL_TEXTURE;
    }

    @Override
    public TropicalFishRenderState createRenderState() {
        return new TropicalFishRenderState();
    }

    @Override
    public void extractRenderState(ClientTropicalFish fish, TropicalFishRenderState state, float partialTick) {
        super.extractRenderState(fish, state, partialTick);
        TropicalFish.Variant variant = currentVariant();
        state.pattern = variant.pattern();
        state.baseColor = variant.baseColor().getTextureDiffuseColor();
        state.patternColor = variant.patternColor().getTextureDiffuseColor();
    }

    @Override
    protected int getModelTint(TropicalFishRenderState state) {
        return state.baseColor;
    }

    @Override
    public void submit(@NotNull TropicalFishRenderState state,
                        com.mojang.blaze3d.vertex.@NotNull PoseStack poseStack,
                        net.minecraft.client.renderer.@NotNull SubmitNodeCollector collector,
                        net.minecraft.client.renderer.state.level.@NotNull CameraRenderState camera) {
        this.model = state.pattern.base() == TropicalFish.Base.LARGE ? this.largeModel : this.smallModel;
        super.submit(state, poseStack, collector, camera);
    }
}
