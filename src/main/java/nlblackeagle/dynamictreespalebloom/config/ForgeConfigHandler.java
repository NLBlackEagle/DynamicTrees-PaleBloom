package nlblackeagle.dynamictreespalebloom.config;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import nlblackeagle.dynamictreespalebloom.DynamicTreesPaleBloom;

@Config(modid = DynamicTreesPaleBloom.MODID)
public class ForgeConfigHandler {

    @Config.Comment("Pale Oak Options")
    @Config.Name("Pale Oak Options")
    public static final PaleOakConfig paleOak = new PaleOakConfig();

    public static class PaleOakConfig {

        @Config.Comment("Chance (per growth pulse) for a hand-grown/bonemealed Pale Oak to generate a Creaking Heart")
        @Config.Name("Creaking Heart Chance (Grown)")
        @Config.RangeDouble(min = 0.0, max = 1.0)
        public double creakingHeartGrowthChance = 0;

        @Config.Comment("Chance (per tree) for a naturally worldgen-spawned Pale Oak to generate a Creaking Heart")
        @Config.Name("Creaking Heart Chance (Worldgen)")
        @Config.RangeDouble(min = 0.0, max = 1.0)
        public double creakingHeartWorldgenChance = 0.10;

        @Config.Comment("Minimum trunk radius required before a Creaking Heart can generate")
        @Config.Name("Creaking Heart Minimum Trunk Radius")
        @Config.RangeInt(min = 1, max = 24)
        public int creakingHeartMinTrunkRadius = 4;

        @Config.Comment("Per-leaf chance (worldgen only) for hanging moss to grow beneath a leaf block")
        @Config.Name("Hanging Moss Chance")
        @Config.RangeDouble(min = 0.0, max = 1.0)
        public double hangingMossChance = 0.075;
    }

    @Config.Comment("Blooming Pale Oak Options")
    @Config.Name("Blooming Pale Oak Options")
    public static final BloomingPaleOakConfig bloomingPaleOak = new BloomingPaleOakConfig();

    public static class BloomingPaleOakConfig {

        @Config.Comment("Chance (per growth pulse) for a hand-grown/bonemealed Blooming Pale Oak to generate a Creaking Heart")
        @Config.Name("Creaking Heart Chance (Grown)")
        @Config.RangeDouble(min = 0.0, max = 1.0)
        public double creakingHeartGrowthChance = 0.10;

        @Config.Comment("Chance (per tree) for a naturally worldgen-spawned Blooming Pale Oak to generate a Creaking Heart")
        @Config.Name("Creaking Heart Chance (Worldgen)")
        @Config.RangeDouble(min = 0.0, max = 1.0)
        public double creakingHeartWorldgenChance = 0.10;

        @Config.Comment("Minimum trunk radius required before a Creaking Heart can generate")
        @Config.Name("Creaking Heart Minimum Trunk Radius")
        @Config.RangeInt(min = 1, max = 24)
        public int creakingHeartMinTrunkRadius = 5;

        @Config.Comment("Chance (per direction, up to 4 checked) for a Sucker Root Nodule to generate near a Blooming Pale Oak. Matches Pale Bloom's own real rate by default.")
        @Config.Name("Sucker Nodule Chance")
        @Config.RangeDouble(min = 0.0, max = 1.0)
        public double suckerNoduleChance = 0.10;
    }

    @Config.Comment("Biome Decoration Options")
    @Config.Name("Biome Decoration Options")
    public static final BiomeDecorationConfig biomeDecoration = new BiomeDecorationConfig();

    public static class BiomeDecorationConfig {

        @Config.Comment("Chance per chunk for a scattered Pale Petals patch to generate in the Pale Garden. 0.0 = never, 1.0 = every chunk. Roughly 10% of the current Hanging Moss chance by default.")
        @Config.Name("Pale Petals Chance")
        @Config.RangeDouble(min = 0.0, max = 1.0)
        public double palePetalsChance = 0.075;

        @Config.Comment("Multiplier on Pale Pumpkin generation rate, matching vanilla pumpkin patch generation. 0.0 = disabled, 1.0 = same rate as vanilla pumpkins.")
        @Config.Name("Pale Pumpkin Generation Multiplier")
        @Config.RangeDouble(min = 0.0, max = 1.0)
        public double palePumpkinChance = 1.0;
    }

    @Config.Comment("Pale Lung Potion Options")
    @Config.Name("Pale Lung Potion Options")
    public static final PaleLungConfig paleLung = new PaleLungConfig();

    public static class PaleLungConfig {

        @Config.Comment("Damage-tick speed of Pale Lung relative to vanilla Poison. 1.0 = same speed as Poison, 0.25 = ticks 4x less often (default).")
        @Config.Name("Tick Speed Multiplier")
        @Config.RangeDouble(min = 0.05, max = 4.0)
        public double tickSpeedMultiplier = 0.25;

        @Config.Comment("Wearing a helmet enchanted with Respiration grants immunity to Pale Lung - blocks new applications outright, and clears it if the helmet gets equipped mid-effect.")
        @Config.Name("Respiration Grants Immunity")
        public boolean respirationGrantsImmunity = true;

        @Config.Comment("Swap Incense Thorns' natural-growth Poison effect over to Pale Lung.")
        @Config.Name("Replace Incense Thorns Poison")
        public boolean replaceIncenseThornsPoison = true;

        @Config.Comment("Per-tick chance for an entity affected by Pale Lung to emit a white ambient particle. 0 = never, 1 = every tick.")
        @Config.Name("Ambient Particle Chance")
        @Config.RangeDouble(min = 0.0, max = 1.0)
        public double ambientParticleChance = 0.05;

        @Config.Comment("Whether \"Pale Lung Entity List\" is treated as a whitelist (ONLY listed entities can be affected by Pale Lung) or a blacklist (listed entities are excluded, everyone else can be affected).")
        @Config.Name("Pale Lung Entity List Mode")
        public EntityListMode entityListMode = EntityListMode.BLACKLIST;

        @Config.Comment("Entity registry names (e.g. \"minecraft:zombie\", \"minecraft:player\") used with \"Pale Lung Entity List Mode\" to control which entities Pale Lung can affect at all. Blocks new applications outright, the same mechanism as Respiration immunity. Empty list + blacklist mode = everyone can be affected (default).")
        @Config.Name("Pale Lung Entity List")
        public String[] entityList = new String[0];

        public enum EntityListMode {
            WHITELIST, BLACKLIST
        }
    }

    @Config.Comment("Seed Bomb / Explosion Options - covers what a detonating Seed Bomb actually does, whether triggered by a Pale Lung death, a Reaping Willow death, or a plain thrown \"palebloom:seed_bomb\" item.")
    @Config.Name("Seed Bomb Options")
    public static final SeedBombConfig seedBomb = new SeedBombConfig();

    public static class SeedBombConfig {

        @Config.Comment("When an entity dies while affected by Pale Lung, trigger Seed Bomb effects (flora scatter, Pale Lung spread, particles, moss, etc.) at the death location - mirroring Reaping Willow's own on-death Seed Bomb behaviour.")
        @Config.Name("Pale Lung Death Triggers Seed Bomb Effects")
        public boolean seedBombOnDeath = true;

        @Config.Comment("Whenever a Seed Bomb detonates (from a Pale Lung death, a Reaping Willow death, or otherwise), also apply Pale Lung to every living entity caught in its radius, alongside whatever else the Seed Bomb already does.")
        @Config.Name("Seed Bomb Spreads Pale Lung")
        public boolean seedBombSpreadsPaleLung = true;

        @Config.Comment("Radius (in blocks) for a detonating Seed Bomb's area of effect - governs how far it scatters flora, spreads Pale Lung, spawns death particles, spreads moss, and its native Wither/Creeper-conversion range - for everything EXCEPT Reaping Willow deaths (see \"Reaping Willow Radius\" for that one).")
        @Config.Name("Seed Bomb Radius")
        @Config.RangeDouble(min = 1.0, max = 16.0)
        public double seedBombRadius = 4.0;

        @Config.Comment("Same as \"Seed Bomb Radius\", but specifically for Reaping Willow deaths.")
        @Config.Name("Reaping Willow Radius")
        @Config.RangeDouble(min = 1.0, max = 16.0)
        public double reapingWillowRadius = 5.0;

        @Config.Comment("Whether the Seed Bomb's Pale Lung spread also affects players caught in the radius.")
        @Config.Name("Seed Bomb Affects Players")
        public boolean seedBombAffectsPlayers = false;

        @Config.Comment("Blocks (as \"modid:block\" or \"modid:block:meta\") or Dynamic Trees species (as \"modid:species\") a detonating Seed Bomb may randomly scatter within its radius, for everything EXCEPT Reaping Willow deaths (see \"Reaping Willow Flora Pool\" for that one). Optionally append \",weight\" to any entry (e.g. \"palebloom:bramble,100\") to bias how often it's picked relative to the others - higher = more likely. Entries with no weight suffix default to 100; a weight of 0 or less excludes that entry entirely.")
        @Config.Name("Seed Bomb Flora Pool")
        public String[] seedBombFloraPool = {
                "dynamictreespalebloom:pale_oak,25",
                "dynamictreespalebloom:pale_blooming,15",
                "dynamictreespalebloom:pale_birch,20",
                "palebloom:pale_plant_double,35",
                "palebloom:pale_plant_double:1,35",
                "palebloom:pale_plant_double:2,35",
                "palebloom:bramble,35",
                "palebloom:incense_thorns,25",
                "palebloom:eyeblossom_closed,70",
                "palebloom:pale_petals,70",
                "minecraft:tallgrass:1,100"
        };

        @Config.Comment("Blocks/species a detonating Seed Bomb may randomly scatter within its radius specifically for Reaping Willow deaths, separate from the general \"Seed Bomb Flora Pool\". Same optional \",weight\" suffix syntax applies here too.")
        @Config.Name("Reaping Willow Flora Pool")
        public String[] reapingWillowFloraPool = {
                "palebloom:pollenhead,100"
        };

        @Config.Comment("How many flora placement attempts a detonating Seed Bomb makes within its radius.")
        @Config.Name("Seed Bomb Flora Count")
        @Config.RangeInt(min = 0, max = 32)
        public int seedBombFloraCount = 2;

        @Config.Comment("Mute the vanilla explosion sound a Pale Lung death's Seed Bomb makes on detonation. Only reliable in singleplayer/integrated-server (see PaleLungSeedBombSoundHandler).")
        @Config.Name("Silence Seed Bomb Explosion Sound")
        public boolean seedBombSilent = true;

        @Config.Comment("Spawn a burst of upward-floating Cloud particles above the spot where a Seed Bomb detonates (Reaping Willow death or otherwise).")
        @Config.Name("Seed Bomb Death Particles")
        public boolean seedBombDeathParticles = true;

        @Config.Comment("Adds a genuinely 3D/spherical Pale Moss spread on top of Pale Bloom's own native one (which is purely 2D and only follows terrain on a single flat layer). Uses the same Seed Bomb/Reaping Willow radius as everything else here.")
        @Config.Name("Seed Bomb Vertical Moss Spread")
        public boolean seedBombVerticalMoss = true;

        @Config.Comment("When a Pale Lung death's Seed Bomb detonates, intercept and replace any Wither it would natively apply to nearby entities with Pale Lung instead (subject to the Pale Lung Entity List). Does NOT affect Reaping Willow's own Wither application, which stays as-is.")
        @Config.Name("Seed Bomb Converts Wither To Pale Lung")
        public boolean seedBombConvertsWither = true;
    }

    @Config.Comment("Options specifically for replicating the RLCraft Dregora modpack experience. Off by default - other packs using this addon should have to opt in.")
    @Config.Name("RLCraft Dregora Options")
    public static final RLCraftDregoraConfig rlcraftDregora = new RLCraftDregoraConfig();

    public static class RLCraftDregoraConfig {

        @Config.Comment("When set to true, disables all items in-game listed in the item blacklist.")
        @Config.Name("Enable the item blacklist")
        public boolean enableItemBlacklist = false;

        @Config.Comment("Real Pale Bloom items/blocks to remove entirely, using their real registry names (modid:name). Add more here as needed.")
        @Config.Name("Item Blacklist")
        public String[] itemBlacklist = {
                "palebloom:mannequin",
                "palebloom:creaking_heart",
                "palebloom:reaping_willow_sapling",
                "palebloom:seed_bomb",
                "palebloom:pale_moss_cloak"
        };

        @Config.Comment("Fixes a real Pale Bloom bug: harvesting Incense Thorns drops the item twice (once from its own manual NBT-preserving drop, once from the default drop path underneath it). True = only drop once.")
        @Config.Name("Fix Incense Thorns Double Drop")
        public boolean fixIncenseThornsDoubleDrop = true;

        @Config.Comment("Allows Dynamic Trees to plant and grow fully underground, bypassing DT's own skylight requirements (both the initial seed-planting check and the ongoing leaf-survival check). Affects this addon's own Pale Bloom saplings/trees and vanilla Dark Oak specifically, not every Dynamic Trees species.")
        @Config.Name("Enable Underground Growth")
        public boolean enableUndergroundGrowth = false;

        @Config.Comment("Makes Reaping Willow always aggressive toward players on sight. It currently has no ability to spontaneously target a player at all (only retaliates if attacked first) - this adds that.")
        @Config.Name("Reaping Willow Always Aggressive")
        public boolean reapingWillowAggressive = false;

        @Config.Comment("Allow Reaping Willow to spawn naturally, in the Pale Garden biome only.")
        @Config.Name("Reaping Willow Natural Spawning")
        public boolean reapingWillowNaturalSpawn = false;

        @Config.Comment("Spawn weight for Reaping Willow, relative to other hostile mobs in the Pale Garden.")
        @Config.Name("Reaping Willow Spawn Weight")
        public int reapingWillowSpawnWeight = 150;

        @Config.Comment("Minimum group size when Reaping Willow spawns.")
        @Config.Name("Reaping Willow Min Group Size")
        public int reapingWillowMinGroupSize = 1;

        @Config.Comment("Maximum group size when Reaping Willow spawns.")
        @Config.Name("Reaping Willow Max Group Size")
        public int reapingWillowMaxGroupSize = 1;

        @Config.Comment("Allow Reaping Willow to spawn under direct open sky, not just fully underground/covered areas.")
        @Config.Name("Reaping Willow Spawn Under Open Sky")
        public boolean reapingWillowSpawnUnderOpenSky = false;

        @Config.Comment("Only allow Reaping Willow to spawn at night.")
        @Config.Name("Reaping Willow Night Only")
        public boolean reapingWillowNightOnly = false;

        @Config.Comment("Maximum Y level Reaping Willow can spawn at.")
        @Config.Name("Reaping Willow Max Height")
        public int reapingWillowMaxHeight = 50;

        @Config.Comment("Maximum light level for Reaping Willow to spawn")
        @Config.Name("Reaping Willow Max Light Level")
        public int reapingWillowMaxLightLevel = 15;

        @Config.Comment("On death, Reaping Willow explodes like a real Seed Bomb (spreads Pale Moss, applies Wither to nearby non-pale creatures, converts nearby Creepers to Pale Creepers - no block destruction) and places a Pollenhead at the death location.")
        @Config.Name("Reaping Willow Explodes On Death")
        public boolean reapingWillowExplodeOnDeath = false;
    }

    @Mod.EventBusSubscriber(modid = DynamicTreesPaleBloom.MODID)
    public static class EventHandler {

        @SubscribeEvent
        public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
            if (event.getModID().equals(DynamicTreesPaleBloom.MODID)) {
                ConfigManager.sync(DynamicTreesPaleBloom.MODID, Config.Type.INSTANCE);
            }
        }
    }
}
