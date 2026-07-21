package nlblackeagle.dynamictreespalebloom.worldgen;

import com.sirsquidly.palebloom.common.blocks.BlockPalePetals;
import com.sirsquidly.palebloom.init.JTPGBiomes;
import com.sirsquidly.palebloom.init.JTPGBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.common.IWorldGenerator;
import nlblackeagle.dynamictreespalebloom.config.ForgeConfigHandler;

import java.util.Random;

// Scattered, flower-like ground decoration across the whole Pale Garden -
// not tied to trees at all, unlike Sucker Roots. One roll per chunk against
// the configured chance; on success, a small clustered patch generates,
// mirroring vanilla's own flower-patch density/spread conventions.
public class WorldGenPalePetals implements IWorldGenerator {

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
        if (world.provider.getDimension() != 0) return;

        BlockPos chunkCenter = new BlockPos((chunkX << 4) + 8, 0, (chunkZ << 4) + 8);
        Biome biome = world.getBiome(chunkCenter);
        if (biome != JTPGBiomes.PALE_GARDEN) return;

        if (random.nextFloat() >= ForgeConfigHandler.biomeDecoration.palePetalsChance) return;

        int attempts = 4 + random.nextInt(5); // small clustered patch, matching vanilla flower density
        for (int i = 0; i < attempts; i++) {
            int x = (chunkX << 4) + random.nextInt(16);
            int z = (chunkZ << 4) + random.nextInt(16);
            BlockPos surface = world.getHeight(new BlockPos(x, 0, z));
            BlockPos groundPos = surface.down();

            if (!world.isAirBlock(surface)) continue;

            Block ground = world.getBlockState(groundPos).getBlock();
            boolean validGround = ground == JTPGBlocks.PALE_MOSS
                    || ground == Blocks.GRASS
                    || ground == Blocks.DIRT
                    || world.getBlockState(groundPos).getMaterial() == Material.GRASS;

            if (!validGround) continue;

            int amount = 1 + random.nextInt(4);
            EnumFacing facing = EnumFacing.Plane.HORIZONTAL.random(random);

            world.setBlockState(surface, JTPGBlocks.PALE_PETALS.getDefaultState()
                    .withProperty(BlockPalePetals.AMOUNT, amount)
                    .withProperty(BlockPalePetals.FACING, facing));
        }
    }
}
