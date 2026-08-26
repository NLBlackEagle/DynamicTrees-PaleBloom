package nlblackeagle.dynamictreespalebloom.trees;

import com.ferreusveritas.dynamictrees.ModTrees;
import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.blocks.BlockBranch;
import com.ferreusveritas.dynamictrees.blocks.BlockBranchThick;
import com.ferreusveritas.dynamictrees.blocks.BlockSurfaceRoot;
import com.ferreusveritas.dynamictrees.systems.featuregen.FeatureGenClearVolume;
import com.ferreusveritas.dynamictrees.systems.featuregen.FeatureGenFlareBottom;
import com.ferreusveritas.dynamictrees.systems.featuregen.FeatureGenMound;
import com.ferreusveritas.dynamictrees.systems.featuregen.FeatureGenRoots;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.trees.TreeFamily;
import com.sirsquidly.palebloom.init.JTPGBlocks;
import nlblackeagle.dynamictreespalebloom.DynamicTreesPaleBloom;
import nlblackeagle.dynamictreespalebloom.ModContent;
import nlblackeagle.dynamictreespalebloom.config.ForgeConfigHandler;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.List;
import java.util.function.BiFunction;

// Bigger/older-looking variant of Pale Oak - reuses the same trunk texture
// (Pale Bloom's real Blooming variant only differs in leaves/sapling, no
// separate log block exists), but with taller/thicker growth parameters and
// its own separate Creaking Heart chance (config: BloomingPaleOakConfig).
public class TreeBloomingPaleOak extends TreeFamily {

    public static Block logBlock = JTPGBlocks.PALE_OAK_LOG;

    public class SpeciesBloomingPaleOak extends Species {

        private final FeatureGenSuckerRoots suckerRootsFeature = new FeatureGenSuckerRoots(
                (float) ForgeConfigHandler.bloomingPaleOak.suckerNoduleChance);

        SpeciesBloomingPaleOak(TreeFamily treeFamily) {
            super(treeFamily.getName(), treeFamily, ModContent.paleBloomingOakLeavesProperties);

            // Bigger/older than regular Pale Oak (0.30, 18.0, 4, 6, 0.8):
            // taller ceiling, thicker trunk target, taller bare-trunk-before-
            // branching for a more "old growth" silhouette.
            setBasicGrowingParameters(0.30f, 25.0f, 10, 9, 0.8f);
            setGrowthLogicKit(TreeRegistry.findGrowthLogicKit(ModTrees.DARKOAK));

            generateSeed();
            setupStandardSeedDropping();

            addGenFeature(new FeatureGenClearVolume(10));
            addGenFeature(new FeatureGenFlareBottom());
            addGenFeature(new FeatureGenMound(9));
            addGenFeature(new FeatureGenCreakingHeart(
                    (float) ForgeConfigHandler.bloomingPaleOak.creakingHeartGrowthChance,
                    (float) ForgeConfigHandler.bloomingPaleOak.creakingHeartWorldgenChance,
                    (float) ForgeConfigHandler.bloomingPaleOak.creakingHeartUndergroundChance,
                    ForgeConfigHandler.bloomingPaleOak.creakingHeartMinTrunkRadius,
                    16));
            addGenFeature(suckerRootsFeature); // worldgen path (postGeneration)
            addGenFeature(new FeatureGenRoots(13).setScaler(getRootScaler()));

            ModContent.paleBloomingOakLeavesProperties.setTree(treeFamily);
        }

        // The real, genuinely one-time "sapling became a tree" event - covers
        // both organic growth and bonemeal, since both call this same method.
        // Not postGrow, which fires repeatedly for the tree's whole life.
        @Override
        public boolean transitionToTree(World world, BlockPos pos) {
            boolean result = super.transitionToTree(world, pos);
            if (result && !world.isRemote) {
                suckerRootsFeature.generateSuckerFeature(world, pos, world.rand);
            }
            return result;
        }

        protected BiFunction<Integer, Integer, Integer> getRootScaler() {
            return (inRadius, trunkRadius) -> {
                float scale = MathHelper.clamp(trunkRadius >= 13 ? (trunkRadius / 24f) : 0, 0, 1);
                return (int) (inRadius * scale);
            };
        }

        @Override
        public boolean isThick() {
            return true;
        }
    }

    BlockSurfaceRoot surfaceRootBlock;

    public TreeBloomingPaleOak() {
        super(new ResourceLocation(DynamicTreesPaleBloom.MODID, "pale_blooming"));

        setPrimitiveLog(logBlock.getDefaultState());

        surfaceRootBlock = new BlockSurfaceRoot(Material.WOOD, getName() + "root");

        ModContent.paleBloomingOakLeavesProperties.setTree(this);
    }

    @Override
    public boolean isThick() {
        return true;
    }

    @Override
    public BlockBranch createBranch() {
        String branchName = getName() + "branch";
        return new BlockBranchBloomingPaleOak(branchName);
    }

    protected class BlockBranchBloomingPaleOak extends BlockBranchThick {

        public BlockBranchBloomingPaleOak(String name) {
            this(Material.WOOD, name);
        }

        public BlockBranchBloomingPaleOak(Material material, String name) {
            super(material, name, false);
            otherBlock = new BlockBranchBloomingPaleOak(material, name + "x", true);
            otherBlock.otherBlock = this;

            cacheBranchThickStates();
        }

        protected BlockBranchBloomingPaleOak(Material material, String name, boolean extended) {
            super(material, name, extended);
        }
    }

    @Override
    public List<Block> getRegisterableBlocks(List<Block> blockList) {
        blockList = super.getRegisterableBlocks(blockList);
        blockList.add(surfaceRootBlock);
        return blockList;
    }

    @Override
    public BlockSurfaceRoot getSurfaceRoots() {
        return surfaceRootBlock;
    }

    @Override
    public ItemStack getPrimitiveLogItemStack(int qty) {
        ItemStack stack = new ItemStack(logBlock, 1, 0);
        stack.setCount(MathHelper.clamp(qty, 0, 64));
        return stack;
    }

    @Override
    public void createSpecies() {
        setCommonSpecies(new SpeciesBloomingPaleOak(this));
    }
}
