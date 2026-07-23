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
