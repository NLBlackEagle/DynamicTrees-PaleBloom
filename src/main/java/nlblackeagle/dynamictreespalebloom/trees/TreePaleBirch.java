package nlblackeagle.dynamictreespalebloom.trees;

import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.trees.TreeFamily;
import com.sirsquidly.palebloom.init.JTPGBlocks;
import nlblackeagle.dynamictreespalebloom.DynamicTreesPaleBloom;
import nlblackeagle.dynamictreespalebloom.ModContent;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

// Peeping Birch - a genuinely separate species from Pale Oak, with its own
// dedicated log/leaves textures. Not thick (normal DT radius cap of 8 is
// plenty), no Creaking Heart association (that's Pale Oak-specific lore).
// Growth parameters match DT core's own real "Tall Birch" variant exactly,
// since "generates taller" was the explicit requirement.
public class TreePaleBirch extends TreeFamily {

    public static Block logBlock = JTPGBlocks.PEEPING_BIRCH_LOG;

    public class SpeciesPaleBirch extends Species {

        SpeciesPaleBirch(TreeFamily treeFamily) {
            super(treeFamily.getName(), treeFamily, ModContent.paleBirchLeavesProperties);

            // Matches DT core's real SpeciesTallBirch exactly:
            // setBasicGrowingParameters(0.08F, 24.0F, 7, 7, 1.3F)
            setBasicGrowingParameters(0.08f, 24.0f, 7, 7, 1.3f);

            generateSeed();
            setupStandardSeedDropping();

            // FeatureGenBirchStub removed permanently. Tried four separate
            // approaches (static foreign log, growth-signal setRadius(),
            // real radius-matched branch, real branch + leaf-tip
            // reinforcement) - each either damaged the trunk's own structure/
            // rendering or required a visible leaf on what should be a bare
            // stump. Not worth it for a small decorative flourish.

            ModContent.paleBirchLeavesProperties.setTree(treeFamily);
        }
    }

    public TreePaleBirch() {
        super(new ResourceLocation(DynamicTreesPaleBloom.MODID, "pale_birch"));

        setPrimitiveLog(logBlock.getDefaultState());

        ModContent.paleBirchLeavesProperties.setTree(this);
    }

    @Override
    public ItemStack getPrimitiveLogItemStack(int qty) {
        ItemStack stack = new ItemStack(logBlock, 1, 0);
        stack.setCount(MathHelper.clamp(qty, 0, 64));
        return stack;
    }

    @Override
    public void createSpecies() {
        setCommonSpecies(new SpeciesPaleBirch(this));
    }
}
