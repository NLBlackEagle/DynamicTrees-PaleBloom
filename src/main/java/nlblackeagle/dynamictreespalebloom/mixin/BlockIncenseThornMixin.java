package nlblackeagle.dynamictreespalebloom.mixin;

import com.sirsquidly.palebloom.common.blocks.BlockIncenseThorn;
import com.sirsquidly.palebloom.common.blocks.tileentity.TileIncenseThorn;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import nlblackeagle.dynamictreespalebloom.config.ForgeConfigHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(value = BlockIncenseThorn.class)
public class BlockIncenseThornMixin {

    @Inject(method = {"harvestBlock", "func_180657_a"}, at = @At("HEAD"), cancellable = true)
    private void dynamictreespalebloom$fixDoubleDrop(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state, @Nullable TileEntity te, ItemStack stack, CallbackInfo ci) {
        if (!ForgeConfigHandler.miscellaneous.fixIncenseThornsDoubleDrop) return;

        if (te instanceof TileIncenseThorn) {
            TileIncenseThorn tile = (TileIncenseThorn) te;
            BlockIncenseThorn self = (BlockIncenseThorn) (Object) this;

            ItemStack itemstack = new ItemStack(Item.getItemFromBlock(self));
            NBTTagCompound nbt = new NBTTagCompound();

            if (tile.getPotion() != null) {
                nbt.setString("potion", tile.getPotion().getRegistryName().toString());
            }

            itemstack.setTagCompound(nbt);
            self.spawnAsEntity(worldIn, pos, itemstack);
        }

        ci.cancel();
    }
}
