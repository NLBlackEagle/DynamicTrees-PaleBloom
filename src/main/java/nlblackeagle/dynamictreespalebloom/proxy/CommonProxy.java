package nlblackeagle.dynamictreespalebloom.proxy;


import net.minecraftforge.common.MinecraftForge;
import nlblackeagle.dynamictreespalebloom.cells.CellKits;

public class CommonProxy {
	
	public void preInit() {
        CellKits.init();
        MinecraftForge.EVENT_BUS.register(new nlblackeagle.dynamictreespalebloom.handler.CreakingHeartValidityHandler());
	}
	
	public void init() {
	}
	
	public void postInit() {
	}
	
}
