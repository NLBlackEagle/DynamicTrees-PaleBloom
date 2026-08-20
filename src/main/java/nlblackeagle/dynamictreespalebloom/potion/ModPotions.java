package nlblackeagle.dynamictreespalebloom.potion;

import net.minecraft.init.Items;
import net.minecraft.init.PotionTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;
import nlblackeagle.dynamictreespalebloom.DynamicTreesPaleBloom;

import com.sirsquidly.palebloom.init.JTPGItems;

@Mod.EventBusSubscriber(modid = DynamicTreesPaleBloom.MODID)
public class ModPotions {

    public static Potion paleLung;
    public static PotionType paleLungType;
    public static PotionType paleLungTypeLong;

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

        // 1800 ticks (1:30), same amplifier — matches vanilla Long Poison's 2x-duration convention.
        paleLungTypeLong = new PotionType("pale_lung", new PotionEffect(paleLung, 1800));
        paleLungTypeLong.setRegistryName(new ResourceLocation(DynamicTreesPaleBloom.MODID, "pale_lung_long"));
        registry.register(paleLungTypeLong);
    }

    @SubscribeEvent
    public static void registerBrewingRecipes(final RegistryEvent.Register<IRecipe> event) {
        // Registered per-container so each of Potion/Splash Potion/Lingering Potion has its
        // own explicit brewing path, rather than relying on vanilla's generic gunpowder/dragon's
        // breath container conversion to carry a modded PotionType across item types.
        for (Item container : new Item[]{Items.POTIONITEM, Items.SPLASH_POTION, Items.LINGERING_POTION}) {
            // Awkward Potion + Resin Clump -> Pale Lung Potion.
            BrewingRecipeRegistry.addRecipe(
                    PotionUtils.addPotionToItemStack(new ItemStack(container), PotionTypes.AWKWARD),
                    new ItemStack(JTPGItems.RESIN_CLUMP),
                    PotionUtils.addPotionToItemStack(new ItemStack(container), paleLungType));

            // Pale Lung Potion + Redstone -> Long Pale Lung Potion.
            BrewingRecipeRegistry.addRecipe(
                    PotionUtils.addPotionToItemStack(new ItemStack(container), paleLungType),
                    new ItemStack(Items.REDSTONE),
                    PotionUtils.addPotionToItemStack(new ItemStack(container), paleLungTypeLong));
        }

        // Gunpowder (Potion -> Splash Potion) and Dragon's Breath (Splash Potion -> Lingering
        // Potion) container conversions, explicit per tier for the same reason as above.
        for (PotionType tier : new PotionType[]{paleLungType, paleLungTypeLong}) {
            BrewingRecipeRegistry.addRecipe(
                    PotionUtils.addPotionToItemStack(new ItemStack(Items.POTIONITEM), tier),
                    new ItemStack(Items.GUNPOWDER),
                    PotionUtils.addPotionToItemStack(new ItemStack(Items.SPLASH_POTION), tier));

            BrewingRecipeRegistry.addRecipe(
                    PotionUtils.addPotionToItemStack(new ItemStack(Items.SPLASH_POTION), tier),
                    new ItemStack(Items.DRAGON_BREATH),
                    PotionUtils.addPotionToItemStack(new ItemStack(Items.LINGERING_POTION), tier));
        }
    }
}
