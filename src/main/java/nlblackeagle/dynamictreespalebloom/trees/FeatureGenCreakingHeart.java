package nlblackeagle.dynamictreespalebloom.trees;

import com.ferreusveritas.dynamictrees.api.IPostGrowFeature;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.sirsquidly.palebloom.common.blocks.BlockCreakingHeart;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import nlblackeagle.dynamictreespalebloom.ModContent;
import nlblackeagle.dynamictreespalebloom.blocks.BlockBranchCreakingHeart;

import java.util.Random;

public class FeatureGenCreakingHeart implements IPostGrowFeature {

    private final float chance;
    private final float naturalChance;
    private final int minTrunkRadius;
    private final int searchHeight;

    public FeatureGenCreakingHeart(float chance, float naturalChance, int minTrunkRadius, int searchHeight) {
        this.chance = chance;
        this.naturalChance = naturalChance;
        this.minTrunkRadius = minTrunkRadius;
        this.searchHeight = searchHeight;
    }

    @Override
    public boolean postGrow(World world, BlockPos rootPos, BlockPos treePos, Species species, int soilLife, boolean natural) {
        if (world.isRemote) return false;

        Random rand = world.rand;
        if (rand.nextFloat() >= chance) return false;

        if (findExistingHeart(world, treePos)) return false;

        BlockPos heartPos = findValidHeartPosition(world, treePos);
        if (heartPos == null) return false;

        boolean isNatural = rand.nextFloat() < naturalChance;

        // Carry over the radius of the trunk segment we're replacing, so the heart
        // reads as a natural continuation of the trunk rather than a thin twig.
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

    // Embeds directly in the trunk column, replacing an existing branch segment -
    // now safe now that BlockBranchCreakingHeart is properly linked to the tree
    // family via setFamily(), so DT's structural/felling logic recognizes it
    // as a real continuation of the trunk instead of a gap.
    private BlockPos findValidHeartPosition(World world, BlockPos treePos) {
        int startHeight = Math.max(2, searchHeight / 2); // bias toward upper trunk, near the canopy
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
