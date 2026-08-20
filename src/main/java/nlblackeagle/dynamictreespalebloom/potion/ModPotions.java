package nlblackeagle.dynamictreespalebloom.potion;

import net.minecraft.init.Items;
import net.minecraft.init.PotionTypes;
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
    public static PotionType paleLungTypeStrong;

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

        // 432 ticks (21.6s), amplifier 1 — matches vanilla Strong Poison exactly.
        paleLungTypeStrong = new PotionType("pale_lung", new PotionEffect(paleLung, 432, 1));
        paleLungTypeStrong.setRegistryName(new ResourceLocation(DynamicTreesPaleBloom.MODID, "pale_lung_strong"));
        registry.register(paleLungTypeStrong);
    }

    @SubscribeEvent
    public static void registerBrewingRecipes(final RegistryEvent.Register<IRecipe> event) {
        // Awkward Potion + Resin Clump -> Pale Lung Potion.
        BrewingRecipeRegistry.addRecipe(
                PotionUtils.addPotionToItemStack(new ItemStack(Items.POTIONITEM), PotionTypes.AWKWARD),
                new ItemStack(JTPGItems.RESIN_CLUMP),
                PotionUtils.addPotionToItemStack(new ItemStack(Items.POTIONITEM), paleLungType));

        // Pale Lung Potion + Redstone -> Long Pale Lung Potion.
        BrewingRecipeRegistry.addRecipe(
                PotionUtils.addPotionToItemStack(new ItemStack(Items.POTIONITEM), paleLungType),
                new ItemStack(Items.REDSTONE),
                PotionUtils.addPotionToItemStack(new ItemStack(Items.POTIONITEM), paleLungTypeLong));

        // Pale Lung Potion + Glowstone Dust -> Strong Pale Lung Potion.
        BrewingRecipeRegistry.addRecipe(
                PotionUtils.addPotionToItemStack(new ItemStack(Items.POTIONITEM), paleLungType),
                new ItemStack(Items.GLOWSTONE_DUST),
                PotionUtils.addPotionToItemStack(new ItemStack(Items.POTIONITEM), paleLungTypeStrong));
    }
}
