package nlblackeagle.dynamictreespalebloom.trees;

import com.ferreusveritas.dynamictrees.api.IPostGenFeature;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;
import com.sirsquidly.palebloom.common.blocks.BlockSuckerRoots;
import com.sirsquidly.palebloom.init.JTPGBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import java.util.List;
import java.util.Random;

// Cheap, direct-position-check design (no area scanning at all):
// 1. Check one block in each of the 4 cardinal directions from the base,
//    each independently a 10% chance to place a Sucker Root Nodule. Stops
//    at the first success (max 1 nodule per tree, per call).
// 2. Independently (not conditioned on the nodule), crawl outward 1-3 blocks
//    in each cardinal direction from the base (with a little random wobble),
//    trying to place Sucker Roots on valid topsoil along the way.
//
// Only ever runs exactly once per tree, from two genuinely one-time DT
// events - no per-pulse re-rolling and no need to infer "did this already
// happen" from existing blocks at all:
//   - Worldgen: postGeneration (IPostGenFeature), called once when the tree
//     is placed during chunk generation.
//   - Hand-grown: called directly from SpeciesBloomingPaleOak's own override
//     of Species.transitionToTree(), the actual real DT event fired exactly
//     once when a sapling becomes a tree (covers both organic growth and
//     bonemeal, since both paths call transitionToTree the same way). This
//     is NOT postGrow - postGrow fires repeatedly throughout a tree's whole
//     life as part of ongoing branch growth, which is why earlier versions
//     of this feature (built on postGrow) kept re-triggering indefinitely.
public class FeatureGenSuckerRoots implements IPostGenFeature {

    private static final IBlockState ROOT = JTPGBlocks.SUCKER_ROOTS.getDefaultState();
    private static final IBlockState ROOT_NODULE = JTPGBlocks.SUCKER_ROOT_NODULE.getDefaultState();

    private final float noduleChance;

    public FeatureGenSuckerRoots(float noduleChance) {
        this.noduleChance = noduleChance;
    }

    @Override
    public boolean postGeneration(World world, BlockPos rootPos, Species species, Biome biome, int radius, List<BlockPos> endPoints, SafeChunkBounds safeBounds, IBlockState initialDirtState) {
        if (world.isRemote) return false;
        generateSuckerFeature(world, rootPos.up(), world.rand);
        return true;
    }

    // Called directly from SpeciesBloomingPaleOak.transitionToTree() - public
    // so it's callable from outside this package.
    public void generateSuckerFeature(World world, BlockPos basePos, Random rand) {
        boolean placedNodule = false;

        for (EnumFacing facing : EnumFacing.Plane.HORIZONTAL) {
            // FeatureGenMound raises the actual ground level around the tree
            // by 1-2 blocks during worldgen - re-anchor to the real current
            // surface at this X/Z rather than assuming basePos's own height
            // still applies, or checks land buried inside the mound instead
            // of on top of it. Harmless no-op adjustment for hand-grown trees
            // (no mound exists there, so the "real" surface already matches).
            BlockPos surfacePos = world.getTopSolidOrLiquidBlock(basePos.offset(facing));

            if (!placedNodule && rand.nextFloat() < noduleChance) {
                placedNodule = tryPlaceNodule(world, surfacePos, rand);
            }

            int reach = rand.nextInt(3) + 1;
            BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(surfacePos);

            for (int i = 0; i < reach; i++) {
                cursor.move(facing);
                if (rand.nextBoolean()) {
                    cursor.move(rand.nextBoolean() ? facing.rotateY() : facing.rotateY().getOpposite());
                }
                cursor.setPos(world.getTopSolidOrLiquidBlock(cursor.toImmutable()));
                tryPlaceRootOnTopsoil(world, cursor.toImmutable(), rand);
            }
        }
    }

    private boolean tryPlaceNodule(World world, BlockPos surfacePos, Random rand) {
        BlockPos groundPos = surfacePos.down();
        Block ground = world.getBlockState(groundPos).getBlock();
        if (ground == JTPGBlocks.PALE_MOSS || ground == Blocks.GRASS || ground == Blocks.DIRT) {
            world.setBlockState(groundPos, ROOT_NODULE); // embed the nodule into the ground itself
            if (world.isAirBlock(surfacePos)) {
                world.setBlockState(surfacePos, ROOT.withProperty(BlockSuckerRoots.LAYERS, 1 + rand.nextInt(2))); // roots on top of it
            }
            return true;
        }
        return false;
    }

    private void tryPlaceRootOnTopsoil(World world, BlockPos pos, Random rand) {
        if (!world.isAirBlock(pos)) return;
        Block below = world.getBlockState(pos.down()).getBlock();
        if (below == JTPGBlocks.PALE_MOSS || below == Blocks.GRASS || below == Blocks.DIRT) {
            world.setBlockState(pos, ROOT.withProperty(BlockSuckerRoots.LAYERS, 1 + rand.nextInt(2)));
        }
    }
}
