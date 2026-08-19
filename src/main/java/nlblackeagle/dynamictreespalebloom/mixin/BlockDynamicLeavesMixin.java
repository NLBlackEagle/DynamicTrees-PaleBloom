package nlblackeagle.dynamictreespalebloom.mixin;

import com.ferreusveritas.dynamictrees.api.treedata.ILeavesProperties;
import com.ferreusveritas.dynamictrees.blocks.BlockDynamicLeaves;
import com.ferreusveritas.dynamictrees.trees.TreeFamily;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import nlblackeagle.dynamictreespalebloom.config.ForgeConfigHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;
import java.util.List;

// Part of the "Enable Underground Growth" RLCraft Dregora option. DT's
// BlockDynamicLeaves.hasAdequateLight() early-returns true only when the
// leaf has direct sky visibility - underground leaves fall through to
// checking EnumSkyBlock.SKY specifically, a separate lighting channel from
// torches/block-light, meaning no amount of underground lighting could
// ever satisfy it. Since a tree needs its leaves to survive for the whole
// hydration/growth-signal system to function, this single check otherwise
// kills any tree attempting to grow without direct sky access. Bypassed
// (always adequate light) when the config option is enabled AND the leaf
// belongs to one of our four target families - our three Pale Bloom
// species, plus vanilla Dark Oak specifically (not every Dynamic Trees
// species globally).
@Mixin(value = BlockDynamicLeaves.class)
public class BlockDynamicLeavesMixin {

    private static final List<String> TARGET_FAMILIES = Arrays.asList(
            "dynamictreespalebloom:pale_oak",
            "dynamictreespalebloom:pale_blooming",
            "dynamictreespalebloom:pale_birch",
            "dynamictrees:darkoak"
    );

    @Inject(method = "hasAdequateLight", at = @At("HEAD"), cancellable = true)
    private void dynamictreespalebloom$allowUndergroundLeaves(IBlockState blockState, World world, ILeavesProperties leavesProperties, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (!ForgeConfigHandler.featureToggles.enableRLCraftDregora) return;
        if (!ForgeConfigHandler.rlcraftDregora.enableUndergroundGrowth) return;

        TreeFamily family = leavesProperties.getTree();
        if (family == null) return;

        if (TARGET_FAMILIES.contains(family.getName().toString())) {
            cir.setReturnValue(true);
        }
    }
}
