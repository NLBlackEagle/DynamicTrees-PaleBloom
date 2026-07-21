package nlblackeagle.dynamictreespalebloom.proxy;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.registry.GameRegistry;
import nlblackeagle.dynamictreespalebloom.event.CreakingHeartValidityHandler;
import nlblackeagle.dynamictreespalebloom.event.TreeGenCancelPaleGardenEventHandler;
import nlblackeagle.dynamictreespalebloom.worldgen.WorldGenPalePetals;
import nlblackeagle.dynamictreespalebloom.worldgen.WorldGenPalePumpkin;

public class CommonProxy {

    public void preInit() {
        MinecraftForge.EVENT_BUS.register(new CreakingHeartValidityHandler());
        MinecraftForge.TERRAIN_GEN_BUS.register(new TreeGenCancelPaleGardenEventHandler());
        GameRegistry.registerWorldGenerator(new WorldGenPalePetals(), 5);
        GameRegistry.registerWorldGenerator(new WorldGenPalePumpkin(), 5);
    }

    public void init() {
    }

    public void postInit() {
    }

}
