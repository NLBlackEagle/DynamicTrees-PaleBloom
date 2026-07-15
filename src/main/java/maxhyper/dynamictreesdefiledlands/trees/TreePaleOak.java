package maxhyper.dynamictreesdefiledlands.trees;

import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.trees.TreeFamily;
import com.sirsquidly.palebloom.init.JTPGBlocks;
import maxhyper.dynamictreesdefiledlands.DynamicTreesDefiledLands;
import maxhyper.dynamictreesdefiledlands.ModContent;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

public class TreePaleOak extends TreeFamily {

    public static Block logBlock = JTPGBlocks.PALE_OAK_LOG;

    public class SpeciesPaleOak extends Species {

        SpeciesPaleOak(TreeFamily treeFamily) {
            super(treeFamily.getName(), treeFamily, ModContent.paleOakLeavesProperties);

            // Placeholder growth numbers - tune these in the "make it look right" pass.
            // (energy, max height, max branch radius, lowest branch height, tapering)
            setBasicGrowingParameters(0.45f, 22.0f, 8, 6, 0.8f);

            generateSeed();
            setupStandardSeedDropping();

            ModContent.paleOakLeavesProperties.setTree(treeFamily);
        }

        // TODO: underground growth exceptions go here later - likely an override of
        // setStandardSoils() to accept Pale Bloom's moss/podzol-like blocks, and/or
        // an override of isBiomePerfect / a light-requirement check so it can grow
        // without direct sky access. Left as default (normal surface dirt) for now.
    }

    public TreePaleOak() {
        super(new ResourceLocation(DynamicTreesDefiledLands.MODID, "pale_oak"));

        setPrimitiveLog(logBlock.getDefaultState());

        ModContent.paleOakLeavesProperties.setTree(this);
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