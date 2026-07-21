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
//todo: make pale-oak trees only have creaking hearts if grown naturally.

