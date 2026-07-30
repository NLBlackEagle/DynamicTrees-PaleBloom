package nlblackeagle.dynamictreespalebloom.potion;

import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.sirsquidly.palebloom.common.blocks.BlockDoublePalePlant;
import com.sirsquidly.palebloom.common.blocks.BlockIncenseThorn;
import com.sirsquidly.palebloom.common.blocks.BlockPollenhead;
import com.sirsquidly.palebloom.common.blocks.IGardenState;
import com.sirsquidly.palebloom.common.blocks.tileentity.TileIncenseThorn;
import com.sirsquidly.palebloom.common.entity.item.EntitySeedBomb;
import com.sirsquidly.palebloom.config.ConfigParser;
import com.sirsquidly.palebloom.init.JTPGBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import nlblackeagle.dynamictreespalebloom.DynamicTreesPaleBloom;
import nlblackeagle.dynamictreespalebloom.config.ForgeConfigHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;

/**
 * Shared "Seed Bomb" behaviour used by both
 * {@link nlblackeagle.dynamictreespalebloom.event.ReapingWillowDeathHandler} and
 * {@link nlblackeagle.dynamictreespalebloom.event.PaleLungDeathHandler}: applying Pale
 * Lung in a radius, scattering configured flora, and spawning a real EntitySeedBomb so
 * Pale Bloom's own native explosion logic (Pale Moss spread, Wither, Creeper
 * conversion, sounds/particles) runs too.
 * <p>
 * The EntitySeedBomb(World, double, double, double, EntityLivingBase) constructor
 * (confirmed directly from the shipped palebloom-1.0.0.jar) takes a plain
 * EntityLivingBase, not EntityReapingWillow specifically - ReapingWillowDeathHandler's
 * cast to EntityReapingWillow was just an unnecessarily-specific but harmless
 * narrowing, not a requirement. That means the exact same constructor works for any
 * dying entity, so this needs no reflection.
 */
public class PaleLungSeedBomb {

    private static final Logger LOGGER = Logger.getLogger(DynamicTreesPaleBloom.MODID);

    /**
     * Runs the full "seed bomb went off here" behaviour: Pale Lung spread + flora
     * scatter + scheduling a burst of upward-floating Cloud particles to line up with
     * the actual detonation + a genuinely 3D/spherical Pale Moss spread, all within
     * the given radius, using the given flora pool. Does NOT spawn a real
     * EntitySeedBomb - call {@link #spawnNative} separately alongside this where
     * appropriate.
     * <p>
     * Gated on the "Enable Seed Bomb" master toggle here rather than in each caller,
     * since both PaleLungDeathHandler and ReapingWillowDeathHandler call this - one
     * check covers both without touching ReapingWillowDeathHandler's own logic.
     */
    public static void trigger(World world, BlockPos pos, Entity exclude, String[] floraPool, double radius) {
        if (!ForgeConfigHandler.featureToggles.enableSeedBomb) {
            return;
        }

        applyPaleLung(world, pos, exclude, radius);
        scatterFlora(world, pos, floraPool, radius);
        scheduleDeathParticles(world, pos, radius);
        spreadVerticalMoss(world, pos, radius);
    }

    // ------------------------------------------------------------------
    // Vertical/spherical moss spread
    // ------------------------------------------------------------------

    // Pale Bloom's own native moss spread (WorldGenMoss, see EntitySeedBombMixin) is
    // purely 2D: it picks X/Z columns within a flat circle, then walks up/down each
    // column to find the surface and paints exactly one moss block there. It has no
    // real concept of "up" or "down" beyond following terrain, so it can never spread
    // across multiple vertical layers, up a cliff face, or onto a ceiling.
    //
    // This runs as a fully separate, additive pass rather than trying to rewrite Pale
    // Bloom's own algorithm: it scans every block position in an actual 3D sphere
    // around the detonation point and replaces any block already on Pale Bloom's own
    // PaleMossReplacableList that has air directly above it with Pale Moss - the same
    // "ground moss" placement rule the native version uses, just no longer restricted
    // to one flat layer.
    private static void spreadVerticalMoss(World world, BlockPos center, double radius) {
        if (!ForgeConfigHandler.seedBomb.seedBombVerticalMoss) {
            return;
        }
        if (world.isRemote) {
            return;
        }

        int r = (int) Math.ceil(radius);
        double radiusSq = radius * radius;
        IBlockState mossBlock = JTPGBlocks.PALE_MOSS.getDefaultState();

        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -r; dy <= r; dy++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (dx * dx + dy * dy + dz * dz > radiusSq) {
                        continue;
                    }

                    BlockPos pos = center.add(dx, dy, dz);
                    if (!world.isAirBlock(pos.up())) {
                        continue;
                    }

                    IBlockState state = world.getBlockState(pos);
                    if (!ConfigParser.PaleMossReplacableList.contains(state)) {
                        continue;
                    }

                    world.setBlockState(pos, mossBlock, 2);
                }
            }
        }
    }

    // ------------------------------------------------------------------
    // Death particles
    // ------------------------------------------------------------------

    // A real EntitySeedBomb (fuse forced to 0 in spawnNative/ReapingWillowDeathHandler)
    // doesn't detonate the instant it's spawned - it detonates on its own next world
    // tick, same as vanilla TNT. trigger() runs in the same tick the entity is spawned,
    // so firing the particles immediately would show them a tick before the actual
    // explosion. This queue delays them to land on the right tick instead.
    private static final int PARTICLE_DELAY_TICKS = 1;
    private static final List<ScheduledParticles> SCHEDULED_PARTICLES = new ArrayList<>();

    private static void scheduleDeathParticles(World world, BlockPos pos, double radius) {
        if (!ForgeConfigHandler.seedBomb.seedBombDeathParticles) {
            return;
        }
        if (world.isRemote) {
            return;
        }

        SCHEDULED_PARTICLES.add(new ScheduledParticles(world, pos, radius, PARTICLE_DELAY_TICKS));
    }

    /**
     * Advances all pending scheduled particle bursts for this world by one tick,
     * firing any that are now due. Called from {@link nlblackeagle.dynamictreespalebloom.event.PaleLungSeedBombTickHandler}.
     */
    public static void tick(World world) {
        if (world.isRemote || SCHEDULED_PARTICLES.isEmpty()) {
            return;
        }

        Iterator<ScheduledParticles> it = SCHEDULED_PARTICLES.iterator();
        while (it.hasNext()) {
            ScheduledParticles scheduled = it.next();
            if (scheduled.world != world) {
                continue;
            }

            scheduled.ticksRemaining--;
            if (scheduled.ticksRemaining <= 0) {
                spawnDeathParticlesNow(scheduled.world, scheduled.pos, scheduled.radius);
                it.remove();
            }
        }
    }

    private static class ScheduledParticles {
        final World world;
        final BlockPos pos;
        final double radius;
        int ticksRemaining;

        ScheduledParticles(World world, BlockPos pos, double radius, int ticksRemaining) {
            this.world = world;
            this.pos = pos;
            this.radius = radius;
            this.ticksRemaining = ticksRemaining;
        }
    }

    private static void spawnDeathParticlesNow(World world, BlockPos pos, double radius) {
        if (!(world instanceof WorldServer)) {
            return;
        }

        WorldServer serverWorld = (WorldServer) world;
        Random rand = world.rand;
        double radiusSq = radius * radius;

        // Same sphere constraint as spreadVerticalMoss (dx*dx+dy*dy+dz*dz <= radius^2),
        // via simple rejection sampling: draw a point in the enclosing cube and retry
        // until it actually lands inside the sphere. On average this only rejects
        // ~48% of draws, so it's cheap even for a burst this small. Previously this
        // only varied X/Z in a flat square glued to ground level, which was
        // inconsistent with the genuinely spherical moss spread.
        for (int i = 0; i < 50; i++) {
            double dx;
            double dy;
            double dz;
            do {
                dx = (rand.nextDouble() * 2 - 1) * radius;
                dy = (rand.nextDouble() * 2 - 1) * radius;
                dz = (rand.nextDouble() * 2 - 1) * radius;
            } while (dx * dx + dy * dy + dz * dz > radiusSq);

            double x = pos.getX() + 0.5 + dx;
            double y = pos.getY() + 0.1 + dy;
            double z = pos.getZ() + 0.5 + dz;

            // Vanilla's Cloud particle has its own built-in downward-drifting gravity
            // in its client-side physics that position alone never touches - that's
            // why a spread-only spawn would still drift down regardless. Giving it
            // real initial upward velocity here (yOffset + speed > 0) makes it
            // noticeably float up for a normal puff's lifetime instead, though it
            // won't fully cancel that built-in gravity over a very long-lived particle.
            serverWorld.spawnParticle(EnumParticleTypes.CLOUD, true, x, y, z, 1, 0.0, 0.3, 0.0, 0.02);
        }
    }

    /**
     * Spawns a real EntitySeedBomb at the entity's position with its fuse forced to
     * zero, so it detonates using Pale Bloom's own native logic on its very next tick -
     * the same technique ReapingWillowDeathHandler already uses directly.
     */
    public static void spawnNative(World world, EntityLivingBase entity, double radius) {
        if (world.isRemote) {
            return;
        }

        EntitySeedBomb seedBomb = new EntitySeedBomb(world, entity.posX, entity.posY, entity.posZ, entity);
        seedBomb.setFuse(0);
        world.spawnEntity(seedBomb);

        markDetonation(entity.posX, entity.posY, entity.posZ, radius, DetonationSource.PALE_LUNG);
    }

    /**
     * Marks a Reaping-Willow-triggered detonation for tracking, the same way
     * {@link #spawnNative} does for Pale Lung deaths - call this from
     * ReapingWillowDeathHandler right after it spawns its own EntitySeedBomb. Tagged
     * with a different {@link DetonationSource} so consumers (currently just the
     * Wither conversion in PaleLungImmunityHandler) can react to it independently of
     * Pale-Lung-triggered detonations, under its own separate config toggle.
     */
    public static void markReapingWillowDetonation(World world, double x, double y, double z, double radius) {
        if (world.isRemote) {
            return;
        }
        markDetonation(x, y, z, radius, DetonationSource.REAPING_WILLOW);
    }

    public enum DetonationSource {
        PALE_LUNG, REAPING_WILLOW
    }

    // ------------------------------------------------------------------
    // Detonation tracking - used for both sound suppression and the
    // Wither -> Pale Lung conversion, since both need "did a Seed Bomb just go off
    // near here" info. Tagged with a DetonationSource so the two Wither-conversion
    // toggles (Seed Bomb Options' for Pale Lung deaths, RLCraft Dregora's for Reaping
    // Willow) can be checked independently for the right source.
    // ------------------------------------------------------------------

    // Only meaningful in singleplayer/integrated-server for the sound-suppression use
    // (see PaleLungSeedBombSoundHandler) - it's read by a client-side handler that only
    // shares this JVM's memory with the server on an integrated server, so on a true
    // dedicated server the explosion sound would still play there. The Wither
    // conversion use (PaleLungImmunityHandler) is unaffected by that caveat, since it
    // only ever runs server-side.
    private static final Map<BlockPos, Detonation> RECENT_DETONATIONS = new HashMap<>();
    private static final long DETONATION_WINDOW_MS = 2000L;

    private static void markDetonation(double x, double y, double z, double radius, DetonationSource source) {
        purgeExpiredDetonations();
        RECENT_DETONATIONS.put(new BlockPos(x, y, z), new Detonation(System.currentTimeMillis(), radius, source));
    }

    /**
     * Whether a Pale-Lung-triggered Seed Bomb detonated near this position within the
     * last couple of seconds - used by {@link nlblackeagle.dynamictreespalebloom.event.PaleLungSeedBombSoundHandler}
     * to decide whether to mute an incoming generic-explosion sound, and by
     * {@link nlblackeagle.dynamictreespalebloom.event.PaleLungImmunityHandler} to decide
     * whether to convert an incoming Wither application to Pale Lung instead.
     */
    public static boolean isRecentPaleLungDetonation(double x, double y, double z) {
        return findRecentDetonation(x, y, z, DetonationSource.PALE_LUNG);
    }

    /**
     * Same as {@link #isRecentPaleLungDetonation}, but for Reaping-Willow-triggered
     * detonations specifically - used by PaleLungImmunityHandler to gate the separate
     * "Reaping Willow Converts Wither To Pale Lung" toggle.
     */
    public static boolean isRecentReapingWillowDetonation(double x, double y, double z) {
        return findRecentDetonation(x, y, z, DetonationSource.REAPING_WILLOW);
    }

    private static boolean findRecentDetonation(double x, double y, double z, DetonationSource source) {
        purgeExpiredDetonations();
        for (Map.Entry<BlockPos, Detonation> entry : RECENT_DETONATIONS.entrySet()) {
            Detonation detonation = entry.getValue();
            if (detonation.source != source) {
                continue;
            }

            BlockPos pos = entry.getKey();
            double radius = detonation.radius;
            double dx = pos.getX() + 0.5 - x;
            double dy = pos.getY() + 0.5 - y;
            double dz = pos.getZ() + 0.5 - z;
            if (dx * dx + dy * dy + dz * dz <= radius * radius) {
                return true;
            }
        }
        return false;
    }

    private static void purgeExpiredDetonations() {
        long now = System.currentTimeMillis();
        RECENT_DETONATIONS.entrySet().removeIf(e -> now - e.getValue().timeMs > DETONATION_WINDOW_MS);
    }

    private static class Detonation {
        final long timeMs;
        final double radius;
        final DetonationSource source;

        Detonation(long timeMs, double radius, DetonationSource source) {
            this.timeMs = timeMs;
            this.radius = radius;
            this.source = source;
        }
    }

    // ------------------------------------------------------------------
    // Pale Lung spread
    // ------------------------------------------------------------------

    private static void applyPaleLung(World world, BlockPos pos, Entity exclude, double radius) {
        if (!ForgeConfigHandler.seedBomb.seedBombSpreadsPaleLung) {
            return;
        }
        if (ModPotions.paleLung == null) {
            return;
        }

        AxisAlignedBB area = new AxisAlignedBB(pos).grow(radius);

        List<EntityLivingBase> nearby = world.getEntitiesWithinAABB(EntityLivingBase.class, area);
        for (EntityLivingBase living : nearby) {
            if (living == exclude) {
                continue;
            }
            if (!PaleLungEntityMatcher.matchesAny(ForgeConfigHandler.seedBomb.seedBombAffectsEntities, living)) {
                continue;
            }

            living.addPotionEffect(new PotionEffect(ModPotions.paleLung, 200, 0));
        }
    }

    // ------------------------------------------------------------------
    // Flora scatter
    // ------------------------------------------------------------------

    private static final int DEFAULT_FLORA_WEIGHT = 100;

    private static void scatterFlora(World world, BlockPos pos, String[] pool, double radius) {
        int count = ForgeConfigHandler.seedBomb.seedBombFloraCount;
        if (pool == null || pool.length == 0) {
            return;
        }

        List<WeightedFlora> weightedPool = parsePool(pool);
        if (weightedPool.isEmpty()) {
            return;
        }

        Random rand = world.rand;

        // Always guarantee something from the pool at the exact death/detonation spot
        // - this is what shows up there instead of a hardcoded Pollenhead - as a
        // freebie separate from (not counted against) the `count` random scatter
        // attempts below. Previously this consumed the first of the `count` attempts,
        // which meant a low count (e.g. 2) left barely any budget for genuinely
        // radius-scaled placements, making radius changes easy to miss entirely.
        placeFloraAttempt(world, pos, 0, 0, weightedPool, rand);

        for (int i = 0; i < count; i++) {
            int dx = (int) Math.round((rand.nextDouble() * 2 - 1) * radius);
            int dz = (int) Math.round((rand.nextDouble() * 2 - 1) * radius);
            placeFloraAttempt(world, pos, dx, dz, weightedPool, rand);
        }
    }

    private static void placeFloraAttempt(World world, BlockPos pos, int dx, int dz, List<WeightedFlora> weightedPool, Random rand) {
        ParsedFlora parsed = pickWeighted(weightedPool, rand);
        if (parsed == null) {
            return;
        }

        BlockPos columnPos = pos.add(dx, 0, dz);

        // Double-tall plants (BlockDoublePalePlant, BlockPollenhead) need 2 vertical
        // spaces free (both halves get placed); everything else - including Dynamic
        // Trees saplings, which do their own soil/replaceable validation internally -
        // only needs 1.
        int neededHeight = parsed.isBlock()
                && (parsed.block instanceof BlockDoublePalePlant || parsed.block instanceof BlockPollenhead)
                ? 2 : 1;
        BlockPos placePos = findSurface(world, columnPos, neededHeight);
        if (placePos == null) {
            return;
        }

        placeFlora(world, placePos, parsed);
    }

    /**
     * Parses every entry in the pool once up front (rather than re-parsing on every
     * placement attempt), splitting off an optional ",weight" suffix - e.g.
     * "palebloom:bramble,100". Entries without a weight suffix get
     * {@link #DEFAULT_FLORA_WEIGHT}, so old unweighted pools behave exactly as before
     * (uniform random selection). Entries with weight <= 0 are skipped entirely.
     */
    private static List<WeightedFlora> parsePool(String[] pool) {
        List<WeightedFlora> result = new ArrayList<>();

        for (String entry : pool) {
            String spec = entry;
            int weight = DEFAULT_FLORA_WEIGHT;

            int comma = entry.lastIndexOf(',');
            if (comma >= 0) {
                String weightPart = entry.substring(comma + 1).trim();
                try {
                    weight = Integer.parseInt(weightPart);
                    spec = entry.substring(0, comma).trim();
                } catch (NumberFormatException e) {
                    // Not actually a weight suffix (e.g. a stray comma) - treat the
                    // whole thing as the flora spec and fall back to the default weight.
                    spec = entry;
                    weight = DEFAULT_FLORA_WEIGHT;
                }
            }

            if (weight <= 0) {
                continue;
            }

            ParsedFlora parsed = parseFloraEntry(spec);
            if (parsed == null) {
                continue;
            }

            result.add(new WeightedFlora(parsed, weight));
        }

        return result;
    }

    private static ParsedFlora pickWeighted(List<WeightedFlora> weighted, Random rand) {
        int totalWeight = 0;
        for (WeightedFlora w : weighted) {
            totalWeight += w.weight;
        }
        if (totalWeight <= 0) {
            return null;
        }

        int roll = rand.nextInt(totalWeight);
        int cumulative = 0;
        for (WeightedFlora w : weighted) {
            cumulative += w.weight;
            if (roll < cumulative) {
                return w.flora;
            }
        }
        return weighted.get(weighted.size() - 1).flora;
    }

    private static class WeightedFlora {
        final ParsedFlora flora;
        final int weight;

        WeightedFlora(ParsedFlora flora, int weight) {
            this.flora = flora;
            this.weight = weight;
        }
    }

    private static void placeFlora(World world, BlockPos placePos, ParsedFlora parsed) {
        if (parsed.species != null) {
            // Species.plantSapling(World, BlockPos) - confirmed from the real Dynamic
            // Trees 1.12.2 source - checks isReplaceable + BlockDynamicSapling.canSaplingStay
            // itself and places a real dynamic sapling tied to this species. This is the
            // correct, addon-facing API for planting a species programmatically, not a
            // raw block placement (Dynamic Trees saplings aren't a normal single
            // Block+meta the way vanilla saplings are).
            parsed.species.plantSapling(world, placePos);
            return;
        }

        Block block = parsed.block;

        if (block instanceof BlockDoublePalePlant) {
            // placeDoubleAt(World, BlockPos, int typeMeta, boolean awake, int flags) -
            // confirmed directly from the shipped jar. typeMeta lines up 1:1 with the
            // configured meta: 0 = Eyeblossom Bush, 1 = Stiffpod, 2 = Epiales. This
            // places both halves correctly, the same way BlockPollenhead.placeDoubleAt
            // does elsewhere in this addon - a plain setBlockState here would only
            // place a broken single-height half of the plant.
            ((BlockDoublePalePlant) block).placeDoubleAt(world, placePos, parsed.meta, false, 2);
            return;
        }

        if (block instanceof BlockPollenhead) {
            // placeDoubleAt(World, BlockPos, IGardenState.EnumLucidityState, int flags) -
            // confirmed directly from the shipped jar. The configured meta (0/1/2, same
            // convention as pale_plant_double) selects DORMANT/LUCID/AWAKE; defaults to
            // DORMANT (matching this addon's own prior Pollenhead placement) if the
            // meta is out of range or omitted.
            IGardenState.EnumLucidityState[] states = IGardenState.EnumLucidityState.values();
            IGardenState.EnumLucidityState state = (parsed.meta >= 0 && parsed.meta < states.length)
                    ? states[parsed.meta]
                    : IGardenState.EnumLucidityState.DORMANT;
            ((BlockPollenhead) block).placeDoubleAt(world, placePos, state, 2);
            return;
        }

        IBlockState state;
        try {
            state = block.getStateFromMeta(parsed.meta);
        } catch (Exception e) {
            state = block.getDefaultState();
        }

        if (!state.getBlock().canPlaceBlockAt(world, placePos)) {
            return;
        }

        world.setBlockState(placePos, state, 3);

        if (block instanceof BlockIncenseThorn) {
            // A raw placement never runs the block's own onBlockPlacedBy hook, so the
            // tile's Potion field would otherwise come back null - and this addon's own
            // BlockIncenseThornMixin harvest-drop logic calls tile.getPotion() directly,
            // which would NPE the moment someone chops one of these down. Set it
            // explicitly, using the same Pale Lung swap toggle as everywhere else.
            TileEntity te = world.getTileEntity(placePos);
            if (te instanceof TileIncenseThorn) {
                Potion potion = (ForgeConfigHandler.paleLung.replaceIncenseThornsPoison && ModPotions.paleLung != null)
                        ? ModPotions.paleLung
                        : MobEffects.POISON;
                ((TileIncenseThorn) te).setPotion(potion);
            }
        }
    }

    private static BlockPos findSurface(World world, BlockPos columnPos, int height) {
        // Scan a small vertical window around the seed bomb's own height for `height`
        // stacked air blocks sitting on solid ground.
        for (int dy = 3; dy >= -3; dy--) {
            BlockPos candidate = columnPos.add(0, dy, 0);
            BlockPos below = candidate.down();

            boolean spaceClear = true;
            for (int h = 0; h < height; h++) {
                if (!world.isAirBlock(candidate.up(h))) {
                    spaceClear = false;
                    break;
                }
            }

            if (spaceClear
                    && !world.isAirBlock(below)
                    && world.getBlockState(below).isSideSolid(world, below, EnumFacing.UP)) {
                return candidate;
            }
        }
        return null;
    }

    private static ParsedFlora parseFloraEntry(String entry) {
        String[] parts = entry.split(":");
        if (parts.length < 2) {
            LOGGER.warning("[" + DynamicTreesPaleBloom.MODID + "] Invalid Seed Bomb flora entry (expected \"modid:block\", \"modid:block:meta\", or \"modid:species\"): " + entry);
            return null;
        }

        ResourceLocation loc = new ResourceLocation(parts[0], parts[1]);

        Block block = Block.REGISTRY.getObject(loc);
        if (block != null && block != net.minecraft.init.Blocks.AIR) {
            int meta = 0;
            if (parts.length >= 3) {
                try {
                    meta = Integer.parseInt(parts[2]);
                } catch (NumberFormatException ignored) {
                    meta = 0;
                }
            }
            return ParsedFlora.ofBlock(block, meta);
        }

        // Not a registered Block - Dynamic Trees saplings aren't blocks in the
        // registry sense, they're Species (e.g. "dynamictreespalebloom:pale_oak"),
        // planted via Species#plantSapling rather than a raw block placement.
        Species species = TreeRegistry.findSpecies(loc);
        if (species != null) {
            return ParsedFlora.ofSpecies(species);
        }

        LOGGER.warning("[" + DynamicTreesPaleBloom.MODID + "] Unknown Seed Bomb flora entry (no matching Block or Dynamic Trees Species): " + loc);
        return null;
    }

    private static class ParsedFlora {
        final Block block;
        final int meta;
        final Species species;

        private ParsedFlora(Block block, int meta, Species species) {
            this.block = block;
            this.meta = meta;
            this.species = species;
        }

        static ParsedFlora ofBlock(Block block, int meta) {
            return new ParsedFlora(block, meta, null);
        }

        static ParsedFlora ofSpecies(Species species) {
            return new ParsedFlora(null, 0, species);
        }

        boolean isBlock() {
            return block != null;
        }
    }
}
