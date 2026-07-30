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


//todo: Talk with others on what to do with: (With the team and I guess here too)
// Pollenhead, natural growth? Make it always spew stuff around from a "Natural growth resin bulb" that has unlimited resin in it but when harvested poofs into a cloud of particles (unless harvested with a gardencrafted hoe perhaps? (nice progression)
// Incense thorns, natural growth with poison potion, can be harvested using gardencraft hoe.
// NightLight, natural growth, gives off light can hang from cave ceilings?

//todo: Pale Lung potion effect (nlblackeagle.dynamictreespalebloom.potion) -
// acts like Poison (non-lethal magic damage) but ticks at a configurable fraction
// of Poison's speed (0.25x by default) and renders white hearts on the hotbar.
// Done: on-death Pollenhead + Pale Lung spread (PaleLungDeathHandler), Reaping
// Willow's own Seed Bomb also spreads Pale Lung now (PaleLungSeedBomb), Respiration
// grants immunity (PaleLungImmunityHandler), Incense Thorns' Poison swapped to Pale
// Lung via a TileIncenseThorn#getPotion() redirect (TileIncenseThornMixin).
// Still open:
// - Pale-Lung deaths don't spawn a *real* EntitySeedBomb (only Reaping Willow's
//   constructor is confirmed) - so they get Pollenhead + Pale Lung spread, but not
//   Pale Bloom's own native Pale Moss spread / Creeper conversion. Tell me if
//   EntitySeedBomb has a constructor that takes a generic EntityLivingBase/thrower
//   and I'll wire that in too.
// - Not sure Pollenhead itself ever applies Poison/Wither directly - the "Wither
//   nearby non-pale creatures" behaviour we know about lives in EntitySeedBomb's
//   explosion, not Pollenhead. If Pollenhead does poison something on its own,
//   point me at where and I'll swap that too.

//todo: weird root comes from sucker nodule tree, make this apparent


