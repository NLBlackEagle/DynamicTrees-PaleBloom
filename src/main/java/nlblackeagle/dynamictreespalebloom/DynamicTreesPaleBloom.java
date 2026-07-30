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
//todo: the config option B:"Replace Incense Thorns Poison"=true should actually be something like: "Replace the poison effect the pollenhead and incense thorn gives for pale lung": true
//todo: pollenhead area affect still gives off poison effect instead of pale lung. (config options are on for this)
//todo: incense thorns that normally give an area poison effect still give poison instead of pale lung (config options are on for this) B:"Replace Incense Thorns Poison"=true
//todo: weird root comes from sucker nodule tree, make this apparent
//todo: growing a lot of trees seems to lag, run spark while doing this so we know what is causing it.


