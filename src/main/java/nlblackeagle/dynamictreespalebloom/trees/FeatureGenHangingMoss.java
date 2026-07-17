package nlblackeagle.dynamictreespalebloom.trees;

import com.ferreusveritas.dynamictrees.api.IPostGenFeature;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;
import com.sirsquidly.palebloom.init.JTPGBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

// Worldgen-only hanging moss - runs once, right when the tree is generated,
// unlike an ongoing per-tick mechanic. Scans a small area around each branch
// endpoint (where leaves cluster) for actual leaf blocks and rolls moss
// placement per leaf found, so it never grows or spreads after generation.
public class FeatureGenHangingMoss implements IPostGenFeature {

    private final float chance;

    public FeatureGenHangingMoss(float chance) {
        this.chance = chance;
    }

    @Override
    public boolean postGeneration(World world, BlockPos rootPos, Species species, Biome biome, int radius, List<BlockPos> endPoints, SafeChunkBounds safeBounds, IBlockState initialDirtState) {
        if (world.isRemote) return false;

        Random rand = world.rand;
        Set<BlockPos> visited = new HashSet<>();

        for (BlockPos endPoint : endPoints) {
            for (BlockPos leafPos : BlockPos.getAllInBoxMutable(endPoint.add(-2, -2, -2), endPoint.add(2, 2, 2))) {
                BlockPos leafPosImmutable = leafPos.toImmutable();
                if (visited.contains(leafPosImmutable)) continue;
                visited.add(leafPosImmutable);

                if (!TreeHelper.isLeaves(world.getBlockState(leafPos))) continue;
                if (rand.nextFloat() >= chance) continue;

                placeMossChain(world, leafPos.down(), rand);
            }
        }

        return true;
    }

    private void placeMossChain(World world, BlockPos mossPos, Random rand) {
        if (!world.isAirBlock(mossPos) || !world.isAirBlock(mossPos.down())) return;

        world.setBlockState(mossPos, JTPGBlocks.PALE_HANGING_MOSS.getDefaultState());

        // First 3 extra blocks always attempt to place. Beyond that, each
        // additional block only has a 50% chance to continue, so short
        // strands are common and reaching the full 10-block length is rare.
        BlockPos chainPos = mossPos.down();
        int placed = 0;
        while (world.isAirBlock(chainPos) && world.isAirBlock(chainPos.down()) && placed < 10) {
            if (placed >= 3 && rand.nextBoolean()) break;
            world.setBlockState(chainPos, JTPGBlocks.PALE_HANGING_MOSS.getDefaultState());
            chainPos = chainPos.down();
            placed++;
        }
    }
}
