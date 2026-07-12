package io.github.aaronateataco.petsandpals;

import io.github.aaronateataco.petsandpals.rendering.vanilla.allay.ClientAllayRenderer;
import io.github.aaronateataco.petsandpals.rendering.vanilla.armadillo.ClientArmadilloRenderer;
import io.github.aaronateataco.petsandpals.rendering.vanilla.axolotl.ClientAxolotlRenderer;
import io.github.aaronateataco.petsandpals.rendering.vanilla.bat.ClientBatRenderer;
import io.github.aaronateataco.petsandpals.rendering.vanilla.bee.ClientBeeRenderer;
import io.github.aaronateataco.petsandpals.rendering.vanilla.blaze.ClientBlazeRenderer;
import io.github.aaronateataco.petsandpals.rendering.vanilla.bogged.ClientBoggedRenderer;
import io.github.aaronateataco.petsandpals.rendering.vanilla.breeze.ClientBreezeRenderer;
import io.github.aaronateataco.petsandpals.rendering.vanilla.camel.ClientCamelRenderer;
import io.github.aaronateataco.petsandpals.rendering.vanilla.cat.ClientCatRenderer;
import io.github.aaronateataco.petsandpals.rendering.vanilla.cavespider.ClientCaveSpiderModel;
import io.github.aaronateataco.petsandpals.rendering.vanilla.cavespider.ClientCaveSpiderRenderer;
import io.github.aaronateataco.petsandpals.rendering.vanilla.chicken.ClientChickenModel;
import io.github.aaronateataco.petsandpals.rendering.vanilla.chicken.ClientChickenRenderer;
import io.github.aaronateataco.petsandpals.rendering.vanilla.cod.ClientCodRenderer;
import io.github.aaronateataco.petsandpals.rendering.vanilla.coppergolem.ClientCopperGolemRenderer;
import io.github.aaronateataco.petsandpals.rendering.vanilla.cow.ClientCowModel;
import io.github.aaronateataco.petsandpals.rendering.vanilla.cow.ClientCowRenderer;
import io.github.aaronateataco.petsandpals.rendering.vanilla.creaking.ClientCreakingRenderer;
import io.github.aaronateataco.petsandpals.rendering.vanilla.creeper.ClientCreeperRenderer;
import io.github.aaronateataco.petsandpals.rendering.vanilla.dolphin.ClientDolphinRenderer;
import io.github.aaronateataco.petsandpals.rendering.vanilla.donkey.ClientDonkeyRenderer;
import io.github.aaronateataco.petsandpals.rendering.vanilla.drowned.ClientDrownedRenderer;
import io.github.aaronateataco.petsandpals.rendering.vanilla.elderguardian.ClientElderGuardianRenderer;
import io.github.aaronateataco.petsandpals.rendering.vanilla.enderdragon.ClientEnderDragonRenderer;
import io.github.aaronateataco.petsandpals.rendering.vanilla.enderman.ClientEndermanRenderer;
import io.github.aaronateataco.petsandpals.rendering.vanilla.endermite.ClientEndermiteRenderer;
import io.github.aaronateataco.petsandpals.rendering.vanilla.evoker.ClientEvokerModel;
import io.github.aaronateataco.petsandpals.rendering.vanilla.evoker.ClientEvokerRenderer;
import io.github.aaronateataco.petsandpals.rendering.vanilla.fox.ClientFoxModel;
import io.github.aaronateataco.petsandpals.rendering.vanilla.fox.ClientFoxRenderer;
import io.github.aaronateataco.petsandpals.rendering.vanilla.frog.ClientFrogRenderer;
import io.github.aaronateataco.petsandpals.rendering.vanilla.ghast.ClientGhastRenderer;
import io.github.aaronateataco.petsandpals.rendering.vanilla.goat.ClientGoatModel;
import io.github.aaronateataco.petsandpals.rendering.vanilla.goat.ClientGoatRenderer;
import io.github.aaronateataco.petsandpals.rendering.vanilla.guardian.ClientGuardianRenderer;
import io.github.aaronateataco.petsandpals.rendering.vanilla.happyghast.ClientHappyGhastRenderer;
import io.github.aaronateataco.petsandpals.rendering.vanilla.hoglin.ClientHoglinModel;
import io.github.aaronateataco.petsandpals.rendering.vanilla.hoglin.ClientHoglinRenderer;
import io.github.aaronateataco.petsandpals.rendering.vanilla.horse.ClientHorseRenderer;
import io.github.aaronateataco.petsandpals.rendering.vanilla.husk.ClientHuskRenderer;
import io.github.aaronateataco.petsandpals.rendering.vanilla.irongolem.ClientIronGolemRenderer;
import io.github.aaronateataco.petsandpals.rendering.vanilla.llama.ClientLlamaRenderer;
import io.github.aaronateataco.petsandpals.rendering.vanilla.magmacube.ClientMagmaCubeRenderer;
import io.github.aaronateataco.petsandpals.rendering.vanilla.mooshroom.ClientMooshroomRenderer;
import io.github.aaronateataco.petsandpals.rendering.vanilla.nautilus.ClientNautilusRenderer;
import io.github.aaronateataco.petsandpals.rendering.vanilla.panda.ClientPandaRenderer;
import io.github.aaronateataco.petsandpals.rendering.vanilla.parched.ClientParchedRenderer;
import io.github.aaronateataco.petsandpals.rendering.vanilla.parrot.ClientParrotRenderer;
import io.github.aaronateataco.petsandpals.rendering.vanilla.phantom.ClientPhantomRenderer;
import io.github.aaronateataco.petsandpals.rendering.vanilla.pig.ClientPigRenderer;
import io.github.aaronateataco.petsandpals.rendering.vanilla.piglin.ClientPiglinRenderer;
import io.github.aaronateataco.petsandpals.rendering.vanilla.pillager.ClientPillagerModel;
import io.github.aaronateataco.petsandpals.rendering.vanilla.pillager.ClientPillagerRenderer;
import io.github.aaronateataco.petsandpals.rendering.vanilla.polarbear.ClientPolarBearRenderer;
import io.github.aaronateataco.petsandpals.rendering.vanilla.pufferfish.ClientPufferFishRenderer;
import io.github.aaronateataco.petsandpals.rendering.vanilla.rabbit.ClientRabbitModel;
import io.github.aaronateataco.petsandpals.rendering.vanilla.rabbit.ClientRabbitRenderer;
import io.github.aaronateataco.petsandpals.rendering.vanilla.ravager.ClientRavagerRenderer;
import io.github.aaronateataco.petsandpals.rendering.vanilla.salmon.ClientSalmonModel;
import io.github.aaronateataco.petsandpals.rendering.vanilla.salmon.ClientSalmonRenderer;
import io.github.aaronateataco.petsandpals.rendering.vanilla.sheep.ClientSheepModel;
import io.github.aaronateataco.petsandpals.rendering.vanilla.sheep.ClientSheepRenderer;
import io.github.aaronateataco.petsandpals.rendering.vanilla.sheep.ClientSheepWoolLayer;
import io.github.aaronateataco.petsandpals.rendering.vanilla.shulker.ClientShulkerRenderer;
import io.github.aaronateataco.petsandpals.rendering.vanilla.silverfish.ClientSilverfishRenderer;
import io.github.aaronateataco.petsandpals.rendering.vanilla.skeleton.ClientSkeletonRenderer;
import io.github.aaronateataco.petsandpals.rendering.vanilla.slime.ClientSlimeRenderer;
import io.github.aaronateataco.petsandpals.rendering.vanilla.sniffer.ClientSnifferRenderer;
import io.github.aaronateataco.petsandpals.rendering.vanilla.snowgolem.ClientSnowGolemRenderer;
import io.github.aaronateataco.petsandpals.rendering.vanilla.spider.ClientSpiderRenderer;
import io.github.aaronateataco.petsandpals.rendering.vanilla.squid.ClientSquidRenderer;
import io.github.aaronateataco.petsandpals.rendering.vanilla.stray.ClientStrayRenderer;
import io.github.aaronateataco.petsandpals.rendering.vanilla.strider.ClientStriderRenderer;
import io.github.aaronateataco.petsandpals.rendering.vanilla.sulfur_cube.ClientSulfurCubeRenderer;
import io.github.aaronateataco.petsandpals.rendering.vanilla.tadpole.ClientTadpoleRenderer;
import io.github.aaronateataco.petsandpals.rendering.vanilla.turtle.ClientTurtleRenderer;
import io.github.aaronateataco.petsandpals.rendering.vanilla.vex.ClientVexRenderer;
import io.github.aaronateataco.petsandpals.rendering.vanilla.villager.ClientVillagerRenderer;
import io.github.aaronateataco.petsandpals.rendering.vanilla.vindicator.ClientVindicatorRenderer;
import io.github.aaronateataco.petsandpals.rendering.vanilla.wanderingtrader.ClientWanderingTraderRenderer;
import io.github.aaronateataco.petsandpals.rendering.vanilla.warden.ClientWardenRenderer;
import io.github.aaronateataco.petsandpals.rendering.vanilla.witch.ClientWitchRenderer;
import io.github.aaronateataco.petsandpals.rendering.vanilla.wither.ClientWitherRenderer;
import io.github.aaronateataco.petsandpals.rendering.vanilla.witherskeleton.ClientWitherSkeletonRenderer;
import io.github.aaronateataco.petsandpals.rendering.vanilla.wolf.ClientWolfRenderer;
import io.github.aaronateataco.petsandpals.rendering.vanilla.zombie.ClientZombieRenderer;
import io.github.aaronateataco.petsandpals.rendering.vanilla.zombievillager.ClientZombieVillagerModel;
import io.github.aaronateataco.petsandpals.rendering.vanilla.zombievillager.ClientZombieVillagerRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
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
import net.minecraft.client.model.monster.slime.SulfurCubeModel;
import net.minecraft.client.model.monster.spider.SpiderModel;
import net.minecraft.client.model.monster.strider.AdultStriderModel;
import net.minecraft.client.model.monster.vex.VexModel;
import net.minecraft.client.model.monster.warden.WardenModel;
import net.minecraft.client.model.monster.witch.WitchModel;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.SulfurCubeRenderer;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

import static io.github.aaronateataco.petsandpals.PetsInitializer.LOGGER;

/**
 * Another {@code initializer} class. This class does a couple of things:
 * <p> - Assigns renderers to the entities defined in {@link PetsInitializer}
 * <p> - Bakes models into the layers
 * <p> - Creates the keybind to open the config screen, by default {@code p}
 * <p> Suppresses: Deprecation warnings, as {@link EntityRenderers} is marked as
 * {@code deprecated}. I will likely have to find a suitable replacement sometime, but for now,
 * suppressing the warnings will work.
 *
 * @see PetsInitializer
 * @see Central
 */
public class PetsClientInitializer implements ClientModInitializer {

    public static List<String> ADDONS = new ArrayList<>();

    /**
     * Misc rendering stuff
     */
    @Override
    public void onInitializeClient() {

        this.createKeyBinding();

        EntityRenderers.register(PetsInitializer.PET_ORB,
                io.github.aaronateataco.petsandpals.rendering.PetOrbRenderer::new);
        EntityRenderers.register(PetsInitializer.PET_RAFT,
                context -> (net.minecraft.client.renderer.entity.EntityRenderer) new io.github.aaronateataco.petsandpals.rendering.PetRaftRenderer(context));
        // Vanilla falling-block renderer draws the spawn-animation ghost block for free.
        EntityRenderers.register(PetsInitializer.PET_DWELLING,
                context -> (net.minecraft.client.renderer.entity.EntityRenderer) new net.minecraft.client.renderer.entity.FallingBlockRenderer(context));
        EntityRenderers.register(PetsInitializer.SHEEP, ClientSheepRenderer::new);
        EntityRenderers.register(PetsInitializer.CAT, ClientCatRenderer::new);
        EntityRenderers.register(PetsInitializer.ALLAY, ClientAllayRenderer::new);
        EntityRenderers.register(PetsInitializer.ARMADILLO, ClientArmadilloRenderer::new);
        EntityRenderers.register(PetsInitializer.AXOLOTL, ClientAxolotlRenderer::new);
        EntityRenderers.register(PetsInitializer.BAT, ClientBatRenderer::new);
        EntityRenderers.register(PetsInitializer.CAMEL, ClientCamelRenderer::new);
        EntityRenderers.register(PetsInitializer.CHICKEN, ClientChickenRenderer::new);
        EntityRenderers.register(PetsInitializer.COD, ClientCodRenderer::new);
        EntityRenderers.register(PetsInitializer.COPPER_GOLEM, ClientCopperGolemRenderer::new);
        EntityRenderers.register(PetsInitializer.COW, ClientCowRenderer::new);
        EntityRenderers.register(PetsInitializer.DONKEY, ClientDonkeyRenderer::new);
        EntityRenderers.register(PetsInitializer.FROG, ClientFrogRenderer::new);
        EntityRenderers.register(PetsInitializer.HORSE, ClientHorseRenderer::new);
        EntityRenderers.register(PetsInitializer.MOOSHROOM, ClientMooshroomRenderer::new);
        EntityRenderers.register(PetsInitializer.PARROT, ClientParrotRenderer::new);
        EntityRenderers.register(PetsInitializer.PIG, ClientPigRenderer::new);
        EntityRenderers.register(PetsInitializer.RABBIT, ClientRabbitRenderer::new);
        EntityRenderers.register(PetsInitializer.SALMON, ClientSalmonRenderer::new);
        EntityRenderers.register(PetsInitializer.SNIFFER, ClientSnifferRenderer::new);
        EntityRenderers.register(PetsInitializer.SNOW_GOLEM, ClientSnowGolemRenderer::new);
        EntityRenderers.register(PetsInitializer.SQUID, ClientSquidRenderer::new);
        EntityRenderers.register(PetsInitializer.STRIDER, ClientStriderRenderer::new);
        EntityRenderers.register(PetsInitializer.TADPOLE, ClientTadpoleRenderer::new);
        EntityRenderers.register(PetsInitializer.TURTLE, ClientTurtleRenderer::new);
        EntityRenderers.register(PetsInitializer.VILLAGER, ClientVillagerRenderer::new);
        EntityRenderers.register(PetsInitializer.WANDERING_TRADER, ClientWanderingTraderRenderer::new);
        EntityRenderers.register(PetsInitializer.BEE, ClientBeeRenderer::new);
        EntityRenderers.register(PetsInitializer.CAVE_SPIDER, ClientCaveSpiderRenderer::new);
        EntityRenderers.register(PetsInitializer.DOLPHIN, ClientDolphinRenderer::new);
        EntityRenderers.register(PetsInitializer.ENDERMAN, ClientEndermanRenderer::new);
        EntityRenderers.register(PetsInitializer.FOX, ClientFoxRenderer::new);
        EntityRenderers.register(PetsInitializer.GOAT, ClientGoatRenderer::new);
        EntityRenderers.register(PetsInitializer.IRON_GOLEM, ClientIronGolemRenderer::new);
        EntityRenderers.register(PetsInitializer.LLAMA, ClientLlamaRenderer::new);
        EntityRenderers.register(PetsInitializer.NAUTILUS, ClientNautilusRenderer::new);
        EntityRenderers.register(PetsInitializer.PANDA, ClientPandaRenderer::new);
        EntityRenderers.register(PetsInitializer.PIGLIN, ClientPiglinRenderer::new);
        EntityRenderers.register(PetsInitializer.POLAR_BEAR, ClientPolarBearRenderer::new);
        EntityRenderers.register(PetsInitializer.PUFFERFISH, ClientPufferFishRenderer::new);
        EntityRenderers.register(PetsInitializer.SPIDER, ClientSpiderRenderer::new);
        EntityRenderers.register(PetsInitializer.WOLF, ClientWolfRenderer::new);
        EntityRenderers.register(PetsInitializer.ELDER_GUARDIAN_COOKIE, ClientElderGuardianRenderer::new);
        EntityRenderers.register(PetsInitializer.HAPPY_GHAST, ClientHappyGhastRenderer::new);
        EntityRenderers.register(PetsInitializer.BLAZE, ClientBlazeRenderer::new);
        EntityRenderers.register(PetsInitializer.BOGGED, ClientBoggedRenderer::new);
        EntityRenderers.register(PetsInitializer.BREEZE, ClientBreezeRenderer::new);
        EntityRenderers.register(PetsInitializer.CREAKING, ClientCreakingRenderer::new);
        EntityRenderers.register(PetsInitializer.CREEPER, ClientCreeperRenderer::new);
        EntityRenderers.register(PetsInitializer.DROWNED, ClientDrownedRenderer::new);
        EntityRenderers.register(PetsInitializer.ENDERMITE, ClientEndermiteRenderer::new);
        EntityRenderers.register(PetsInitializer.EVOKER, ClientEvokerRenderer::new);
        EntityRenderers.register(PetsInitializer.GHAST, ClientGhastRenderer::new);
        EntityRenderers.register(PetsInitializer.GUARDIAN, ClientGuardianRenderer::new);
        EntityRenderers.register(PetsInitializer.HOGLIN, ClientHoglinRenderer::new);
        EntityRenderers.register(PetsInitializer.HUSK, ClientHuskRenderer::new);
        EntityRenderers.register(PetsInitializer.MAGMA_CUBE, ClientMagmaCubeRenderer::new);
        EntityRenderers.register(PetsInitializer.PARCHED, ClientParchedRenderer::new);
        EntityRenderers.register(PetsInitializer.PHANTOM, ClientPhantomRenderer::new);
        EntityRenderers.register(PetsInitializer.PILLAGER, ClientPillagerRenderer::new);
        EntityRenderers.register(PetsInitializer.RAVAGER, ClientRavagerRenderer::new);
        EntityRenderers.register(PetsInitializer.SHULKER, ClientShulkerRenderer::new);
        EntityRenderers.register(PetsInitializer.SILVERFISH, ClientSilverfishRenderer::new);
        EntityRenderers.register(PetsInitializer.SKELETON, ClientSkeletonRenderer::new);
        EntityRenderers.register(PetsInitializer.SLIME, ClientSlimeRenderer::new);
        EntityRenderers.register(PetsInitializer.STRAY, ClientStrayRenderer::new);
        EntityRenderers.register(PetsInitializer.VEX, ClientVexRenderer::new);
        EntityRenderers.register(PetsInitializer.VINDICATOR, ClientVindicatorRenderer::new);
        EntityRenderers.register(PetsInitializer.WARDEN, ClientWardenRenderer::new);
        EntityRenderers.register(PetsInitializer.WITCH, ClientWitchRenderer::new);
        EntityRenderers.register(PetsInitializer.WITHER_SKELETON, ClientWitherSkeletonRenderer::new);
        EntityRenderers.register(PetsInitializer.ZOMBIE, ClientZombieRenderer::new);
        EntityRenderers.register(PetsInitializer.ZOMBIE_VILLAGER, ClientZombieVillagerRenderer::new);
        EntityRenderers.register(PetsInitializer.ENDER_DRAGON, ClientEnderDragonRenderer::new);
        EntityRenderers.register(PetsInitializer.WITHER, ClientWitherRenderer::new);
        EntityRenderers.register(PetsInitializer.SULPHUR_CUBE, ClientSulfurCubeRenderer::new);

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
        ModelLayerRegistry.registerModelLayer(ClientSulfurCubeRenderer.SULFUR_CUBE_LOCATION, SulfurCubeModel::createOuterBodyLayer);

        ClientLifecycleEvents.CLIENT_STARTED.register((mc) -> {
            LOGGER.info("Pets&Pals addons loaded:{}", ADDONS);
        });
    }

    /**
     * Registers the key binding and an {@code END_CLIENT_TICK} event to check if the key
     * is pressed
     */
    void createKeyBinding() {
        KeyMapping keyMapping = KeyMappingHelper.registerKeyMapping(new KeyMapping("Open Pets Menu", GLFW.GLFW_KEY_SEMICOLON, new KeyMapping.Category(Identifier.fromNamespaceAndPath(PetsInitializer.MOD_ID, "petsandpals.keymapping"))));

        ClientTickEvents.END_CLIENT_TICK.register((client) -> {
            if (keyMapping.consumeClick()) {
                client.gui.setScreen(PetsConfigScreen.getInstance().getModConfigScreenFactory().create(client.gui.screen()));
            }
        });
    }
}