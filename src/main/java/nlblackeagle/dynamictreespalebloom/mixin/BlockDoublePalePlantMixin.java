package nlblackeagle.dynamictreespalebloom.mixin;

import com.sirsquidly.palebloom.common.blocks.BlockDoublePalePlant;
import nlblackeagle.dynamictreespalebloom.config.ForgeConfigHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Real, live-crash-confirmed Pale Bloom bug (decompiled from the shipped
 * palebloom-1.0.0.jar): BlockDoublePalePlant#getStateFromMeta looks up its TYPE with
 * {@code EnumType.values()[meta & 3]}, but EnumType only has 3 values (Eyeblossom Bush,
 * Stiffpod, Epiales) - so any meta whose low two bits are 3 (3, 7, 11, 15) throws
 * ArrayIndexOutOfBoundsException. The block itself never stores those metas, but
 * anything that walks every meta 0-15 does hit them - e.g. OptiFine's shader
 * block.properties parser, which crashes the client the moment a shader pack is
 * selected.
 * <p>
 * This clamps an out-of-range type index back to 0 before the lookup, keeping the
 * HALF (bit 4) and AWAKE (bit 8) bits untouched.
 */
@Mixin(BlockDoublePalePlant.class)
public class BlockDoublePalePlantMixin {

    @ModifyVariable(method = {"getStateFromMeta", "func_176203_a"}, at = @At("HEAD"), argsOnly = true)
    private int dynamictreespalebloom$clampTypeMeta(int meta) {
        if (!ForgeConfigHandler.miscellaneous.fixDoublePalePlantMetaCrash) return meta;

        if ((meta & 3) >= BlockDoublePalePlant.EnumType.values().length) {
            return meta & ~3;
        }
        return meta;
    }
}
