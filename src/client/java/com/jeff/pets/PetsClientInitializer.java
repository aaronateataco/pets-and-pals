package com.jeff.pets;

import com.jeff.pets.rendering.aprilfools.angryghast.AngryGhastRenderer;
import com.jeff.pets.rendering.aprilfools.batato.BatatoModel;
import com.jeff.pets.rendering.aprilfools.batato.BatatoRenderer;
import com.jeff.pets.rendering.aprilfools.diamondchicken.DiamondChickenRenderer;
import com.jeff.pets.rendering.aprilfools.lovegolem.LoveGolemRenderer;
import com.jeff.pets.rendering.aprilfools.megaspud.MegaSpudModel;
import com.jeff.pets.rendering.aprilfools.megaspud.MegaSpudOuterLayer;
import com.jeff.pets.rendering.aprilfools.megaspud.MegaSpudRenderer;
import com.jeff.pets.rendering.aprilfools.mooncow.LegacyCowModel;
import com.jeff.pets.rendering.aprilfools.mooncow.MoonCowRenderer;
import com.jeff.pets.rendering.aprilfools.nerdcreeper.NerdCreeperRenderer;
import com.jeff.pets.rendering.aprilfools.pinkwither.PinkWitherRenderer;
import com.jeff.pets.rendering.aprilfools.plaguewhale.PlaguewhaleRenderer;
import com.jeff.pets.rendering.aprilfools.poisonouspotatozombie.PoisonousPotatoZombieRenderer;
import com.jeff.pets.rendering.aprilfools.potatohusk.PotatoHuskRenderer;
import com.jeff.pets.rendering.aprilfools.raytracing.RayTracingRenderer;
import com.jeff.pets.rendering.aprilfools.redstonebug.RedstoneBugRenderer;
import com.jeff.pets.rendering.aprilfools.smilingcreeper.SmilingCreeperRenderer;
import com.jeff.pets.rendering.aprilfools.toxifin.ToxifinRenderer;
import com.jeff.pets.rendering.aprilfools.toxifin.ToxifinSlabModel;
import com.jeff.pets.rendering.aprilfools.traitor.TraitorRenderer;
import com.jeff.pets.rendering.custom.aprilfools.head.HeadModel;
import com.jeff.pets.rendering.custom.aprilfools.head.HeadRenderer;
import com.jeff.pets.rendering.custom.aquatic.dumbo_octopus.DumboOctopusModel;
import com.jeff.pets.rendering.custom.aquatic.dumbo_octopus.DumboOctopusRenderer;
import com.jeff.pets.rendering.custom.aquatic.koi.KoiModel;
import com.jeff.pets.rendering.custom.aquatic.koi.KoiRenderer;
import com.jeff.pets.rendering.custom.aquatic.stingray.StingrayModel;
import com.jeff.pets.rendering.custom.aquatic.stingray.StingrayRenderer;
import com.jeff.pets.rendering.custom.first.duck.DuckModel;
import com.jeff.pets.rendering.custom.first.duck.DuckRenderer;
import com.jeff.pets.rendering.custom.first.penguin.PenguinModel;
import com.jeff.pets.rendering.custom.first.penguin.PenguinRenderer;
import com.jeff.pets.rendering.custom.first.racoon.RacoonModel;
import com.jeff.pets.rendering.custom.first.racoon.RacoonRenderer;
import com.jeff.pets.rendering.vanilla.allay.ClientAllayRenderer;
import com.jeff.pets.rendering.vanilla.armadillo.ClientArmadilloRenderer;
import com.jeff.pets.rendering.vanilla.axolotl.ClientAxolotlRenderer;
import com.jeff.pets.rendering.vanilla.bat.ClientBatRenderer;
import com.jeff.pets.rendering.vanilla.bee.ClientBeeRenderer;
import com.jeff.pets.rendering.vanilla.blaze.ClientBlazeRenderer;
import com.jeff.pets.rendering.vanilla.bogged.ClientBoggedRenderer;
import com.jeff.pets.rendering.vanilla.breeze.ClientBreezeRenderer;
import com.jeff.pets.rendering.vanilla.camel.ClientCamelRenderer;
import com.jeff.pets.rendering.vanilla.cat.ClientCatRenderer;
import com.jeff.pets.rendering.vanilla.cavespider.ClientCaveSpiderModel;
import com.jeff.pets.rendering.vanilla.cavespider.ClientCaveSpiderRenderer;
import com.jeff.pets.rendering.vanilla.chicken.ClientChickenModel;
import com.jeff.pets.rendering.vanilla.chicken.ClientChickenRenderer;
import com.jeff.pets.rendering.vanilla.cod.ClientCodRenderer;
import com.jeff.pets.rendering.vanilla.coppergolem.ClientCopperGolemRenderer;
import com.jeff.pets.rendering.vanilla.cow.ClientCowModel;
import com.jeff.pets.rendering.vanilla.cow.ClientCowRenderer;
import com.jeff.pets.rendering.vanilla.creaking.ClientCreakingRenderer;
import com.jeff.pets.rendering.vanilla.creeper.ClientCreeperRenderer;
import com.jeff.pets.rendering.vanilla.dolphin.ClientDolphinRenderer;
import com.jeff.pets.rendering.vanilla.donkey.ClientDonkeyRenderer;
import com.jeff.pets.rendering.vanilla.drowned.ClientDrownedRenderer;
import com.jeff.pets.rendering.vanilla.elderguardian.ClientElderGuardianRenderer;
import com.jeff.pets.rendering.vanilla.enderdragon.ClientEnderDragonRenderer;
import com.jeff.pets.rendering.vanilla.enderman.ClientEndermanRenderer;
import com.jeff.pets.rendering.vanilla.endermite.ClientEndermiteRenderer;
import com.jeff.pets.rendering.vanilla.evoker.ClientEvokerModel;
import com.jeff.pets.rendering.vanilla.evoker.ClientEvokerRenderer;
import com.jeff.pets.rendering.vanilla.fox.ClientFoxModel;
import com.jeff.pets.rendering.vanilla.fox.ClientFoxRenderer;
import com.jeff.pets.rendering.vanilla.frog.ClientFrogRenderer;
import com.jeff.pets.rendering.vanilla.ghast.ClientGhastRenderer;
import com.jeff.pets.rendering.vanilla.goat.ClientGoatModel;
import com.jeff.pets.rendering.vanilla.goat.ClientGoatRenderer;
import com.jeff.pets.rendering.vanilla.guardian.ClientGuardianRenderer;
import com.jeff.pets.rendering.vanilla.happyghast.ClientHappyGhastRenderer;
import com.jeff.pets.rendering.vanilla.hoglin.ClientHoglinModel;
import com.jeff.pets.rendering.vanilla.hoglin.ClientHoglinRenderer;
import com.jeff.pets.rendering.vanilla.horse.ClientHorseRenderer;
import com.jeff.pets.rendering.vanilla.husk.ClientHuskRenderer;
import com.jeff.pets.rendering.vanilla.irongolem.ClientIronGolemRenderer;
import com.jeff.pets.rendering.vanilla.llama.ClientLlamaRenderer;
import com.jeff.pets.rendering.vanilla.magmacube.ClientMagmaCubeRenderer;
import com.jeff.pets.rendering.vanilla.mooshroom.ClientMooshroomRenderer;
import com.jeff.pets.rendering.vanilla.nautilus.ClientNautilusRenderer;
import com.jeff.pets.rendering.vanilla.panda.ClientPandaRenderer;
import com.jeff.pets.rendering.vanilla.parched.ClientParchedRenderer;
import com.jeff.pets.rendering.vanilla.parrot.ClientParrotRenderer;
import com.jeff.pets.rendering.vanilla.phantom.ClientPhantomRenderer;
import com.jeff.pets.rendering.vanilla.pig.ClientPigRenderer;
import com.jeff.pets.rendering.vanilla.piglin.ClientPiglinRenderer;
import com.jeff.pets.rendering.vanilla.pillager.ClientPillagerModel;
import com.jeff.pets.rendering.vanilla.pillager.ClientPillagerRenderer;
import com.jeff.pets.rendering.vanilla.polarbear.ClientPolarBearRenderer;
import com.jeff.pets.rendering.vanilla.pufferfish.ClientPufferFishRenderer;
import com.jeff.pets.rendering.vanilla.rabbit.ClientRabbitModel;
import com.jeff.pets.rendering.vanilla.rabbit.ClientRabbitRenderer;
import com.jeff.pets.rendering.vanilla.ravager.ClientRavagerRenderer;
import com.jeff.pets.rendering.vanilla.salmon.ClientSalmonModel;
import com.jeff.pets.rendering.vanilla.salmon.ClientSalmonRenderer;
import com.jeff.pets.rendering.vanilla.sheep.ClientSheepModel;
import com.jeff.pets.rendering.vanilla.sheep.ClientSheepRenderer;
import com.jeff.pets.rendering.vanilla.sheep.ClientSheepWoolLayer;
import com.jeff.pets.rendering.vanilla.shulker.ClientShulkerRenderer;
import com.jeff.pets.rendering.vanilla.silverfish.ClientSilverfishRenderer;
import com.jeff.pets.rendering.vanilla.skeleton.ClientSkeletonRenderer;
import com.jeff.pets.rendering.vanilla.slime.ClientSlimeRenderer;
import com.jeff.pets.rendering.vanilla.sniffer.ClientSnifferRenderer;
import com.jeff.pets.rendering.vanilla.snowgolem.ClientSnowGolemRenderer;
import com.jeff.pets.rendering.vanilla.spider.ClientSpiderRenderer;
import com.jeff.pets.rendering.vanilla.squid.ClientSquidRenderer;
import com.jeff.pets.rendering.vanilla.stray.ClientStrayRenderer;
import com.jeff.pets.rendering.vanilla.strider.ClientStriderRenderer;
import com.jeff.pets.rendering.vanilla.tadpole.ClientTadpoleRenderer;
import com.jeff.pets.rendering.vanilla.turtle.ClientTurtleRenderer;
import com.jeff.pets.rendering.vanilla.vex.ClientVexRenderer;
import com.jeff.pets.rendering.vanilla.villager.ClientVillagerRenderer;
import com.jeff.pets.rendering.vanilla.vindicator.ClientVindicatorRenderer;
import com.jeff.pets.rendering.vanilla.wanderingtrader.ClientWanderingTraderRenderer;
import com.jeff.pets.rendering.vanilla.warden.ClientWardenRenderer;
import com.jeff.pets.rendering.vanilla.witch.ClientWitchRenderer;
import com.jeff.pets.rendering.vanilla.wither.ClientWitherRenderer;
import com.jeff.pets.rendering.vanilla.witherskeleton.ClientWitherSkeletonRenderer;
import com.jeff.pets.rendering.vanilla.wolf.ClientWolfRenderer;
import com.jeff.pets.rendering.vanilla.zombie.ClientZombieRenderer;
import com.jeff.pets.rendering.vanilla.zombievillager.ClientZombieVillagerModel;
import com.jeff.pets.rendering.vanilla.zombievillager.ClientZombieVillagerRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.ambient.BatModel;
import net.minecraft.client.model.animal.allay.AllayModel;
import net.minecraft.client.model.animal.armadillo.AdultArmadilloModel;
import net.minecraft.client.model.animal.axolotl.AdultAxolotlModel;
import net.minecraft.client.model.animal.bee.AdultBeeModel;
import net.minecraft.client.model.animal.camel.AdultCamelModel;
import net.minecraft.client.model.animal.dolphin.DolphinModel;
import net.minecraft.client.model.animal.fish.CodModel;
import net.minecraft.client.model.animal.fish.PufferfishBigModel;
import net.minecraft.client.model.animal.frog.FrogModel;
import net.minecraft.client.model.animal.frog.TadpoleModel;
import net.minecraft.client.model.animal.golem.CopperGolemModel;
import net.minecraft.client.model.animal.golem.IronGolemModel;
import net.minecraft.client.model.animal.golem.SnowGolemModel;
import net.minecraft.client.model.animal.nautilus.NautilusModel;
import net.minecraft.client.model.animal.panda.PandaModel;
import net.minecraft.client.model.animal.parrot.ParrotModel;
import net.minecraft.client.model.animal.sniffer.SnifferModel;
import net.minecraft.client.model.animal.squid.SquidModel;
import net.minecraft.client.model.animal.turtle.AdultTurtleModel;
import net.minecraft.client.model.monster.blaze.BlazeModel;
import net.minecraft.client.model.monster.breeze.BreezeModel;
import net.minecraft.client.model.monster.creaking.CreakingModel;
import net.minecraft.client.model.monster.dragon.EnderDragonModel;
import net.minecraft.client.model.monster.enderman.EndermanModel;
import net.minecraft.client.model.monster.endermite.EndermiteModel;
import net.minecraft.client.model.monster.ghast.GhastModel;
import net.minecraft.client.model.monster.guardian.GuardianModel;
import net.minecraft.client.model.monster.phantom.PhantomModel;
import net.minecraft.client.model.monster.ravager.RavagerModel;
import net.minecraft.client.model.monster.shulker.ShulkerModel;
import net.minecraft.client.model.monster.silverfish.SilverfishModel;
import net.minecraft.client.model.monster.skeleton.BoggedModel;
import net.minecraft.client.model.monster.skeleton.SkeletonModel;
import net.minecraft.client.model.monster.slime.MagmaCubeModel;
import net.minecraft.client.model.monster.slime.SlimeModel;
import net.minecraft.client.model.monster.spider.SpiderModel;
import net.minecraft.client.model.monster.strider.AdultStriderModel;
import net.minecraft.client.model.monster.vex.VexModel;
import net.minecraft.client.model.monster.warden.WardenModel;
import net.minecraft.client.model.monster.witch.WitchModel;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

import static com.jeff.pets.PetsInitializer.LOGGER;

/**
 * Another {@code initializer} class. This class does a couple of things:
 * <p> - Assigns renderers to the entities defined in {@link PetsInitializer}
 * <p> - Bakes models into the layers
 * <p> - Creates the keybind to open the config screen, by default {@code p}
 * <p> Suppresses: Deprecation warnings, as {@link EntityRendererRegistry} is marked as
 * {@code deprecated}. I will likely have to find a suitable replacement sometime, but for now,
 * suppressing the warnings will work.
 *
 * @see PetsInitializer
 * @see Central
 */
@SuppressWarnings("deprecation")
public class PetsClientInitializer implements ClientModInitializer {

    public static List<String> ADDONS = new ArrayList<>();

    /**
     * Misc rendering stuff
     */
    @Override
    public void onInitializeClient() {

        this.createKeyBinding();

        EntityRendererRegistry.register(PetsInitializer.HEAD, HeadRenderer::new);
        EntityRendererRegistry.register(PetsInitializer.DUCK, DuckRenderer::new);
        EntityRendererRegistry.register(PetsInitializer.RACOON, RacoonRenderer::new);
        EntityRendererRegistry.register(PetsInitializer.PENGUIN, PenguinRenderer::new);
        EntityRendererRegistry.register(PetsInitializer.SHEEP, ClientSheepRenderer::new);
        EntityRendererRegistry.register(PetsInitializer.CAT, ClientCatRenderer::new);
        EntityRendererRegistry.register(PetsInitializer.ALLAY, ClientAllayRenderer::new);
        EntityRendererRegistry.register(PetsInitializer.ARMADILLO, ClientArmadilloRenderer::new);
        EntityRendererRegistry.register(PetsInitializer.AXOLOTL, ClientAxolotlRenderer::new);
        EntityRendererRegistry.register(PetsInitializer.BAT, ClientBatRenderer::new);
        EntityRendererRegistry.register(PetsInitializer.CAMEL, ClientCamelRenderer::new);
        EntityRendererRegistry.register(PetsInitializer.CHICKEN, ClientChickenRenderer::new);
        EntityRendererRegistry.register(PetsInitializer.COD, ClientCodRenderer::new);
        EntityRendererRegistry.register(PetsInitializer.COPPER_GOLEM, ClientCopperGolemRenderer::new);
        EntityRendererRegistry.register(PetsInitializer.COW, ClientCowRenderer::new);
        EntityRendererRegistry.register(PetsInitializer.DONKEY, ClientDonkeyRenderer::new);
        EntityRendererRegistry.register(PetsInitializer.FROG, ClientFrogRenderer::new);
        EntityRendererRegistry.register(PetsInitializer.HORSE, ClientHorseRenderer::new);
        EntityRendererRegistry.register(PetsInitializer.MOOSHROOM, ClientMooshroomRenderer::new);
        EntityRendererRegistry.register(PetsInitializer.PARROT, ClientParrotRenderer::new);
        EntityRendererRegistry.register(PetsInitializer.PIG, ClientPigRenderer::new);
        EntityRendererRegistry.register(PetsInitializer.RABBIT, ClientRabbitRenderer::new);
        EntityRendererRegistry.register(PetsInitializer.SALMON, ClientSalmonRenderer::new);
        EntityRendererRegistry.register(PetsInitializer.SNIFFER, ClientSnifferRenderer::new);
        EntityRendererRegistry.register(PetsInitializer.SNOW_GOLEM, ClientSnowGolemRenderer::new);
        EntityRendererRegistry.register(PetsInitializer.SQUID, ClientSquidRenderer::new);
        EntityRendererRegistry.register(PetsInitializer.STRIDER, ClientStriderRenderer::new);
        EntityRendererRegistry.register(PetsInitializer.TADPOLE, ClientTadpoleRenderer::new);
        EntityRendererRegistry.register(PetsInitializer.TURTLE, ClientTurtleRenderer::new);
        EntityRendererRegistry.register(PetsInitializer.VILLAGER, ClientVillagerRenderer::new);
        EntityRendererRegistry.register(PetsInitializer.WANDERING_TRADER, ClientWanderingTraderRenderer::new);
        EntityRendererRegistry.register(PetsInitializer.BEE, ClientBeeRenderer::new);
        EntityRendererRegistry.register(PetsInitializer.CAVE_SPIDER, ClientCaveSpiderRenderer::new);
        EntityRendererRegistry.register(PetsInitializer.DOLPHIN, ClientDolphinRenderer::new);
        EntityRendererRegistry.register(PetsInitializer.ENDERMAN, ClientEndermanRenderer::new);
        EntityRendererRegistry.register(PetsInitializer.FOX, ClientFoxRenderer::new);
        EntityRendererRegistry.register(PetsInitializer.GOAT, ClientGoatRenderer::new);
        EntityRendererRegistry.register(PetsInitializer.IRON_GOLEM, ClientIronGolemRenderer::new);
        EntityRendererRegistry.register(PetsInitializer.LLAMA, ClientLlamaRenderer::new);
        EntityRendererRegistry.register(PetsInitializer.NAUTILUS, ClientNautilusRenderer::new);
        EntityRendererRegistry.register(PetsInitializer.PANDA, ClientPandaRenderer::new);
        EntityRendererRegistry.register(PetsInitializer.PIGLIN, ClientPiglinRenderer::new);
        EntityRendererRegistry.register(PetsInitializer.POLAR_BEAR, ClientPolarBearRenderer::new);
        EntityRendererRegistry.register(PetsInitializer.PUFFERFISH, ClientPufferFishRenderer::new);
        EntityRendererRegistry.register(PetsInitializer.SPIDER, ClientSpiderRenderer::new);
        EntityRendererRegistry.register(PetsInitializer.WOLF, ClientWolfRenderer::new);
        EntityRendererRegistry.register(PetsInitializer.ELDER_GUARDIAN_COOKIE, ClientElderGuardianRenderer::new);
        EntityRendererRegistry.register(PetsInitializer.HAPPY_GHAST, ClientHappyGhastRenderer::new);
        EntityRendererRegistry.register(PetsInitializer.BLAZE, ClientBlazeRenderer::new);
        EntityRendererRegistry.register(PetsInitializer.BOGGED, ClientBoggedRenderer::new);
        EntityRendererRegistry.register(PetsInitializer.BREEZE, ClientBreezeRenderer::new);
        EntityRendererRegistry.register(PetsInitializer.CREAKING, ClientCreakingRenderer::new);
        EntityRendererRegistry.register(PetsInitializer.CREEPER, ClientCreeperRenderer::new);
        EntityRendererRegistry.register(PetsInitializer.DROWNED, ClientDrownedRenderer::new);
        EntityRendererRegistry.register(PetsInitializer.ENDERMITE, ClientEndermiteRenderer::new);
        EntityRendererRegistry.register(PetsInitializer.EVOKER, ClientEvokerRenderer::new);
        EntityRendererRegistry.register(PetsInitializer.GHAST, ClientGhastRenderer::new);
        EntityRendererRegistry.register(PetsInitializer.GUARDIAN, ClientGuardianRenderer::new);
        EntityRendererRegistry.register(PetsInitializer.HOGLIN, ClientHoglinRenderer::new);
        EntityRendererRegistry.register(PetsInitializer.HUSK, ClientHuskRenderer::new);
        EntityRendererRegistry.register(PetsInitializer.MAGMA_CUBE, ClientMagmaCubeRenderer::new);
        EntityRendererRegistry.register(PetsInitializer.PARCHED, ClientParchedRenderer::new);
        EntityRendererRegistry.register(PetsInitializer.PHANTOM, ClientPhantomRenderer::new);
        EntityRendererRegistry.register(PetsInitializer.PILLAGER, ClientPillagerRenderer::new);
        EntityRendererRegistry.register(PetsInitializer.RAVAGER, ClientRavagerRenderer::new);
        EntityRendererRegistry.register(PetsInitializer.SHULKER, ClientShulkerRenderer::new);
        EntityRendererRegistry.register(PetsInitializer.SILVERFISH, ClientSilverfishRenderer::new);
        EntityRendererRegistry.register(PetsInitializer.SKELETON, ClientSkeletonRenderer::new);
        EntityRendererRegistry.register(PetsInitializer.SLIME, ClientSlimeRenderer::new);
        EntityRendererRegistry.register(PetsInitializer.STRAY, ClientStrayRenderer::new);
        EntityRendererRegistry.register(PetsInitializer.VEX, ClientVexRenderer::new);
        EntityRendererRegistry.register(PetsInitializer.VINDICATOR, ClientVindicatorRenderer::new);
        EntityRendererRegistry.register(PetsInitializer.WARDEN, ClientWardenRenderer::new);
        EntityRendererRegistry.register(PetsInitializer.WITCH, ClientWitchRenderer::new);
        EntityRendererRegistry.register(PetsInitializer.WITHER_SKELETON, ClientWitherSkeletonRenderer::new);
        EntityRendererRegistry.register(PetsInitializer.ZOMBIE, ClientZombieRenderer::new);
        EntityRendererRegistry.register(PetsInitializer.ZOMBIE_VILLAGER, ClientZombieVillagerRenderer::new);
        EntityRendererRegistry.register(PetsInitializer.ENDER_DRAGON, ClientEnderDragonRenderer::new);
        EntityRendererRegistry.register(PetsInitializer.WITHER, ClientWitherRenderer::new);
        EntityRendererRegistry.register(PetsInitializer.BATATO, BatatoRenderer::new);
        EntityRendererRegistry.register(PetsInitializer.ANGRY_GHAST, AngryGhastRenderer::new);
        EntityRendererRegistry.register(PetsInitializer.BATATO, BatatoRenderer::new);
        EntityRendererRegistry.register(PetsInitializer.DIAMOND_CHICKEN, DiamondChickenRenderer::new);
        EntityRendererRegistry.register(PetsInitializer.LOVE_GOLEM, LoveGolemRenderer::new);
        EntityRendererRegistry.register(PetsInitializer.MEGA_SPUD, MegaSpudRenderer::new);
        EntityRendererRegistry.register(PetsInitializer.MOON_COW, MoonCowRenderer::new);
        EntityRendererRegistry.register(PetsInitializer.NERD_CREEPER, NerdCreeperRenderer::new);
        EntityRendererRegistry.register(PetsInitializer.PINK_WITHER, PinkWitherRenderer::new);
        EntityRendererRegistry.register(PetsInitializer.PLAGUEWHALE_SLAB, PlaguewhaleRenderer::new);
        EntityRendererRegistry.register(PetsInitializer.POISONOUS_POTATO_ZOMBIE, PoisonousPotatoZombieRenderer::new);
        EntityRendererRegistry.register(PetsInitializer.POTATO_HUSK, PotatoHuskRenderer::new);
        EntityRendererRegistry.register(PetsInitializer.RAY_TRACING, RayTracingRenderer::new);
        EntityRendererRegistry.register(PetsInitializer.REDSTONE_BUG, RedstoneBugRenderer::new);
        EntityRendererRegistry.register(PetsInitializer.SMILING_CREEPER, SmilingCreeperRenderer::new);
        EntityRendererRegistry.register(PetsInitializer.TOXIFIN_SLAB, ToxifinRenderer::new);
        EntityRendererRegistry.register(PetsInitializer.TRAITOR, TraitorRenderer::new);
        EntityRendererRegistry.register(PetsInitializer.DUMBO_OCTOPUS, DumboOctopusRenderer::new);
        EntityRendererRegistry.register(PetsInitializer.KOI, KoiRenderer::new);
        EntityRendererRegistry.register(PetsInitializer.STINGRAY, StingrayRenderer::new);

        ModelLayerRegistry.registerModelLayer(HeadModel.LAYER_LOCATION, HeadModel::getTexturedModelData);
        ModelLayerRegistry.registerModelLayer(RacoonRenderer.RACOON_LOCATION, RacoonModel::getTexturedModelData);
        ModelLayerRegistry.registerModelLayer(DuckModel.LAYER_LOCATION, DuckModel::getTexturedModelData);
        ModelLayerRegistry.registerModelLayer(PenguinModel.PENGUIN_LOCATION, PenguinModel::getTexturedModelData);
        ModelLayerRegistry.registerModelLayer(ClientSheepRenderer.SHEEP_LOCATION, ClientSheepModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(ClientSheepWoolLayer.SHEEP_WOOL_LOCATION, ClientSheepModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(ClientCatRenderer.CAT_LOCATION, ClientCatRenderer::createCatBodyLayer);
        ModelLayerRegistry.registerModelLayer(ClientAllayRenderer.ALLAY_TEXTURE, AllayModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(ClientArmadilloRenderer.ARMADILLO_LOCATION, AdultArmadilloModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(ClientAxolotlRenderer.AXOLOTL_LOCATION, AdultAxolotlModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(ClientBatRenderer.BAT_LOCATION, BatModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(ClientCamelRenderer.CAMEL_LOCATION, AdultCamelModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(ClientChickenRenderer.CHICKEN_LOCATION, ClientChickenModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(ClientCodRenderer.COD_LOCATION, CodModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(ClientCopperGolemRenderer.COPPER_GOLEM_LOCATION, CopperGolemModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(ClientCowRenderer.COW_LOCATION, ClientCowModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(ClientDonkeyRenderer.DONKEY_LOCATION, ClientDonkeyRenderer::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(ClientFrogRenderer.FROG_LOCATION, FrogModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(ClientHorseRenderer.HORSE_LOCATION, ClientHorseRenderer::createBaseHorseLayer);
        ModelLayerRegistry.registerModelLayer(ClientMooshroomRenderer.MOOSHROOM_LOCATION, ClientCowModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(ClientParrotRenderer.PARROT_LOCATION, ParrotModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(ClientPigRenderer.PIG_LOCATION, ClientPigRenderer::createBasePigModel);
        ModelLayerRegistry.registerModelLayer(ClientRabbitRenderer.RABBIT_LOCATION, ClientRabbitRenderer::createBaseRabbitLayer);
        ModelLayerRegistry.registerModelLayer(ClientSalmonRenderer.SALMON_LOCATION, ClientSalmonModel::getTexturedModelData);
        ModelLayerRegistry.registerModelLayer(ClientSnifferRenderer.SNIFFER_LOCATION, SnifferModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(ClientSnowGolemRenderer.SNOW_GOLEM, SnowGolemModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(ClientSquidRenderer.SQUID_LOCATION, SquidModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(ClientStriderRenderer.STRIDER_LOCATION, AdultStriderModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(ClientTadpoleRenderer.TADPOLE_LOCATION, TadpoleModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(ClientTurtleRenderer.TURTLE_LOCATION, AdultTurtleModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(ClientVillagerRenderer.VILLAGER_LOCATION, ClientVillagerRenderer::createBaseVillagerLayer);
        ModelLayerRegistry.registerModelLayer(ClientWanderingTraderRenderer.WANDERING_TRADER_LOCATION, ClientVillagerRenderer::createBaseVillagerLayer);
        ModelLayerRegistry.registerModelLayer(ClientBeeRenderer.BEE_LOCATION, AdultBeeModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(ClientCaveSpiderRenderer.CAVE_SPIDER_LOCATION, ClientCaveSpiderModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(ClientDolphinRenderer.DOLPHIN_LOCATION, DolphinModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(ClientEndermanRenderer.ENDERMAN_LOCATION, EndermanModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(ClientFoxRenderer.FOX_LOCATION, ClientFoxModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(ClientGoatRenderer.GOAT_LOCATION, ClientGoatModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(ClientIronGolemRenderer.IRON_GOLEM_LOCATION, IronGolemModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(ClientLlamaRenderer.LLAMA_LOCATION, ClientLlamaRenderer::createLlamaLayer);
        ModelLayerRegistry.registerModelLayer(ClientNautilusRenderer.NAUTILUS_LOCATION, NautilusModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(ClientPandaRenderer.PANDA_LOCAITON, PandaModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(ClientPiglinRenderer.PIGLIN_LOCATION, ClientPiglinRenderer::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(ClientPolarBearRenderer.POLAR_BEAR_LOCATION, ClientPolarBearRenderer::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(ClientPufferFishRenderer.PUFFERFISH_LOCATION, PufferfishBigModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(ClientSpiderRenderer.SPIDER_LOCATION, SpiderModel::createSpiderBodyLayer);
        ModelLayerRegistry.registerModelLayer(ClientWolfRenderer.WOLF_LOCATION, ClientWolfRenderer::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(ClientElderGuardianRenderer.ELDER_GUARDIAN_LOCATION, GuardianModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(ClientHappyGhastRenderer.GHAST_LOCATION, ClientHappyGhastRenderer::createGhastBodyLayer);
        ModelLayerRegistry.registerModelLayer(ClientBlazeRenderer.BLAZE_LOCATION, BlazeModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(ClientBoggedRenderer.BOGGED_LOCATION, BoggedModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(ClientBreezeRenderer.BREEZE_LOCATION, BreezeModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(ClientCreakingRenderer.CREAKING_LOCATION, CreakingModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(ClientCreeperRenderer.CREEPER_LOCATION, ClientCreeperRenderer::createBaseCreeperLayer);
        ModelLayerRegistry.registerModelLayer(ClientDrownedRenderer.DROWNED_LOCATION, ClientDrownedRenderer::createBaseDrownedLayer);
        ModelLayerRegistry.registerModelLayer(ClientEndermiteRenderer.ENDERMITE_LOCATION, EndermiteModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(ClientEvokerRenderer.EVOKER_LOCATION, ClientEvokerModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(ClientGhastRenderer.GHAST_LOCATION, GhastModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(ClientGuardianRenderer.GUARDIAN_LOCATION, GuardianModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(ClientHoglinRenderer.HOGLIN_LOCATION, ClientHoglinModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(ClientHuskRenderer.HUSK_LOCATION, ClientZombieRenderer::createBaseZombieLayer);
        ModelLayerRegistry.registerModelLayer(ClientMagmaCubeRenderer.MAGMA_CUBE_LOCATION, MagmaCubeModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(ClientParchedRenderer.PARCHED_LOCATION, SkeletonModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(ClientPhantomRenderer.PHANTOM_LOCATION, PhantomModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(ClientPillagerRenderer.PILLAGER_LOCATION, ClientPillagerModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(ClientRavagerRenderer.RAVAGER_LOCATION, RavagerModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(ClientShulkerRenderer.SHULKER_LOCATION, ShulkerModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(ClientSilverfishRenderer.SILVERFISH_LOCATION, SilverfishModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(ClientSkeletonRenderer.SKELETON_LOCATION, SkeletonModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(ClientSlimeRenderer.SLIME_LOCATION, SlimeModel::createInnerBodyLayer);
        ModelLayerRegistry.registerModelLayer(ClientStrayRenderer.STRAY_LOCATION, SkeletonModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(ClientVexRenderer.VEX_LOCATION, VexModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(ClientVindicatorRenderer.VINDICATOR_LOCATION, ClientEvokerModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(ClientWardenRenderer.WARDEN_LOCATION, WardenModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(ClientWitchRenderer.WITCH_LOCATION, WitchModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(ClientWitherSkeletonRenderer.WITHER_SKELETON_LOCATION, SkeletonModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(ClientZombieRenderer.ZOMBIE_LOCATION, ClientZombieRenderer::createBaseZombieLayer);
        ModelLayerRegistry.registerModelLayer(ClientZombieVillagerRenderer.ZOMBIE_VILLAGER_LOCATION, ClientZombieVillagerModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(ClientEnderDragonRenderer.ENDER_DRAGON_LOCATION, EnderDragonModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(ClientWitherRenderer.WITHER_LOCATION, ClientWitherRenderer::createBaseWitherLayer);
        ModelLayerRegistry.registerModelLayer(AngryGhastRenderer.ANGRY_GHAST_LOCATION, GhastModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(BatatoRenderer.BATATO_LOCAITON, BatatoModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(DiamondChickenRenderer.DIAMOND_CHICKEN_LOCATION, ClientChickenModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(LoveGolemRenderer.LOVE_GOLEM_LOCATION, IronGolemModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(MegaSpudRenderer.MEGA_SPUD_LOCATION, MegaSpudModel::createInnerBodyLayer);
        ModelLayerRegistry.registerModelLayer(MegaSpudOuterLayer.MEGA_SPUD_OUTER_LOCATION, MegaSpudModel::createOuterBodyLayer);
        ModelLayerRegistry.registerModelLayer(MoonCowRenderer.MOON_COW_LOCATION, LegacyCowModel::createLegacyCowModel);
        ModelLayerRegistry.registerModelLayer(NerdCreeperRenderer.NERD_CREEPER_LOCATION, ClientCreeperRenderer::createBaseCreeperLayer);
        ModelLayerRegistry.registerModelLayer(PinkWitherRenderer.PINK_WITHER_LOCATION, ClientWitherRenderer::createBaseWitherLayer);
        ModelLayerRegistry.registerModelLayer(PlaguewhaleRenderer.PLAGUEWHALE_LOCATION, ToxifinSlabModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(PoisonousPotatoZombieRenderer.POISONOUS_POTATO_ZOMBIE_LOCATION, ClientZombieRenderer::createBaseZombieLayer);
        ModelLayerRegistry.registerModelLayer(PotatoHuskRenderer.POTATO_HUSK_LOCATION, ClientZombieRenderer::createBaseZombieLayer);
        ModelLayerRegistry.registerModelLayer(RayTracingRenderer.RAY_TRACING_LOCATION, RayTracingRenderer::createBasePlayerBodyLayer);
        ModelLayerRegistry.registerModelLayer(RedstoneBugRenderer.REDSTONE_BUG_LOCATION, SilverfishModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(SmilingCreeperRenderer.SMILING_CREEPER_LOCATION, ClientCreeperRenderer::createBaseCreeperLayer);
        ModelLayerRegistry.registerModelLayer(ToxifinRenderer.TOXIFIN_LOCATION, ToxifinSlabModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(TraitorRenderer.TRAITOR_LOCATION, ClientEvokerModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(DumboOctopusRenderer.DUMBO_OCTOPUS_LOCATION, DumboOctopusModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(KoiRenderer.KOI_LOCATION, KoiModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(StingrayRenderer.STINGRAY_LOCATION, StingrayModel::createBodyLayer);

        ClientLifecycleEvents.CLIENT_STARTED.register((mc) -> {
            LOGGER.info("PetsMod addons loaded:{}", ADDONS);
        });
    }

    /**
     * Registers the key binding and an {@code END_CLIENT_TICK} event to check if the key
     * is pressed
     */
    void createKeyBinding() {
        KeyMapping keyMapping = KeyMappingHelper.registerKeyMapping(new KeyMapping("Open Pets Menu", GLFW.GLFW_KEY_P, new KeyMapping.Category(Identifier.fromNamespaceAndPath(PetsInitializer.MOD_ID, "petsmod.keymapping"))));

        ClientTickEvents.END_CLIENT_TICK.register((client) -> {
            if (keyMapping.consumeClick()) {
                client.setScreen(PetsConfigScreen.getInstance().getModConfigScreenFactory().create(client.screen));
            }
        });
    }
}