package nlblackeagle.dynamictreespalebloom.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.sirsquidly.palebloom.common.blocks.BlockIncenseThorn;
import net.minecraft.block.BlockBush;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import nlblackeagle.dynamictreespalebloom.config.ForgeConfigHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BlockIncenseThorn.class)
public class BlockIncenseThornMixin {

    @WrapWithCondition(method = "harvestBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockBush;harvestBlock(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/tileentity/TileEntity;Lnet/minecraft/item/ItemStack;)V"))
    private boolean dynamictreespalebloom$fixDoubleDrop(BlockBush instance, World world, EntityPlayer entityPlayer, BlockPos blockPos, IBlockState iBlockState, TileEntity tileEntity, ItemStack itemStack) {
        return !ForgeConfigHandler.miscellaneous.fixIncenseThornsDoubleDrop;
    }
}
