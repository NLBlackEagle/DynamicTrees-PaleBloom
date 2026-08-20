package nlblackeagle.dynamictreespalebloom.mixin;

import com.ferreusveritas.dynamictrees.blocks.BlockBranch;
import com.sirsquidly.palebloom.common.blocks.BlockCreakingHeart;
import com.sirsquidly.palebloom.init.JTPGItems;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import nlblackeagle.dynamictreespalebloom.ModContent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// DT's whole-tree felling (futureBreak -> destroyBranchFromNode) calculates
// drops generically by wood volume, completely bypassing our Creaking
// Heart's own harvestBlock logic - felling a tree with a natural heart
// embedded currently drops zero Amber Valve. Worse, the actual destruction
// happens INSIDE destroyBranchFromNode itself (before it even returns), and
// DT's own BranchDestructionData can't preserve our custom NATURAL property
// through its encode/decode round-trip anyway (it only reconstructs a
// generic state via radius+connections). So instead of trying to hook DT's
// internal machinery, this scans a cheap vertical line above/below the cut
// point BEFORE any destruction happens (while the real blocks, with their
// real NATURAL flag, are still intact), and drops the bonus item ourselves
// if a natural heart is found - independent of DT's own drop list entirely.
@Mixin(value = BlockBranch.class)
public class BlockBranchFellingMixin {

    @Inject(method = "futureBreak", at = @At("HEAD"))
    private void dynamictreespalebloom$dropAmberOnFelling(IBlockState state, World world, BlockPos cutPos, EntityLivingBase entity, CallbackInfo ci) {
        if (world.isRemote) return;

        Block block = state.getBlock();
        if (!isOurTree(block)) return;

        BlockPos naturalHeartPos = findNaturalHeartNearby(world, cutPos);
        if (naturalHeartPos != null) {
            Block.spawnAsEntity(world, cutPos, new ItemStack(JTPGItems.AMBER_VALVE));
        }
    }

    private boolean isOurTree(Block block) {
        return block == ModContent.paleOakBranchBlock
                || block == ModContent.paleOakBranchCreakingHeart
                || block == ModContent.paleOakBranchCreakingHeartX
                || block == ModContent.paleBloomingOakBranchBlock
                || block == ModContent.paleBloomingOakBranchCreakingHeart
                || block == ModContent.paleBloomingOakBranchCreakingHeartX
                || (ModContent.paleOakBranchBlock instanceof com.ferreusveritas.dynamictrees.blocks.BlockBranchThick
                    && block == ((com.ferreusveritas.dynamictrees.blocks.BlockBranchThick) ModContent.paleOakBranchBlock).otherBlock)
                || (ModContent.paleBloomingOakBranchBlock instanceof com.ferreusveritas.dynamictrees.blocks.BlockBranchThick
                    && block == ((com.ferreusveritas.dynamictrees.blocks.BlockBranchThick) ModContent.paleBloomingOakBranchBlock).otherBlock);
    }

    // We control the heart's placement logic ourselves (FeatureGenCreakingHeart
    // always embeds it directly in the trunk column, 8-15 blocks above the
    // tree's base, never offset horizontally) - so instead of a wide 3D
    // search, just scan a vertical line straight up/down from the cut point.
    // A few blocks of downward buffer accounts for the player not
    // necessarily chopping exactly at the tree's true base. This drops the
    // scan from ~13,000 positions down to about 24.
    private BlockPos findNaturalHeartNearby(World world, BlockPos cutPos) {
        for (int y = -3; y <= 20; y++) {
            BlockPos pos = cutPos.up(y);
            Block block = world.getBlockState(pos).getBlock();
            if (block == ModContent.paleOakBranchCreakingHeart || block == ModContent.paleOakBranchCreakingHeartX
                    || block == ModContent.paleBloomingOakBranchCreakingHeart || block == ModContent.paleBloomingOakBranchCreakingHeartX) {
                IBlockState state = world.getBlockState(pos);
                if (state.getValue(BlockCreakingHeart.NATURAL)) {
                    return pos.toImmutable();
                }
            }
        }
        return null;
    }
}
