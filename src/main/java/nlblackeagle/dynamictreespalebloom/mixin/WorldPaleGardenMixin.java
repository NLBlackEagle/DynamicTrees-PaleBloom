package nlblackeagle.dynamictreespalebloom.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.sirsquidly.palebloom.common.world.WorldPaleGarden;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(WorldPaleGarden.class)
public abstract class WorldPaleGardenMixin {
    @WrapOperation(
            method = "getNearestBulbs",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;isBlockLoaded(Lnet/minecraft/util/math/BlockPos;)Z")
    )
    private static boolean mixin(World world, BlockPos pos, Operation<Boolean> original){
        if(!original.call(world, pos)) return false;
        IBlockState state = world.getBlockState(pos);
        return state.getBlock().hasTileEntity(state);
    }
}
