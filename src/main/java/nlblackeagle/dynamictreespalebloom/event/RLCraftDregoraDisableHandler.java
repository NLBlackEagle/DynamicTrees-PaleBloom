package nlblackeagle.dynamictreespalebloom.event;

import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistryModifiable;
import nlblackeagle.dynamictreespalebloom.DynamicTreesPaleBloom;
import nlblackeagle.dynamictreespalebloom.config.ForgeConfigHandler;

import java.util.ArrayList;
import java.util.List;

// Removes crafting recipes that produce blacklisted Pale Bloom items - the
// items themselves stay fully registered and functional, only the ability
// to craft them goes away. Deliberately narrower than trying to remove the
// items entirely: Block/Item registries are non-modifiable at runtime
// (confirmed - throws UnsupportedOperationException), but the recipe
// registry doesn't have that same restriction.
//
// Uses @Mod.EventBusSubscriber with a static method, NOT manual
// MinecraftForge.EVENT_BUS.register() in CommonProxy.preInit() - registry
// events fire before any mod's preInit runs, so an instance registered
// there would already be too late to catch the initial registration pass.
// This matches the same pattern ModContent already uses successfully for
// its own recipe registration.
@Mod.EventBusSubscriber(modid = DynamicTreesPaleBloom.MODID)
public class RLCraftDregoraDisableHandler {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRegisterRecipes(RegistryEvent.Register<IRecipe> event) {
        if (!ForgeConfigHandler.rlcraftDregora.enableItemBlacklist) return;

        IForgeRegistryModifiable<IRecipe> registry = (IForgeRegistryModifiable<IRecipe>) event.getRegistry();

        List<Item> blacklistedItems = new ArrayList<>();
        for (String entry : ForgeConfigHandler.rlcraftDregora.itemBlacklist) {
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(entry));
            if (item != null) blacklistedItems.add(item);
        }

        List<ResourceLocation> recipesToRemove = new ArrayList<>();
        for (IRecipe recipe : registry.getValuesCollection()) {
            Item output = recipe.getRecipeOutput().getItem();
            if (blacklistedItems.contains(output)) {
                recipesToRemove.add(recipe.getRegistryName());
            }
        }

        for (ResourceLocation loc : recipesToRemove) {
            registry.remove(loc);
        }
    }
}
