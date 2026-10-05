package nlblackeagle.dynamictreespalebloom.event;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistryModifiable;
import nlblackeagle.dynamictreespalebloom.DynamicTreesPaleBloom;
import nlblackeagle.dynamictreespalebloom.config.ForgeConfigHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

// Pale Bloom's pale_moss_cloak_<scion>.json grafting recipes have no
// item_exists condition, so with the cloak disabled in Pale Bloom's config
// they still register - with an output ItemPaleMossCloak instance that was
// never added to the item registry (registry name null). Forge's recipe
// registry validation then throws on every world load, and JEI errors out
// building its recipe list. JSON recipes are loaded before the IRecipe
// registry event fires, so they're already present by the time this runs.
@Mod.EventBusSubscriber(modid = DynamicTreesPaleBloom.MODID)
public class UnregisteredRecipeOutputHandler {

    private static final Logger LOGGER = Logger.getLogger(DynamicTreesPaleBloom.MODID);

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRegisterRecipes(RegistryEvent.Register<IRecipe> event) {
        if (!ForgeConfigHandler.miscellaneous.fixUnregisteredRecipeOutputCrash) return;

        IForgeRegistryModifiable<IRecipe> registry = (IForgeRegistryModifiable<IRecipe>) event.getRegistry();

        List<ResourceLocation> recipesToRemove = new ArrayList<>();
        for (IRecipe recipe : registry.getValuesCollection()) {
            ItemStack output = recipe.getRecipeOutput();
            if (!output.isEmpty() && output.getItem().getRegistryName() == null) {
                recipesToRemove.add(recipe.getRegistryName());
            }
        }

        for (ResourceLocation loc : recipesToRemove) {
            LOGGER.info("Removing recipe " + loc + " - its output item is not registered");
            registry.remove(loc);
        }
    }
}
