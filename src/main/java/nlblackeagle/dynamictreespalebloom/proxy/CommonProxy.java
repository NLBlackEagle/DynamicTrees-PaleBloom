package nlblackeagle.dynamictreespalebloom.proxy;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.registry.GameRegistry;
import nlblackeagle.dynamictreespalebloom.event.CreakingHeartValidityHandler;
import nlblackeagle.dynamictreespalebloom.event.ReapingWillowAggressionHandler;
import nlblackeagle.dynamictreespalebloom.event.ReapingWillowDeathHandler;
import nlblackeagle.dynamictreespalebloom.event.ReapingWillowSpawnHandler;
import nlblackeagle.dynamictreespalebloom.event.RLCraftDregoraDisableHandler;
import nlblackeagle.dynamictreespalebloom.event.TreeGenCancelPaleGardenEventHandler;
import nlblackeagle.dynamictreespalebloom.worldgen.WorldGenPalePetals;
import nlblackeagle.dynamictreespalebloom.worldgen.WorldGenPalePumpkin;

public class CommonProxy {

    public void preInit() {
        MinecraftForge.EVENT_BUS.register(new CreakingHeartValidityHandler());
        MinecraftForge.EVENT_BUS.register(new RLCraftDregoraDisableHandler());
        MinecraftForge.EVENT_BUS.register(new ReapingWillowAggressionHandler());
        MinecraftForge.EVENT_BUS.register(new ReapingWillowSpawnHandler());
        MinecraftForge.EVENT_BUS.register(new ReapingWillowDeathHandler());
        MinecraftForge.TERRAIN_GEN_BUS.register(new TreeGenCancelPaleGardenEventHandler());
        GameRegistry.registerWorldGenerator(new WorldGenPalePetals(), 5);
        GameRegistry.registerWorldGenerator(new WorldGenPalePumpkin(), 5);
    }

    public void init() {
        ReapingWillowSpawnHandler.registerSpawns();
    }

    public void postInit() {
    }

}
