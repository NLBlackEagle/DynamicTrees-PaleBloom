package nlblackeagle.dynamictreespalebloom.worldgen;

import com.sirsquidly.palebloom.init.JTPGBiomes;
import com.sirsquidly.palebloom.init.JTPGBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.common.IWorldGenerator;
import nlblackeagle.dynamictreespalebloom.config.ForgeConfigHandler;

import java.util.Random;

// Mirrors vanilla's real WorldGenPumpkin/genDecorationPumpkin algorithm,
// confirmed against the Minecraft Wiki's pumpkin generation documentation:
// a 1-in-32 per-chunk gate (scaled by our multiplier), and if triggered, up
// to 64 candidate positions searched for a single valid spot (air above
// grass-like ground) - the first valid spot found gets one pumpkin, then
// generation stops for that chunk. At multiplier 1.0 this matches vanilla's
// real rate exactly; 0.0 disables it entirely.
public class WorldGenPalePumpkin implements IWorldGenerator {

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
        if (world.provider.getDimension() != 0) return;

        double multiplier = ForgeConfigHandler.biomeDecoration.palePumpkinChance;
        if (multiplier <= 0.0) return;

        BlockPos chunkCenter = new BlockPos((chunkX << 4) + 8, 0, (chunkZ << 4) + 8);
        Biome biome = world.getBiome(chunkCenter);
        if (biome != JTPGBiomes.PALE_GARDEN) return;

        // Vanilla's real per-chunk gate is 1/32 - scaled here by our multiplier.
        if (random.nextDouble() >= multiplier / 32.0) return;

        BlockPos origin = new BlockPos((chunkX << 4) + 8 + random.nextInt(16), 0, (chunkZ << 4) + 8 + random.nextInt(16));

        for (int i = 0; i < 64; ++i) {
            BlockPos pos = origin.add(random.nextInt(8) - random.nextInt(8), random.nextInt(4) - random.nextInt(4), random.nextInt(8) - random.nextInt(8));
            BlockPos surface = world.getHeight(pos);

            if (!world.isAirBlock(surface)) continue;

            Block ground = world.getBlockState(surface.down()).getBlock();
            boolean validGround = ground == JTPGBlocks.PALE_MOSS
                    || ground == Blocks.GRASS
                    || ground == Blocks.DIRT
                    || world.getBlockState(surface.down()).getMaterial() == Material.GRASS;

            if (!validGround) continue;

            world.setBlockState(surface, JTPGBlocks.PALE_PUMPKIN.getDefaultState(), 2);
            return; // real vanilla places one pumpkin per successful chunk roll, not a scatter
        }
    }
}
