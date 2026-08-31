package nlblackeagle.dynamictreespalebloom.proxy;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.registry.GameRegistry;
import nlblackeagle.dynamictreespalebloom.event.CreakingHeartValidityHandler;
import nlblackeagle.dynamictreespalebloom.event.PaleLungDeathHandler;
import nlblackeagle.dynamictreespalebloom.event.PaleLungImmunityHandler;
import nlblackeagle.dynamictreespalebloom.event.PaleLungSeedBombTickHandler;
import nlblackeagle.dynamictreespalebloom.event.PaleLungWeaponHandler;
import nlblackeagle.dynamictreespalebloom.event.ReapingWillowAggressionHandler;
import nlblackeagle.dynamictreespalebloom.event.ReapingWillowDeathHandler;
import nlblackeagle.dynamictreespalebloom.event.ReapingWillowDropsHandler;
import nlblackeagle.dynamictreespalebloom.event.ReapingWillowSpawnHandler;
import nlblackeagle.dynamictreespalebloom.event.RLCraftDregoraDisableHandler;
import nlblackeagle.dynamictreespalebloom.event.TreeGenCancelPaleGardenEventHandler;
import nlblackeagle.dynamictreespalebloom.worldgen.WorldGenCaveFlora;
import nlblackeagle.dynamictreespalebloom.worldgen.WorldGenCaveHangingMoss;
import nlblackeagle.dynamictreespalebloom.worldgen.WorldGenPalePetals;
import nlblackeagle.dynamictreespalebloom.worldgen.WorldGenPalePumpkin;
import nlblackeagle.dynamictreespalebloom.worldgen.UndergroundSoilRegistration;
import nlblackeagle.dynamictreespalebloom.worldgen.WorldGenUndergroundTrees;

public class CommonProxy {

    public void preInit() {
        MinecraftForge.EVENT_BUS.register(new CreakingHeartValidityHandler());
        MinecraftForge.EVENT_BUS.register(new RLCraftDregoraDisableHandler());
        MinecraftForge.EVENT_BUS.register(new ReapingWillowAggressionHandler());
        MinecraftForge.EVENT_BUS.register(new ReapingWillowSpawnHandler());
        MinecraftForge.EVENT_BUS.register(new ReapingWillowDeathHandler());
        MinecraftForge.EVENT_BUS.register(new ReapingWillowDropsHandler());
        MinecraftForge.EVENT_BUS.register(new PaleLungDeathHandler());
        MinecraftForge.EVENT_BUS.register(new PaleLungImmunityHandler());
        MinecraftForge.EVENT_BUS.register(new PaleLungWeaponHandler());
        MinecraftForge.EVENT_BUS.register(new PaleLungSeedBombTickHandler());
        MinecraftForge.TERRAIN_GEN_BUS.register(new TreeGenCancelPaleGardenEventHandler());
        GameRegistry.registerWorldGenerator(new WorldGenPalePetals(), 5);
        GameRegistry.registerWorldGenerator(new WorldGenPalePumpkin(), 5);
        GameRegistry.registerWorldGenerator(new WorldGenUndergroundTrees(), 5);
        GameRegistry.registerWorldGenerator(new WorldGenCaveHangingMoss(), 5);
        GameRegistry.registerWorldGenerator(new WorldGenCaveFlora(), 5);
    }

    public void init() {
        ReapingWillowSpawnHandler.registerSpawns();
        UndergroundSoilRegistration.register();
    }

    public void postInit() {
    }

}
