package nlblackeagle.dynamictreespalebloom.proxy;

import net.minecraftforge.common.MinecraftForge;
import nlblackeagle.dynamictreespalebloom.cells.CellKits;
import nlblackeagle.dynamictreespalebloom.event.CreakingHeartValidityHandler;
import nlblackeagle.dynamictreespalebloom.event.TreeGenCancelPaleGardenEventHandler;

public class CommonProxy {

    public void preInit() {
        CellKits.init();
        MinecraftForge.EVENT_BUS.register(new CreakingHeartValidityHandler());
        MinecraftForge.TERRAIN_GEN_BUS.register(new TreeGenCancelPaleGardenEventHandler());
    }

    public void init() {
    }

    public void postInit() {
    }

}
