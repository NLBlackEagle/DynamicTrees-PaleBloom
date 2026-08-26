package nlblackeagle.dynamictreespalebloom.worldgen;

import com.sirsquidly.palebloom.common.blocks.BlockDoublePalePlant;
import com.sirsquidly.palebloom.common.blocks.BlockIncenseThorn;
import com.sirsquidly.palebloom.common.blocks.BlockPollenhead;
import com.sirsquidly.palebloom.common.blocks.IGardenState;
import com.sirsquidly.palebloom.init.JTPGBiomes;
import com.sirsquidly.palebloom.init.JTPGBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.common.IWorldGenerator;
import nlblackeagle.dynamictreespalebloom.DynamicTreesPaleBloom;
import nlblackeagle.dynamictreespalebloom.config.ForgeConfigHandler;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.function.Predicate;
import java.util.logging.Logger;

/**
 * Moves a batch of flora that used to be OTG Plant(...) resources onto this addon's
 * own worldgen pass, in the Pale Garden.
 * <p>
 * Each pool attempt does a single top-to-bottom column scan (same technique as
 * WorldGenCaveHangingMoss's ceiling search, just inverted to look at the block below
 * instead of above) over the entry's Y range, reservoir-sampling one valid "exposed
 * Grass or Pale Moss ground" position - this naturally catches both true surface
 * exposure and any cave floor exposed within that range in one bounded pass, instead
 * of OTG's usual random-position-then-height-check approach.
 * <p>
 * Double-tall plants (Pale Plant Double, Pollenhead) go through their real
 * placeDoubleAt(...) API rather than a raw setBlockState, same reasoning as
 * PaleLungSeedBomb's own flora scatter - a plain placement only ever sets the bottom
 * half. Incense Thorns doesn't need its potion set here at all any more:
 * TileIncenseThornMixin's "Default Unset Incense Thorns To Poison" already defaults
 * any un-potioned Incense Thorns to Poison (or Pale Lung) the first time it tries to
 * run its aura.
 */
public class WorldGenCaveFlora implements IWorldGenerator {

    private static final Logger LOGGER = Logger.getLogger(DynamicTreesPaleBloom.MODID);

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
        if (world.provider.getDimension() != 0) return;

        BlockPos chunkCenter = new BlockPos((chunkX << 4) + 8, 0, (chunkZ << 4) + 8);
        Biome biome = world.getBiome(chunkCenter);
        if (biome != JTPGBiomes.PALE_GARDEN) return;

        for (String entry : ForgeConfigHandler.caveFlora.caveFloraPool) {
            PoolEntry parsed = parseEntry(entry);
            if (parsed == null) continue;

            Predicate<IBlockState> groundCheck = state -> {
                for (GroundType type : parsed.groundTypes) {
                    if (type.matches(state)) return true;
                }
                return false;
            };
            int neededHeight = (parsed.block instanceof BlockDoublePalePlant || parsed.block instanceof BlockPollenhead) ? 2 : 1;

            for (int i = 0; i < parsed.frequency; i++) {
                int x = (chunkX << 4) + random.nextInt(16);
                int z = (chunkZ << 4) + random.nextInt(16);

                BlockPos spot = findGroundSpot(world, x, z, parsed.minY, parsed.maxY, groundCheck, neededHeight, random);
                if (spot == null) continue;
                if (random.nextDouble() * 100.0 >= parsed.rarity) continue;

                place(world, spot, parsed.block, parsed.meta);

                if (parsed.block instanceof BlockIncenseThorn) {
                    maybePlaceResinBulbNearby(world, spot, random);
                }
            }
        }

        scatterMossGround(world, chunkX, chunkZ, random);
    }

    // ------------------------------------------------------------------
    // Ground-spot finding - single top-to-bottom column scan, reservoir-sampling one
    // valid position out of however many turn up in that one pass.
    // ------------------------------------------------------------------

    private static BlockPos findGroundSpot(World world, int x, int z, int minY, int maxY, Predicate<IBlockState> groundCheck, int neededHeight, Random random) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, minY, z);
        BlockPos chosen = null;
        int found = 0;

        for (int y = maxY; y >= minY; y--) {
            pos.setPos(x, y, z);
            if (!world.isAirBlock(pos)) continue;

            boolean clearAbove = true;
            for (int h = 1; h < neededHeight; h++) {
                if (!world.isAirBlock(pos.up(h))) {
                    clearAbove = false;
                    break;
                }
            }
            if (!clearAbove) continue;

            BlockPos below = pos.down();
            IBlockState belowState = world.getBlockState(below);

            // Require an actual solid, full-cube surface (same rule used for the
            // hanging moss ceiling attachment) - this is what stops a later pool
            // entry's scan from treating an earlier one's freshly-placed bramble,
            // eyeblossom, etc. as valid "ground" and stacking on top of it: every
            // Pale Bloom flora block is a thin bush shape with no solid face, so
            // this excludes them all regardless of what their Material happens to be.
            if (belowState.getBlockFaceShape(world, below, EnumFacing.UP) != BlockFaceShape.SOLID) continue;
            if (!groundCheck.test(belowState)) continue;

            found++;
            if (random.nextInt(found) == 0) {
                chosen = pos.toImmutable();
            }
        }

        return chosen;
    }

    private enum GroundType {
        GRASS, MOSS;

        boolean matches(IBlockState state) {
            Block block = state.getBlock();
            switch (this) {
                case GRASS:
                    return block == Blocks.GRASS || block == Blocks.DIRT || state.getMaterial() == Material.GRASS;
                case MOSS:
                    return block == JTPGBlocks.PALE_MOSS;
                default:
                    return false;
            }
        }
    }

    // ------------------------------------------------------------------
    // Placement
    // ------------------------------------------------------------------

    private static void place(World world, BlockPos pos, Block block, int meta) {
        if (block instanceof BlockDoublePalePlant) {
            // placeDoubleAt(World, BlockPos, typeMeta, awake, flags) - places both
            // halves correctly, unlike a raw setBlockState (see PaleLungSeedBomb).
            ((BlockDoublePalePlant) block).placeDoubleAt(world, pos, meta, false, 2);
            return;
        }

        if (block instanceof BlockPollenhead) {
            IGardenState.EnumLucidityState[] states = IGardenState.EnumLucidityState.values();
            IGardenState.EnumLucidityState state = (meta >= 0 && meta < states.length)
                    ? states[meta]
                    : IGardenState.EnumLucidityState.DORMANT;
            ((BlockPollenhead) block).placeDoubleAt(world, pos, state, 2);
            return;
        }

        IBlockState state;
        try {
            state = block.getStateFromMeta(meta);
        } catch (Exception e) {
            state = block.getDefaultState();
        }

        if (!state.getBlock().canPlaceBlockAt(world, pos)) return;
        world.setBlockState(pos, state, 2);
    }

    // Companion placement: give a freshly-placed Incense Thorns an actual chance of
    // finding resin by putting a Resin Bulb right next to it, independent of - and on
    // top of - the pool's own separate Resin Bulb entry.
    private static void maybePlaceResinBulbNearby(World world, BlockPos thornPos, Random random) {
        if (random.nextDouble() >= ForgeConfigHandler.caveFlora.resinBulbNearIncenseThornsChance) return;

        List<BlockPos> candidates = new ArrayList<>();
        for (EnumFacing facing : EnumFacing.Plane.HORIZONTAL) {
            BlockPos candidate = thornPos.offset(facing);
            if (!world.isAirBlock(candidate)) continue;

            BlockPos below = candidate.down();
            if (!world.getBlockState(below).isSideSolid(world, below, EnumFacing.UP)) continue;

            candidates.add(candidate);
        }
        if (candidates.isEmpty()) return;

        BlockPos chosen = candidates.get(random.nextInt(candidates.size()));
        if (!JTPGBlocks.RESIN_BULB.canPlaceBlockAt(world, chosen)) return;
        world.setBlockState(chosen, JTPGBlocks.RESIN_BULB.getDefaultState(), 2);
    }

    // ------------------------------------------------------------------
    // Cave moss ground - replaces exposed ground (not a flora entry, since it
    // replaces the floor itself rather than sitting on top of it) with Pale Moss
    // Block, same single-column-scan technique. Targets stone/dirt/grass-family
    // ground specifically, not just already-grassy patches, since bare cave stone is
    // the actual dominant floor material this is meant to soften.
    // ------------------------------------------------------------------

    private static void scatterMossGround(World world, int chunkX, int chunkZ, Random random) {
        int attempts = ForgeConfigHandler.caveFlora.caveMossGroundAttemptsPerChunk;
        int minY = ForgeConfigHandler.caveFlora.caveMossGroundMinY;
        int maxY = ForgeConfigHandler.caveFlora.caveMossGroundMaxY;
        double rarity = ForgeConfigHandler.caveFlora.caveMossGroundRarity;

        Predicate<IBlockState> mossableGround = state -> {
            Block block = state.getBlock();
            if (block == JTPGBlocks.PALE_MOSS) return false;
            Material material = state.getMaterial();
            return material == Material.ROCK || material == Material.GRASS || block == Blocks.DIRT;
        };

        for (int i = 0; i < attempts; i++) {
            int x = (chunkX << 4) + random.nextInt(16);
            int z = (chunkZ << 4) + random.nextInt(16);

            BlockPos spot = findGroundSpot(world, x, z, minY, maxY, mossableGround, 1, random);
            if (spot == null) continue;
            if (random.nextDouble() * 100.0 >= rarity) continue;

            world.setBlockState(spot.down(), JTPGBlocks.PALE_MOSS.getDefaultState(), 2);
        }
    }

    // ------------------------------------------------------------------
    // Pool entry parsing - "modid:block[:meta],frequency,rarity,minY,maxY,groundTypes"
    // ------------------------------------------------------------------

    private static class PoolEntry {
        final Block block;
        final int meta;
        final int frequency;
        final double rarity;
        final int minY;
        final int maxY;
        final EnumSet<GroundType> groundTypes;

        PoolEntry(Block block, int meta, int frequency, double rarity, int minY, int maxY, EnumSet<GroundType> groundTypes) {
            this.block = block;
            this.meta = meta;
            this.frequency = frequency;
            this.rarity = rarity;
            this.minY = minY;
            this.maxY = maxY;
            this.groundTypes = groundTypes;
        }
    }

    private static PoolEntry parseEntry(String entry) {
        String[] parts = entry.split(",");
        if (parts.length < 6) {
            LOGGER.warning("[" + DynamicTreesPaleBloom.MODID + "] Invalid Cave Flora Pool entry (expected \"modid:block[:meta],frequency,rarity,minY,maxY,groundTypes\"): " + entry);
            return null;
        }

        try {
            String[] blockParts = parts[0].trim().split(":");
            if (blockParts.length < 2) {
                LOGGER.warning("[" + DynamicTreesPaleBloom.MODID + "] Invalid Cave Flora Pool block spec: " + parts[0]);
                return null;
            }
            ResourceLocation loc = new ResourceLocation(blockParts[0], blockParts[1]);
            Block block = Block.REGISTRY.getObject(loc);
            if (block == null || block == Blocks.AIR) {
                LOGGER.warning("[" + DynamicTreesPaleBloom.MODID + "] Unknown Cave Flora Pool block: " + loc);
                return null;
            }
            int meta = blockParts.length >= 3 ? Integer.parseInt(blockParts[2]) : 0;

            int frequency = Integer.parseInt(parts[1].trim());
            double rarity = Double.parseDouble(parts[2].trim());
            int minY = Integer.parseInt(parts[3].trim());
            int maxY = Integer.parseInt(parts[4].trim());

            EnumSet<GroundType> groundTypes = EnumSet.noneOf(GroundType.class);
            for (String typeName : parts[5].trim().split("\\|")) {
                try {
                    groundTypes.add(GroundType.valueOf(typeName.trim().toUpperCase(Locale.ROOT)));
                } catch (IllegalArgumentException e) {
                    LOGGER.warning("[" + DynamicTreesPaleBloom.MODID + "] Unknown Cave Flora Pool ground type: " + typeName);
                }
            }
            if (groundTypes.isEmpty()) return null;

            return new PoolEntry(block, meta, frequency, rarity, minY, maxY, groundTypes);
        } catch (NumberFormatException e) {
            LOGGER.warning("[" + DynamicTreesPaleBloom.MODID + "] Invalid Cave Flora Pool entry (bad number): " + entry);
            return null;
        }
    }
}
