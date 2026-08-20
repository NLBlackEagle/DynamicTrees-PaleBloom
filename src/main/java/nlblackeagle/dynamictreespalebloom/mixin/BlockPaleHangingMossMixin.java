package nlblackeagle.dynamictreespalebloom.mixin;

import com.ferreusveritas.dynamictrees.blocks.BlockBranchThick;
import com.ferreusveritas.dynamictrees.blocks.LeavesPaging;
import com.sirsquidly.palebloom.common.blocks.BlockPaleHangingMoss;
import net.minecraft.block.Block;
import nlblackeagle.dynamictreespalebloom.DynamicTreesPaleBloom;
import nlblackeagle.dynamictreespalebloom.ModContent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// blockCausesAmbience hardcodes a check against the real, static Pale Oak log/
// leaves blocks - since the moss we place sits under DT's dynamic leaves (and
// potentially the trunk) instead, that check always fails silently, disabling
// the moss's own ambient rustling sound. Same pattern as the Creaking Heart
// support check from earlier in this project.
@Mixin(value = BlockPaleHangingMoss.class, remap = false)
public class BlockPaleHangingMossMixin {

    @Inject(method = "blockCausesAmbience", at = @At("HEAD"), cancellable = true)
    private void dynamictreespalebloom$recognizeDynamicTrunkAndLeaves(Block block, CallbackInfoReturnable<Boolean> cir) {
        boolean isOurLeaves = LeavesPaging.getLeavesMapForModId(DynamicTreesPaleBloom.MODID).containsValue(block);

        boolean isOurTrunk = block == ModContent.paleOakBranchBlock
                || (ModContent.paleOakBranchBlock instanceof BlockBranchThick
                    && block == ((BlockBranchThick) ModContent.paleOakBranchBlock).otherBlock)
                || block == ModContent.paleOakBranchCreakingHeart;

        if (isOurLeaves || isOurTrunk) {
            cir.setReturnValue(true);
        }
    }
}
