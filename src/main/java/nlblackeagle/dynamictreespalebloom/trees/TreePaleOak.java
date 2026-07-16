package nlblackeagle.dynamictreespalebloom.trees;

import com.ferreusveritas.dynamictrees.ModTrees;
import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.blocks.BlockBranch;
import com.ferreusveritas.dynamictrees.blocks.BlockBranchThick;
import com.ferreusveritas.dynamictrees.systems.featuregen.FeatureGenFlareBottom;
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

public class TreePaleOak extends TreeFamily {

    public static Block logBlock = JTPGBlocks.PALE_OAK_LOG;

    public class SpeciesPaleOak extends Species {

        SpeciesPaleOak(TreeFamily treeFamily) {
            super(treeFamily.getName(), treeFamily, ModContent.paleOakLeavesProperties);

            setBasicGrowingParameters(0.30f, 18.0f, 4, 6, 0.8f);
            setGrowthLogicKit(TreeRegistry.findGrowthLogicKit(ModTrees.DARKOAK));

            generateSeed();
            setupStandardSeedDropping();

            addGenFeature(new FeatureGenFlareBottom());
            addGenFeature(new FeatureGenCreakingHeart(0.01f, 0.10f, 1.0f, 4, 16));

            ModContent.paleOakLeavesProperties.setTree(treeFamily);
        }

        @Override
        public boolean isThick() {
            return true;
        }
    }

    public TreePaleOak() {
        super(new ResourceLocation(DynamicTreesPaleBloom.MODID, "pale_oak"));

        setPrimitiveLog(logBlock.getDefaultState());

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