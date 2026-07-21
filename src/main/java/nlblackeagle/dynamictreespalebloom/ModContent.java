package nlblackeagle.dynamictreespalebloom;

import com.ferreusveritas.dynamictrees.ModItems;
import com.ferreusveritas.dynamictrees.ModRecipes;
import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.api.WorldGenRegistry.BiomeDataBasePopulatorRegistryEvent;
import com.ferreusveritas.dynamictrees.api.client.ModelHelper;
import com.ferreusveritas.dynamictrees.api.treedata.ILeavesProperties;
import com.ferreusveritas.dynamictrees.blocks.BlockRooty;
import com.ferreusveritas.dynamictrees.blocks.LeavesPaging;
import com.ferreusveritas.dynamictrees.blocks.LeavesProperties;
import com.ferreusveritas.dynamictrees.items.DendroPotion.DendroPotionType;
import com.ferreusveritas.dynamictrees.systems.DirtHelper;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.trees.TreeFamily;
import com.sirsquidly.palebloom.common.blocks.base.BlockJTPGSapling;
import com.sirsquidly.palebloom.init.JTPGBlocks;
import net.minecraft.item.ItemBlock;
import nlblackeagle.dynamictreespalebloom.blocks.BlockBranchCreakingHeart;
import nlblackeagle.dynamictreespalebloom.trees.TreePaleOak;
import nlblackeagle.dynamictreespalebloom.worldgen.BiomeDataBasePopulator;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.ArrayList;
import java.util.Collections;

@Mod.EventBusSubscriber(modid = DynamicTreesPaleBloom.MODID)
@ObjectHolder(DynamicTreesPaleBloom.MODID)
public class ModContent {

    public static ILeavesProperties paleOakLeavesProperties;
    public static Block paleOakBranchBlock;
    public static Block paleOakBranchCreakingHeart;
    public static Block paleOakBranchCreakingHeartX;
    public static ILeavesProperties paleBloomingOakLeavesProperties;
    public static Block paleBloomingOakBranchBlock;
    public static Block paleBloomingOakBranchCreakingHeart;
    public static Block paleBloomingOakBranchCreakingHeartX;
    public static ILeavesProperties paleBirchLeavesProperties;
    public static Block paleBirchBranchBlock;
    public static ArrayList<TreeFamily> trees = new ArrayList<TreeFamily>();

    @SubscribeEvent
    public static void registerDataBasePopulators(final BiomeDataBasePopulatorRegistryEvent event) {
        event.register(new BiomeDataBasePopulator());
    }

    @SubscribeEvent
    public static void registerBlocks(final RegistryEvent.Register<Block> event) {
        IForgeRegistry<Block> registry = event.getRegistry();

        DirtHelper.registerSoil(JTPGBlocks.PALE_MOSS, DirtHelper.DIRTLIKE);

        // NOTE: cell kit string below is a best-guess at DT core's built-in
        // "deciduous" kit name - verify against your DT core jar if this throws
        // a lookup failure at runtime.
        paleOakLeavesProperties = new LeavesProperties(
                JTPGBlocks.PALE_OAK_LEAVES.getDefaultState(),
                TreeRegistry.findCellKit("dynamictrees:deciduous")) {
            @Override
            public ItemStack getPrimitiveLeavesItemStack() {
                return new ItemStack(JTPGBlocks.PALE_OAK_LEAVES);
            }
        };

        LeavesPaging.getLeavesBlockForSequence(DynamicTreesPaleBloom.MODID, 0, paleOakLeavesProperties);

        paleBloomingOakLeavesProperties = new LeavesProperties(
                JTPGBlocks.BLOOMING_PALE_OAK_LEAVES.getDefaultState(),
                TreeRegistry.findCellKit("dynamictrees:deciduous")) {
            @Override
            public ItemStack getPrimitiveLeavesItemStack() {
                return new ItemStack(JTPGBlocks.BLOOMING_PALE_OAK_LEAVES);
            }
        };

        LeavesPaging.getLeavesBlockForSequence(DynamicTreesPaleBloom.MODID, 1, paleBloomingOakLeavesProperties);

        paleBirchLeavesProperties = new LeavesProperties(
                JTPGBlocks.PEEPING_BIRCH_LEAVES.getDefaultState(),
                TreeRegistry.findCellKit("dynamictrees:deciduous")) {
            @Override
            public ItemStack getPrimitiveLeavesItemStack() {
                return new ItemStack(JTPGBlocks.PEEPING_BIRCH_LEAVES);
            }
        };

        LeavesPaging.getLeavesBlockForSequence(DynamicTreesPaleBloom.MODID, 2, paleBirchLeavesProperties);

        TreeFamily paleOak = new TreePaleOak();
        paleOakBranchBlock = paleOak.getDynamicBranch();

        BlockBranchCreakingHeart creakingHeartBranch = new BlockBranchCreakingHeart("pale_oak_creaking_heart");
        creakingHeartBranch.setFamily(paleOak);
        paleOak.addValidBranches(creakingHeartBranch);
        paleOakBranchCreakingHeart = creakingHeartBranch;
        registry.register(paleOakBranchCreakingHeart);
        paleOakBranchCreakingHeartX = creakingHeartBranch.otherBlock;
        registry.register(paleOakBranchCreakingHeartX);

        TreeFamily paleBloomingOak = new nlblackeagle.dynamictreespalebloom.trees.TreeBloomingPaleOak();
        paleBloomingOakBranchBlock = paleBloomingOak.getDynamicBranch();

        BlockBranchCreakingHeart bloomingCreakingHeartBranch = new BlockBranchCreakingHeart("pale_blooming_creaking_heart");
        bloomingCreakingHeartBranch.setFamily(paleBloomingOak);
        paleBloomingOak.addValidBranches(bloomingCreakingHeartBranch);
        paleBloomingOakBranchCreakingHeart = bloomingCreakingHeartBranch;
        registry.register(paleBloomingOakBranchCreakingHeart);
        paleBloomingOakBranchCreakingHeartX = bloomingCreakingHeartBranch.otherBlock;
        registry.register(paleBloomingOakBranchCreakingHeartX);

        TreeFamily paleBirch = new nlblackeagle.dynamictreespalebloom.trees.TreePaleBirch();
        paleBirchBranchBlock = paleBirch.getDynamicBranch();

        Collections.addAll(trees, paleOak, paleBloomingOak, paleBirch);

        trees.forEach(tree -> tree.registerSpecies(Species.REGISTRY));
        ArrayList<Block> treeBlocks = new ArrayList<>();
        trees.forEach(tree -> tree.getRegisterableBlocks(treeBlocks));
        treeBlocks.addAll(LeavesPaging.getLeavesMapForModId(DynamicTreesPaleBloom.MODID).values());
        registry.registerAll(treeBlocks.toArray(new Block[0]));
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        IForgeRegistry<Item> registry = event.getRegistry();

        ArrayList<Item> treeItems = new ArrayList<>();
        trees.forEach(tree -> tree.getRegisterableItems(treeItems));
        registry.registerAll(treeItems.toArray(new Item[0]));
        registry.register(new ItemBlock(paleOakBranchCreakingHeart).setRegistryName(paleOakBranchCreakingHeart.getRegistryName()));
        registry.register(new ItemBlock(paleOakBranchCreakingHeartX).setRegistryName(paleOakBranchCreakingHeartX.getRegistryName()));
        registry.register(new ItemBlock(paleBloomingOakBranchCreakingHeart).setRegistryName(paleBloomingOakBranchCreakingHeart.getRegistryName()));
        registry.register(new ItemBlock(paleBloomingOakBranchCreakingHeartX).setRegistryName(paleBloomingOakBranchCreakingHeartX.getRegistryName()));
    }

    @SubscribeEvent
    public static void registerRecipes(RegistryEvent.Register<IRecipe> event) {
        setUpSeedRecipes("pale_oak", new ItemStack(JTPGBlocks.PALE_SAPLING, 1, BlockJTPGSapling.EnumType.PALE_OAK.getMetadata()));
        setUpSeedRecipes("pale_blooming", new ItemStack(JTPGBlocks.PALE_SAPLING, 1, BlockJTPGSapling.EnumType.BLOOMING_PALE_OAK.getMetadata()));
        setUpSeedRecipes("pale_birch", new ItemStack(JTPGBlocks.PALE_SAPLING, 1, BlockJTPGSapling.EnumType.PEEPING_BIRCH.getMetadata()));
    }

    public static void setUpSeedRecipes(String name, ItemStack treeSapling) {
        Species treeSpecies = TreeRegistry.findSpecies(new ResourceLocation(DynamicTreesPaleBloom.MODID, name));
        ItemStack treeSeed = treeSpecies.getSeedStack(1);
        ItemStack treeTransformationPotion = ModItems.dendroPotion.setTargetTree(
                new ItemStack(ModItems.dendroPotion, 1, DendroPotionType.TRANSFORM.getIndex()), treeSpecies.getFamily());
        BrewingRecipeRegistry.addRecipe(new ItemStack(ModItems.dendroPotion, 1, DendroPotionType.TRANSFORM.getIndex()), treeSeed, treeTransformationPotion);
        ModRecipes.createDirtBucketExchangeRecipes(treeSapling, treeSeed, true);
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        for (TreeFamily tree : trees) {
            ModelHelper.regModel(tree.getDynamicBranch());
            ModelHelper.regModel(tree.getCommonSpecies().getSeed());
            ModelHelper.regModel(tree);
        }
        LeavesPaging.getLeavesMapForModId(DynamicTreesPaleBloom.MODID).forEach((key, leaves) ->
                ModelLoader.setCustomStateMapper(leaves, new StateMap.Builder().ignore(BlockLeaves.DECAYABLE).build()));

        nlblackeagle.dynamictreespalebloom.blocks.BlockBranchCreakingHeart.registerStateMapper(paleOakBranchCreakingHeart);
        ModelHelper.regModel(net.minecraft.item.Item.getItemFromBlock(paleOakBranchCreakingHeart));
        nlblackeagle.dynamictreespalebloom.blocks.BlockBranchCreakingHeart.registerStateMapper(paleOakBranchCreakingHeartX);
        ModelHelper.regModel(net.minecraft.item.Item.getItemFromBlock(paleOakBranchCreakingHeartX));

        nlblackeagle.dynamictreespalebloom.blocks.BlockBranchCreakingHeart.registerStateMapper(paleBloomingOakBranchCreakingHeart);
        ModelHelper.regModel(net.minecraft.item.Item.getItemFromBlock(paleBloomingOakBranchCreakingHeart));
        nlblackeagle.dynamictreespalebloom.blocks.BlockBranchCreakingHeart.registerStateMapper(paleBloomingOakBranchCreakingHeartX);
        ModelHelper.regModel(net.minecraft.item.Item.getItemFromBlock(paleBloomingOakBranchCreakingHeartX));
    }
}