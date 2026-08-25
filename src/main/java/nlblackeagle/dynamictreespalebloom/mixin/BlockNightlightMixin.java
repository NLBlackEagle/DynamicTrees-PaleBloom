package nlblackeagle.dynamictreespalebloom.mixin;

import com.sirsquidly.palebloom.common.blocks.BlockNightlight;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import nlblackeagle.dynamictreespalebloom.config.ForgeConfigHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Real, live-crash-confirmed Pale Bloom bug (decompiled from the shipped
 * palebloom-1.0.0.jar): BlockNightlight#updateTick calls super.updateTick(...) first -
 * like any BlockBush, that can legitimately break the block into air right there if
 * canBlockStay() fails (Nightlight hangs from a ceiling; canBlockStay requires solid
 * ground, or Pale Hanging Moss specifically, directly above it - lose that support and
 * vanilla's own checkAndDropBlock removes it mid-tick). updateTick then
 * unconditionally calls preformSwapping(world, pos, false) anyway, which does its own
 * fresh world.getBlockState(pos) lookup and reads the AWAKE property straight off it -
 * if the block was just removed by the super call, that's now Blocks.AIR's
 * (property-less) BlockStateContainer, and the getValue() throws
 * IllegalArgumentException, crashing the entire world tick (WorldServer#updateBlocks
 * never catches it).
 * <p>
 * Rather than chase every possible reason a Nightlight's support could vanish, this
 * guards preformSwapping itself: bail out before the crashing lookup whenever the
 * block actually sitting at pos isn't a Nightlight any more.
 */
@Mixin(value = BlockNightlight.class, remap = false)
public abstract class BlockNightlightMixin {

    @Inject(method = "preformSwapping", at = @At("HEAD"), cancellable = true)
    private void dynamictreespalebloom$guardMissingBlock(World world, BlockPos pos, boolean removed, CallbackInfo ci) {
        if (!ForgeConfigHandler.miscellaneous.fixNightlightRemovalCrash) return;

        Block self = (Block) (Object) this;
        if (world.getBlockState(pos).getBlock() != self) {
            ci.cancel();
        }
    }
}
