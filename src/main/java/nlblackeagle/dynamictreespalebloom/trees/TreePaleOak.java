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
import nlblackeagle.dynamictreespalebloom.trees.FeatureGenCreakingHeart;
import nlblackeagle.dynamictreespalebloom.ModContent;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

import java.util.List;
import java.util.function.BiFunction;

public class TreePaleOak extends TreeFamily {

    public static Block logBlock = JTPGBlocks.PALE_OAK_LOG;

    public class SpeciesPaleOak extends Species {

        SpeciesPaleOak(TreeFamily treeFamily) {
            super(treeFamily.getName(), treeFamily, ModContent.paleOakLeavesProperties);

            setBasicGrowingParameters(0.30f, 18.0f, 4, 6, 0.8f);
            setGrowthLogicKit(TreeRegistry.findGrowthLogicKit(ModTrees.DARKOAK));

            generateSeed();
            setupStandardSeedDropping();

            // Matches Dark Oak's real gen-feature set exactly:
            addGenFeature(new FeatureGenClearVolume(6));      // Clear a spot for the thick trunk
            addGenFeature(new FeatureGenFlareBottom());       // Flare the bottom
            addGenFeature(new FeatureGenMound(5));            // Root mound + fixes overhanging trunks near drop-offs
            addGenFeature(new FeatureGenCreakingHeart(0.01f, 0.10f, 1.0f, 4, 16));
            addGenFeature(new FeatureGenRoots(13).setScaler(getRootScaler())); // Surface roots, added last like Dark Oak

            ModContent.paleOakLeavesProperties.setTree(treeFamily);
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

    public TreePaleOak() {
        super(new ResourceLocation(DynamicTreesPaleBloom.MODID, "pale_oak"));

        setPrimitiveLog(logBlock.getDefaultState());

        surfaceRootBlock = new BlockSurfaceRoot(Material.WOOD, getName() + "root");

        ModContent.paleOakLeavesProperties.setTree(this);
    }

    @Override
    public boolean isThick() {
        return true;
    }

    @Override
    public BlockBranch createBranch() {
        String branchName = getName() + "branch";
        return new BlockBranchPaleOak(branchName);
    }

    protected class BlockBranchPaleOak extends BlockBranchThick {

        public BlockBranchPaleOak(String name) {
            this(Material.WOOD, name);
        }

        public BlockBranchPaleOak(Material material, String name) {
            super(material, name, false);
            otherBlock = new BlockBranchPaleOak(material, name + "x", true);
            otherBlock.otherBlock = this;

            cacheBranchThickStates();
        }

        protected BlockBranchPaleOak(Material material, String name, boolean extended) {
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
        setCommonSpecies(new SpeciesPaleOak(this));
    }
}
