package nlblackeagle.dynamictreespalebloom;

import com.ferreusveritas.dynamictrees.ModConstants;

import nlblackeagle.dynamictreespalebloom.proxy.CommonProxy;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid= DynamicTreesPaleBloom.MODID, name= DynamicTreesPaleBloom.NAME, dependencies = DynamicTreesPaleBloom.DEPENDENCIES)
public class  DynamicTreesPaleBloom {
	
	public static final String MODID = "dynamictreespalebloom";
	public static final String NAME = "Dynamic Trees for Pale Bloom";
	public static final String DEPENDENCIES = "required-after:" + ModConstants.DYNAMICTREES_LATEST
			+ ";required-after:palebloom;required-after:fermiumbooter;required-after:geckolib3";
	
	@Mod.Instance
	public static DynamicTreesPaleBloom instance;
	
	@SidedProxy(clientSide = "nlblackeagle.dynamictreespalebloom.proxy.ClientProxy", serverSide = "nlblackeagle.dynamictreespalebloom.proxy.CommonProxy") //com.
	public static CommonProxy proxy;
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		proxy.preInit();
	}
	
	@EventHandler
	public void init(FMLInitializationEvent event) {
		proxy.init();
	}
	
	@EventHandler
	public void postInit(FMLPostInitializationEvent event) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		proxy.postInit();
	}
	
}

//todo: Check sounds of the mod, they should work for the DT compat too.
//todo: make sure the trees drop the correct items.
//todo: create a config and expose the rarity at which creaking hearts generate in pale trees
//todo: add hanging moss to the trees
//todo: make pale-oak trees only have creaking hearts if grown naturally.
//todo: add blossoming pale-oak trees and make them appear older, thicker roots and bigger.
// also make them have a rare chance of generating a creaking-heart, like 10% again and make
// it a config as well.
//todo: Add the Peeping Birch (Eye-full Birch variant. Generates taller,
// with occasional cut-off stumps where branches would've been at)
