package nlblackeagle.dynamictreespalebloom.potion;

import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;
import nlblackeagle.dynamictreespalebloom.DynamicTreesPaleBloom;

@Mod.EventBusSubscriber(modid = DynamicTreesPaleBloom.MODID)
public class ModPotions {

    public static Potion paleLung;
    public static PotionType paleLungType;

    @SubscribeEvent
    public static void registerPotions(final RegistryEvent.Register<Potion> event) {
        IForgeRegistry<Potion> registry = event.getRegistry();

        paleLung = new PotionPaleLung();
        paleLung.setRegistryName(new ResourceLocation(DynamicTreesPaleBloom.MODID, "pale_lung"));
        registry.register(paleLung);
    }

    @SubscribeEvent
    public static void registerPotionTypes(final RegistryEvent.Register<PotionType> event) {
        IForgeRegistry<PotionType> registry = event.getRegistry();

        // 900 ticks (45s) base duration, matching vanilla's base Potion of Poison.
        paleLungType = new PotionType("pale_lung", new PotionEffect(paleLung, 900));
        paleLungType.setRegistryName(new ResourceLocation(DynamicTreesPaleBloom.MODID, "pale_lung"));
        registry.register(paleLungType);
    }
}
