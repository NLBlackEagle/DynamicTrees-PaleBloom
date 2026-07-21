package nlblackeagle.dynamictreespalebloom.blocks;

import com.ferreusveritas.dynamictrees.blocks.BlockBranchThick;
import com.sirsquidly.palebloom.common.blocks.BlockCreakingHeart;
import com.sirsquidly.palebloom.common.blocks.tileentity.TileCreakingHeart;
import com.sirsquidly.palebloom.config.ConfigCache;
import com.sirsquidly.palebloom.common.world.WorldPaleGarden;
import com.sirsquidly.palebloom.init.JTPGItems;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Random;

// Now extends BlockBranchThick (was BlockBranchBasic) - a Basic branch caps
// its radius at RADMAX_NORMAL (8), which reads visibly thin embedded in a
// genuinely thick trunk section (Blooming Pale Oak can reach well past 8).
// Extending Thick unlocks the full RADMAX_THICK (24) range, same as the
// actual trunk, requiring the same otherBlock self-pairing constructor
// pattern already used for BlockBranchPaleOak/BlockBranchBloomingPaleOak.
public class BlockBranchCreakingHeart extends BlockBranchThick implements net.minecraft.block.ITileEntityProvider {

    public BlockBranchCreakingHeart(String name) {
        this(Material.WOOD, name);
    }

    public BlockBranchCreakingHeart(Material material, String name) {
        super(material, name, false);
        otherBlock = new BlockBranchCreakingHeart(material, name + "x", true);
        otherBlock.otherBlock = this;

        cacheBranchThickStates();
        setHardness(3.0F); // matches BlockCreakingHeart-ish wood hardness
    }

    protected BlockBranchCreakingHeart(Material material, String name, boolean extended) {
        super(material, name, extended);
        setHardness(3.0F);
    }

    // Lets callers outside this package (FeatureGenCreakingHeart) set the radius
    // without needing direct access to the protected RADIUSNYBBLE field. Uses
    // the exact inverse of BlockBranchThick.getRadius()'s own encoding formula:
    // effective radius = nybble + (extended ? 17 : 1), clamped 1-24.
    public static IBlockState withRadius(IBlockState state, int radius) {
        BlockBranchCreakingHeart block = (BlockBranchCreakingHeart) state.getBlock();
        int clampedRadius = MathHelper.clamp(radius, 1, RADMAX_THICK);
        int nybble = MathHelper.clamp(clampedRadius - (block.extended ? 17 : 1), 0, 15);
        return state.withProperty(RADIUSNYBBLE, nybble);
    }

    @SideOnly(Side.CLIENT)
    public static void registerStateMapper(Block block) {
        // Visually identical to a normal trunk segment at all times - state is
        // still tracked functionally (HEART_STATE/NATURAL), just never affects
        // which model gets rendered. Discovery happens via the hit-particle
        // effect below instead of a distinct appearance.
        ModelLoader.setCustomStateMapper(block, new net.minecraft.client.renderer.block.statemap.StateMap.Builder()
                .ignore(RADIUSNYBBLE, BlockCreakingHeart.HEART_STATE, BlockCreakingHeart.NATURAL)
                .build());
    }

    @Override
    public void onBlockClicked(World world, BlockPos pos, EntityPlayer player) {
        if (world.isRemote) {
            for (int i = 0; i < 8; i++) {
                double x = pos.getX() + 0.5 + (world.rand.nextDouble() - 0.5) * 0.8;
                double y = pos.getY() + 0.5 + (world.rand.nextDouble() - 0.5) * 0.8;
                double z = pos.getZ() + 0.5 + (world.rand.nextDouble() - 0.5) * 0.8;
                world.spawnParticle(net.minecraft.util.EnumParticleTypes.REDSTONE, x, y, z, 1.0, 0.65, 0.0);
            }
        }
        super.onBlockClicked(world, pos, player);
    }

    @Override
    public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, java.util.Random rand) {
        if (rand.nextInt(16) == 0 && stateIn.getValue(BlockCreakingHeart.HEART_STATE) != BlockCreakingHeart.EnumHeartState.UPROOTED
                && WorldPaleGarden.isNight(worldIn)) {
            worldIn.playSound(pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F,
                    com.sirsquidly.palebloom.init.JTPGSounds.BLOCK_CREAKING_HEART_AMBIENT,
                    net.minecraft.util.SoundCategory.BLOCKS, 1.0F, 1.0F, false);
        }
    }

    @Override
    protected BlockStateContainer createBlockState() {
        // BlockBranchThick's own createBlockState uses RADIUSNYBBLE (0-15),
        // not RADIUS (1-24) from BlockBranchBasic - our earlier version
        // replaced DT's real property list instead of extending it, dropping
        // RADIUSNYBBLE entirely and crashing the moment getRadius() tried to
        // read it during the "x" companion's own construction.
        IProperty[] listedProperties = {RADIUSNYBBLE, BlockCreakingHeart.HEART_STATE, BlockCreakingHeart.NATURAL};
        return new ExtendedBlockState(this, listedProperties, CONNECTIONS);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        BlockCreakingHeart.EnumHeartState[] states = BlockCreakingHeart.EnumHeartState.values();
        return this.getDefaultState()
                .withProperty(BlockCreakingHeart.HEART_STATE, states[meta % states.length])
                .withProperty(BlockCreakingHeart.NATURAL, meta >= states.length);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int meta = state.getValue(BlockCreakingHeart.HEART_STATE).ordinal();
        if (state.getValue(BlockCreakingHeart.NATURAL)) meta += BlockCreakingHeart.EnumHeartState.values().length;
        return meta;
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileCreakingHeart();
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    ///////////////////////////////////////////
    // HARVEST / DROPS (mirrors BlockCreakingHeart, since we don't inherit it)
    ///////////////////////////////////////////

    @Override
    public void onBlockHarvested(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player) {
        TileEntity tileentity = worldIn.getTileEntity(pos);

        if (tileentity instanceof TileCreakingHeart) {
            Object creaking = ((TileCreakingHeart) tileentity).getCreaking();
            if (creaking instanceof com.sirsquidly.palebloom.common.entity.EntityCreaking) {
                com.sirsquidly.palebloom.common.entity.EntityCreaking c = (com.sirsquidly.palebloom.common.entity.EntityCreaking) creaking;
                c.setLastAttackedEntity(player);
                c.preformTwitchingDeath(43);
            }
        }
        super.onBlockHarvested(worldIn, pos, state, player);
    }

    @Override
    protected boolean canSilkHarvest() {
        return true;
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return JTPGItems.RESIN_CLUMP;
    }

    @Override
    public int quantityDropped(Random random) {
        return 1 + random.nextInt(3);
    }

    @Override
    public int quantityDroppedWithBonus(int fortune, Random random) {
        return this.quantityDropped(random) + random.nextInt(fortune + 1);
    }

    @Override
    public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state, @Nullable TileEntity te, ItemStack stack) {
        super.harvestBlock(worldIn, player, pos, state, te, stack);
        if (ConfigCache.crkHrt_alertReapingWillows) WorldPaleGarden.alertReapingWillow(worldIn, pos, player, 16);

        if (state.getValue(BlockCreakingHeart.NATURAL)) {
            if (ConfigCache.crkHrt_dropAmberValveNatural) spawnAsEntity(worldIn, pos, new ItemStack(JTPGItems.AMBER_VALVE));

            int i = MathHelper.getInt(worldIn.rand, 20, 24);
            while (i > 0) {
                int j = EntityXPOrb.getXPSplit(i);
                i -= j;
                worldIn.spawnEntity(new EntityXPOrb(worldIn, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, j));
            }
        }
    }

    @Override
    public boolean hasComparatorInputOverride(IBlockState state) {
        return true;
    }

    @Override
    public int getComparatorInputOverride(IBlockState blockState, World worldIn, BlockPos pos) {
        TileEntity tileentity = worldIn.getTileEntity(pos);
        if (tileentity instanceof TileCreakingHeart) return ((TileCreakingHeart) tileentity).getComparatorOutput();
        return 0;
    }
}
