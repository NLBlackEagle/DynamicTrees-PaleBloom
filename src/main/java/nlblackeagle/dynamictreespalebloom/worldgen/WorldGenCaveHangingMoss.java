package nlblackeagle.dynamictreespalebloom.worldgen;

import com.sirsquidly.palebloom.init.JTPGBiomes;
import com.sirsquidly.palebloom.init.JTPGBlocks;
import net.minecraft.block.IGrowable;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.common.IWorldGenerator;
import nlblackeagle.dynamictreespalebloom.config.ForgeConfigHandler;

import java.util.Random;

/**
 * Replaces an external "pick a random (x, y, z) in y 5-60, check air + solid-or-leaves
 * above + light level, retry" placement loop for cave-ceiling Pale Hanging Moss +
 * Nightlight strands - that's rejection sampling over a mostly-solid 3D volume (most
 * random draws land in stone, most of the rest fail the support/light checks too), so
 * the expected number of wasted checks before one success can run into the hundreds.
 * <p>
 * This scans each attempted (x, z) column exactly once, top to bottom, over the
 * configured Y range - a single bounded pass (at most maxY-minY+1 checks) that's
 * guaranteed to find every valid position in that column, rather than repeatedly
 * guessing. Reservoir sampling (pick each newly-found candidate with probability
 * 1/countSoFar) picks uniformly among whatever's found in that one pass, so strands
 * aren't biased toward whichever cave layer happens to be scanned first.
 * <p>
 * Placement reuses BlockNightlight's own IGrowable#grow(...) (confirmed by decompiling
 * the shipped palebloom-1.0.0.jar) instead of hand-building blockstates: grow() walks
 * down through any existing Pale Hanging Moss chain from the given position, turns the
 * current tip into a moss segment, and re-places the passed Nightlight state one block
 * further down - so placing the anchor Nightlight and calling grow() N times extends a
 * proper moss-strand-with-a-light-at-the-tip exactly the way natural/bonemeal growth
 * would, with correct blockstates guaranteed. Growth silently stops if it runs out of
 * clear vertical space, so the actual final length can come out shorter than requested
 * - that's an intentional, harmless side effect of reusing the real growth logic, not
 * a bug to guard against.
 * <p>
 * A chosen anchor also needs floor clearance: less than the configured minimum is
 * rejected outright, and anything past the preferred amount gets progressively rarer
 * (a per-block chance falloff, never a hard cutoff) rather than simply being capped -
 * see {@link #passesFloorClearanceCheck}.
 */
public class WorldGenCaveHangingMoss implements IWorldGenerator {

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
        if (world.provider.getDimension() != 0) return;

        BlockPos chunkCenter = new BlockPos((chunkX << 4) + 8, 0, (chunkZ << 4) + 8);
        Biome biome = world.getBiome(chunkCenter);
        if (biome != JTPGBiomes.PALE_GARDEN) return;

        int attempts = ForgeConfigHandler.biomeDecoration.caveHangingMossAttemptsPerChunk;
        int minY = ForgeConfigHandler.biomeDecoration.caveHangingMossMinY;
        int maxY = ForgeConfigHandler.biomeDecoration.caveHangingMossMaxY;
        int maxLight = ForgeConfigHandler.biomeDecoration.caveHangingMossMaxLightLevel;

        for (int i = 0; i < attempts; i++) {
            int x = (chunkX << 4) + random.nextInt(16);
            int z = (chunkZ << 4) + random.nextInt(16);

            BlockPos anchor = findCeilingSpot(world, x, z, minY, maxY, maxLight, random);
            if (anchor == null) continue;
            if (!passesFloorClearanceCheck(world, anchor, random)) continue;

            placeStrand(world, random, anchor);
        }
    }

    // Rejects anchors that are basically resting on the floor (< min clearance
    // outright), then makes anything past the preferred clearance progressively rarer
    // - never a hard cutoff, just a per-block chance falloff - so a Nightlight hanging
    // over a tall open cavern stays possible, just uncommon.
    private static boolean passesFloorClearanceCheck(World world, BlockPos anchor, Random random) {
        int minClearance = ForgeConfigHandler.biomeDecoration.caveHangingMossMinFloorClearance;
        int preferredClearance = ForgeConfigHandler.biomeDecoration.caveHangingMossPreferredFloorClearance;
        double falloff = ForgeConfigHandler.biomeDecoration.caveHangingMossFloorClearanceFalloff;

        int scanCap = Math.max(minClearance, preferredClearance) + 16;
        int clearance = measureFloorClearance(world, anchor, scanCap);

        if (clearance < minClearance) return false;
        if (clearance <= preferredClearance) return true;

        double chance = Math.pow(falloff, clearance - preferredClearance);
        return random.nextDouble() < chance;
    }

    // Counts consecutive air blocks starting directly below the anchor, up to
    // maxScan, stopping at the first non-air block (the floor). Reaching maxScan
    // without hitting a floor (a very tall open cavern) is treated as "clearance ==
    // maxScan" rather than scanning indefinitely.
    private static int measureFloorClearance(World world, BlockPos anchor, int maxScan) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int clearance = 0;

        for (int i = 1; i <= maxScan; i++) {
            pos.setPos(anchor.getX(), anchor.getY() - i, anchor.getZ());
            if (!world.isAirBlock(pos)) break;
            clearance++;
        }

        return clearance;
    }

    // Single top-to-bottom pass over the column, reservoir-sampling one valid position
    // out of however many turn up, instead of independently re-rolling x/y/z.
    private static BlockPos findCeilingSpot(World world, int x, int z, int minY, int maxY, int maxLight, Random random) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, minY, z);
        BlockPos chosen = null;
        int found = 0;

        for (int y = maxY; y >= minY; y--) {
            pos.setPos(x, y, z);
            if (!world.isAirBlock(pos)) continue;

            // Same validity rule BlockNightlight's own canBlockStay uses (confirmed via
            // decompile) - solid face directly above, or an existing hanging moss
            // segment - so anything placed here won't immediately invalidate itself.
            BlockPos above = pos.up();
            IBlockState aboveState = world.getBlockState(above);
            boolean validSupport = aboveState.getBlockFaceShape(world, above, EnumFacing.DOWN) == BlockFaceShape.SOLID
                    || aboveState.getBlock() == JTPGBlocks.PALE_HANGING_MOSS;
            if (!validSupport) continue;

            if (world.getLightFromNeighbors(pos) > maxLight) continue;

            found++;
            if (random.nextInt(found) == 0) {
                chosen = pos.toImmutable();
            }
        }

        return chosen;
    }

    private static void placeStrand(World world, Random random, BlockPos anchor) {
        IBlockState nightlightState = pickNightlightState(random);
        world.setBlockState(anchor, nightlightState, 2);

        int minLength = ForgeConfigHandler.biomeDecoration.caveHangingMossMinLength;
        int maxLength = ForgeConfigHandler.biomeDecoration.caveHangingMossMaxLength;
        int length = minLength + random.nextInt(maxLength - minLength + 1);

        IGrowable growable = (IGrowable) JTPGBlocks.NIGHTLIGHT;
        for (int i = 0; i < length; i++) {
            growable.grow(world, random, anchor, nightlightState);
        }
    }

    // getStateFromMeta (confirmed via decompile): AGE = meta & 3, AWAKE = (meta & 4) != 0.
    // Meta 7 is AGE=3 (fully grown bulb) + AWAKE=true (lit) - 80% of strands use that,
    // the rest get a random one of the other 7 combinations (0-6).
    private static IBlockState pickNightlightState(Random random) {
        int meta = random.nextInt(100) < 80 ? 7 : random.nextInt(7);
        return JTPGBlocks.NIGHTLIGHT.getStateFromMeta(meta);
    }
}
