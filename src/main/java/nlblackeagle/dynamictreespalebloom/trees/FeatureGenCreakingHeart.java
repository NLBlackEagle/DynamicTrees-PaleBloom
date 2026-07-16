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
import java.util.Random;

public class FeatureGenCreakingHeart implements IPostGrowFeature, IPostGenFeature {

    private final float chance;
    private final float genChance;
    private final float naturalChance;
    private final int minTrunkRadius;
    private final int searchHeight;

    public FeatureGenCreakingHeart(float chance, float genChance, float naturalChance, int minTrunkRadius, int searchHeight) {
        this.chance = chance;
        this.genChance = genChance;
        this.naturalChance = naturalChance;
        this.minTrunkRadius = minTrunkRadius;
        this.searchHeight = searchHeight;
    }

    @Override
    public boolean postGrow(World world, BlockPos rootPos, BlockPos treePos, Species species, int soilLife, boolean natural) {
        if (world.isRemote) return false;
        if (world.rand.nextFloat() >= chance) return false;
        return tryPlaceHeart(world, treePos, world.rand);
    }

    @Override
    public boolean postGeneration(World world, BlockPos rootPos, Species species, Biome biome, int radius, List<BlockPos> endPoints, SafeChunkBounds safeBounds, IBlockState initialDirtState) {
        if (world.isRemote) return false;
        if (world.rand.nextFloat() >= genChance) return false;
        // Worldgen trees are placed fully-grown in one pass, so the trunk is
        // already complete by the time this runs - safe to search immediately.
        return tryPlaceHeart(world, rootPos.up(), world.rand);
    }

    private boolean tryPlaceHeart(World world, BlockPos treePos, Random rand) {
        if (findExistingHeart(world, treePos)) return false;

        BlockPos heartPos = findValidHeartPosition(world, treePos);
        if (heartPos == null) return false;

        boolean isNatural = rand.nextFloat() < naturalChance;

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
        int startHeight = Math.max(2, searchHeight / 2);
        for (int y = startHeight; y < searchHeight; y++) {
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
