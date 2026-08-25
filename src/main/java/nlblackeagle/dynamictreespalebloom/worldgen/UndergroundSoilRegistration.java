package nlblackeagle.dynamictreespalebloom.worldgen;

import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.systems.DirtHelper;
import com.ferreusveritas.dynamictrees.trees.Species;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import nlblackeagle.dynamictreespalebloom.config.ForgeConfigHandler;

// Cave floors found by WorldGenUndergroundTrees are almost always plain
// Blocks.STONE, but Species.isAcceptableSoilForWorldgen() only accepts the
// "dirtlike" adjective by default (dirt/grass/podzol/farmland/mycelium) -
// meaning every underground generation attempt silently fails with
// FAILSOIL, tree or no tree found. Registering a new adjective for STONE
// and opting only the four species already treated as "underground-capable"
// elsewhere in this codebase (see SeedMixin/BlockDynamicLeavesMixin's
// TARGET_FAMILIES) into it - via DynamicTrees' own public soil API, same
// as ModContent's existing DirtHelper.registerSoil(PALE_MOSS, DIRTLIKE)
// call - fixes this without affecting any other species in the game.
public class UndergroundSoilRegistration {

    private static final String STONE_ADJECTIVE = "dynamictreespalebloom_stonelike";

    public static void register() {
        if (!ForgeConfigHandler.featureToggles.enableRLCraftDregora) return;
        if (!ForgeConfigHandler.rlcraftDregora.enableUndergroundGrowth) return;

        DirtHelper.createNewAdjective(STONE_ADJECTIVE);
        DirtHelper.registerSoil(Blocks.STONE, STONE_ADJECTIVE);

        addStoneSoil("dynamictreespalebloom", "pale_oak");
        addStoneSoil("dynamictreespalebloom", "pale_blooming");
        addStoneSoil("dynamictreespalebloom", "pale_birch");
        addStoneSoil("dynamictrees", "darkoak");
    }

    private static void addStoneSoil(String modid, String name) {
        Species species = TreeRegistry.findSpecies(new ResourceLocation(modid, name));
        if (species != null) {
            species.addAcceptableSoils(STONE_ADJECTIVE);
        }
    }
}
