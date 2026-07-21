package nlblackeagle.dynamictreespalebloom.trees;

import com.ferreusveritas.dynamictrees.api.IPostGenFeature;
import com.ferreusveritas.dynamictrees.api.IPostGrowFeature;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;
import com.sirsquidly.palebloom.common.blocks.BlockCreakingHeart;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import nlblackeagle.dynamictreespalebloom.ModContent;
import nlblackeagle.dynamictreespalebloom.blocks.BlockBranchCreakingHeart;

import java.util.List;

public class FeatureGenCreakingHeart implements IPostGrowFeature, IPostGenFeature {

    private final float chance;
    private final float genChance;
    private final int minTrunkRadius;
    private final int searchHeight;

    public FeatureGenCreakingHeart(float chance, float genChance, int minTrunkRadius, int searchHeight) {
        this.chance = chance;
        this.genChance = genChance;
        this.minTrunkRadius = minTrunkRadius;
        this.searchHeight = searchHeight;
    }

    @Override
    public boolean postGrow(World world, BlockPos rootPos, BlockPos treePos, Species species, int soilLife, boolean natural) {
        if (world.isRemote) return false;
        if (world.rand.nextFloat() >= chance) return false;
        // Reached via planting/growth (hand-planted or self-seeded), never
        // raw worldgen placement - so this is never a "natural" heart.
        return tryPlaceHeart(world, treePos, false);
    }

    @Override
    public boolean postGeneration(World world, BlockPos rootPos, Species species, Biome biome, int radius, List<BlockPos> endPoints, SafeChunkBounds safeBounds, IBlockState initialDirtState) {
        if (world.isRemote) return false;
        if (world.rand.nextFloat() >= genChance) return false;
        // Worldgen trees are placed fully-grown in one pass, so the trunk is
        // already complete by the time this runs - safe to search immediately.
        // This path only ever fires for actual worldgen-generated trees, so
        // the heart is always genuinely "natural" here.
        return tryPlaceHeart(world, rootPos.up(), true);
    }

    private boolean tryPlaceHeart(World world, BlockPos treePos, boolean isNatural) {
        if (findExistingHeart(world, treePos)) return false;

        BlockPos heartPos = findValidHeartPosition(world, treePos);
        if (heartPos == null) return false;

        int matchedRadius = TreeHelper.getRadius(world, heartPos);

        IBlockState heartState = BlockBranchCreakingHeart.withRadius(
                ModContent.paleOakBranchCreakingHeart.getDefaultState(), matchedRadius)
                .withProperty(BlockCreakingHeart.HEART_STATE, BlockCreakingHeart.EnumHeartState.UPROOTED)
                .withProperty(BlockCreakingHeart.NATURAL, isNatural);

        world.setBlockState(heartPos, heartState);
        return true;
    }

    private boolean findExistingHeart(World world, BlockPos treePos) {
        for (int y = 0; y < searchHeight; y++) {
            if (world.getBlockState(treePos.up(y)).getBlock() == ModContent.paleOakBranchCreakingHeart) {
                return true;
            }
        }
        return false;
    }

    private BlockPos findValidHeartPosition(World world, BlockPos treePos) {
        // Search the lower half of the trunk, not the upper half - DT trunks
        // taper as they get taller, so the upper portion is naturally
        // thinner regardless of the trunk's real thick base. Searching low
        // (just above the flared base) means the heart lands somewhere
        // genuinely thick, matching the trunk's actual visual bulk.
        int endHeight = Math.max(3, searchHeight / 2);
        for (int y = 2; y < endHeight; y++) {
            BlockPos candidate = treePos.up(y);

            if (!TreeHelper.isBranch(world.getBlockState(candidate))) continue;
            if (TreeHelper.getRadius(world, candidate) < minTrunkRadius) continue;
            if (!TreeHelper.isBranch(world.getBlockState(candidate.up()))) continue;
            if (!TreeHelper.isBranch(world.getBlockState(candidate.down()))) continue;

            return candidate;
        }
        return null;
    }
}
