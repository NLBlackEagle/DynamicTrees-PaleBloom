package nlblackeagle.dynamictreespalebloom.mixin;

import com.ferreusveritas.dynamictrees.blocks.BlockBranchThick;
import com.sirsquidly.palebloom.common.blocks.BlockCreakingHeart;
import net.minecraft.block.Block;
import nlblackeagle.dynamictreespalebloom.ModContent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockCreakingHeart.class)
public class BlockCreakingHeartMixin {

    @Inject(method = "blockSupportsHeart", at = @At("HEAD"), cancellable = true, remap = false)
    private void dynamictreespalebloom$supportDynamicPaleOak(Block block, CallbackInfoReturnable<Boolean> cir) {
        if (block == ModContent.paleOakBranchBlock
                || (ModContent.paleOakBranchBlock instanceof BlockBranchThick
                && block == ((BlockBranchThick) ModContent.paleOakBranchBlock).otherBlock)) {
            cir.setReturnValue(true);
        }
    }
}