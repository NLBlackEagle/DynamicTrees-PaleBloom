package nlblackeagle.dynamictreespalebloom.worldgen;

import com.ferreusveritas.dynamictrees.ModConfigs;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;
import com.ferreusveritas.dynamictrees.worldgen.BiomeDataBase;
import com.ferreusveritas.dynamictrees.worldgen.TreeGenerator;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.common.IWorldGenerator;
import nlblackeagle.dynamictreespalebloom.config.ForgeConfigHandler;

import java.util.Random;

// DT core already has everything needed to grow a tree downward into a cave
// pocket instead of up from the surface - BiomeDataBase$BiomeEntry has a
// "subterranean" flag, and WorldGeneratorTrees.GroundFinder branches to
// findSubterraneanGround() (scan for an air pocket's floor) when that flag
// is set on the entry it's given. It's only ever actually used for the
// Nether ("we don't generate there" per DT's own default.json comment), so
// nothing in the Overworld ever exercises it. This class is a second,
// independent IWorldGenerator pass that reuses that exact same machinery
// (TreeGenerator.makeTree + UndergroundGroundFinder, a thin subclass of
// WorldGeneratorTrees.GroundFinder that narrows its hardcoded y 0-128 cave
// scan down to y 0-70) against our own standalone BiomeDataBase - built
// here, not registered with WorldGenRegistry - so it never touches the
// shared surface database the normal Pale Garden trees use. Same
// Poisson-disc canopy layout as the surface pass (deterministic per chunk,
// so this doesn't reserve any new canopy space), just resolved to a cave
// floor instead of the height map. Gated behind the same two flags that
// already govern underground tree survival (SeedMixin/BlockDynamicLeavesMixin's
// "Enable Underground Growth"): with that off, a worldgen-spawned
// underground tree's leaves would fail hasAdequateLight() and the tree
// would die shortly after generating, so there's no point spawning it in
// the first place.
public class WorldGenUndergroundTrees implements IWorldGenerator {

    // Populated by ModContent.onPopulateDataBase() during DT's own
    // PopulateDataBaseEvent window - see that method for why this can't be
    // built lazily here on first use (BiomeDataBasePopulatorJson's static
    // selector/applier property maps are wiped by cleanup() shortly after
    // that event fires, long before any chunk actually generates).
    private static BiomeDataBase undergroundDataBase;

    public static void setUndergroundDataBase(BiomeDataBase dbase) {
        undergroundDataBase = dbase;
    }

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
        if (!ForgeConfigHandler.featureToggles.enableRLCraftDregora) return;
        if (!ForgeConfigHandler.rlcraftDregora.enableUndergroundGrowth) return;
        if (world.provider.getDimension() != 0) return;
        if (undergroundDataBase == null) return;

        if (random.nextFloat() >= ForgeConfigHandler.rlcraftDregora.undergroundTreeSpawnChance) return;

        int dimensionId = world.provider.getDimension();
        BiomeDataBase surfaceDbase = TreeGenerator.getTreeGenerator().getBiomeDataBase(dimensionId);
        if (surfaceDbase == TreeGenerator.DIMENSIONBLACKLISTED || ModConfigs.dimensionBlacklist.contains(dimensionId)) return;

        TreeGenerator treeGenerator = TreeGenerator.getTreeGenerator();
        BiomeDataBase dbase = undergroundDataBase;
        SafeChunkBounds bounds = new SafeChunkBounds(world, new ChunkPos(chunkX, chunkZ));

        treeGenerator.getCircleProvider().getPoissonDiscs(world, chunkX, 0, chunkZ).forEach(disc ->
                treeGenerator.makeTree(world, dbase, disc, new UndergroundGroundFinder(), bounds));
    }
}
