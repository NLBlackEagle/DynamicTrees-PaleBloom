package nlblackeagle.dynamictreespalebloom.config;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import nlblackeagle.dynamictreespalebloom.DynamicTreesPaleBloom;

@Config(modid = DynamicTreesPaleBloom.MODID)
public class ForgeConfigHandler {

    @Config.Comment("Master enable/disable switches for entire feature sections. Off by default - everything in this addon beyond the base Dynamic Trees compat is opt-in.")
    @Config.Name("Feature Toggles")
    public static final FeatureTogglesConfig featureToggles = new FeatureTogglesConfig();

    public static class FeatureTogglesConfig {

        @Config.Comment("Master switch for the Pale Lung Potion Options section. When false, Pale Lung is never applied by anything in this addon (Incense Thorns, Seed Bomb Wither conversion, Seed Bomb area spread, etc.), regardless of those features' own individual settings.")
        @Config.Name("Enable Pale Lung Options")
        public boolean enablePaleLung = false;

        @Config.Comment("Master switch for the Seed Bomb Options section. When false, none of this addon's own Seed Bomb additions (on-death trigger, flora scatter, extra Pale Lung spread, death particles, vertical moss, Wither conversion, radius rescaling) run at all, regardless of those features' own individual settings. Does NOT affect Reaping Willow's own native explosion (Pollenhead + real Seed Bomb spawn), which is governed by Reaping Willow Explodes On Death under RLCraft Dregora Options instead.")
        @Config.Name("Enable Seed Bomb Options")
        public boolean enableSeedBomb = false;

        @Config.Comment("Master switch for the RLCraft Dregora Options section. When false, none of that section's features run at all, regardless of those features' own individual settings.")
        @Config.Name("Enable RLCraft Dregora Options")
        public boolean enableRLCraftDregora = false;
    }

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

        @Config.Comment("Chance (per direction, up to 4) for a Sucker Root Nodule to generate near a Blooming Pale Oak. 0.1 is Pale Bloom's default.")
        @Config.Name("Sucker Nodule Chance")
        @Config.RangeDouble(min = 0.0, max = 1.0)
        public double suckerNoduleChance = 0.10;
    }

    @Config.Comment("Biome Decoration Options")
    @Config.Name("Biome Decoration Options")
    public static final BiomeDecorationConfig biomeDecoration = new BiomeDecorationConfig();

    public static class BiomeDecorationConfig {

        @Config.Comment("Chance per chunk for a scattered Pale Petals patch to generate in the Pale Garden. 0.0 = never, 1.0 = every chunk.")
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

        @Config.Comment("Wearing a helmet enchanted with Respiration grants immunity to Pale Lung.")
        @Config.Name("Respiration Grants Immunity")
        public boolean respirationGrantsImmunity = true;

        @Config.Comment("Swap Incense Thorns Poison effect over to Pale Lung.")
        @Config.Name("Replace Incense Thorns Poison")
        public boolean replaceIncenseThornsPoison = true;

        @Config.Comment("Per-tick chance for an entity affected by Pale Lung to emit a white ambient particle. 0 = never, 1 = every tick.")
        @Config.Name("Ambient Particle Chance")
        @Config.RangeDouble(min = 0.0, max = 1.0)
        public double ambientParticleChance = 0.05;

        @Config.Comment("Entity registry names (e.g. \"minecraft:zombie\", \"minecraft:player\") used with \"Pale Lung Entity List Mode\" to control which entities can contract the pale lung effect. Also supports wildcards: @all, @player, @hostile, @passive, @ambient, and @<modid> (e.g. \"@palebloom\" matches every entity registered by the palebloom mod).")
        @Config.Name("Pale Lung Entity List")
        public String[] entityList = {"@palebloom"};

        @Config.Comment("Whether \"Pale Lung Entity List\" is treated as a whitelist (ONLY listed entities can be affected by Pale Lung) or a blacklist (listed entities are excluded, everyone else can be affected).")
        @Config.Name("Pale Lung Entity List Mode")
        public EntityListMode entityListMode = EntityListMode.BLACKLIST;

        @Config.Comment("Same registry-name/@wildcard syntax as \"Pale Lung Entity List\", but controls which Pale-Lung-affected entities actually take tick damage from it, vs which just carry the effect harmlessly (still shows white hearts, ambient particles, etc., just no damage).")
        @Config.Name("Pale Lung Entity Damage List")
        public String[] entityDamageList = {"@passive", "@player", "@srparasites"};

        @Config.Comment("Whether \"Pale Lung Entity Damage List\" is a whitelist (ONLY listed entities take damage) or a blacklist (listed entities are excluded from damage, everyone else takes it).")
        @Config.Name("Pale Lung Entity Damage List Mode")
        public EntityListMode entityDamageListMode = EntityListMode.WHITELIST;

        public enum EntityListMode {
            WHITELIST, BLACKLIST
        }
    }

    @Config.Comment("Seed Bomb / Explosion Options - covers what a detonating Seed Bomb actually does, whether triggered by a Pale Lung death, a Reaping Willow death, or a plain thrown \"palebloom:seed_bomb\" item.")
    @Config.Name("Seed Bomb Options")
    public static final SeedBombConfig seedBomb = new SeedBombConfig();

    public static class SeedBombConfig {

        @Config.Comment("When an entity dies while affected by Pale Lung, trigger Seed Bomb effects (flora scatter, Pale Lung spread, particles, moss, etc.) at the death location.")
        @Config.Name("Pale Lung Death Triggers Seed Bomb Effects")
        public boolean seedBombOnDeath = true;

        @Config.Comment("Whenever a Seed Bomb detonates (from a Pale Lung death, a Reaping Willow death, or otherwise), also apply Pale Lung to every living entity caught in its radius, alongside whatever else the Seed Bomb already does.")
        @Config.Name("Seed Bomb Spreads Pale Lung")
        public boolean seedBombSpreadsPaleLung = true;

        @Config.Comment("Radius (in blocks) for the affected area in which moss and flora growth occurs when a pale lung affected entity dies.")
        @Config.Name("Seed Bomb Radius")
        @Config.RangeDouble(min = 1.0, max = 16.0)
        public double seedBombRadius = 4.0;

        @Config.Comment("Same as \"Seed Bomb Radius\", but specifically for Reaping Willow deaths.")
        @Config.Name("Reaping Willow Radius")
        @Config.RangeDouble(min = 1.0, max = 16.0)
        public double reapingWillowRadius = 6.0;

        @Config.Comment("Same registry-name/@wildcard syntax as \"Pale Lung Entity List\" - controls which entities the Seed Bomb's Pale Lung spread can affect. Default (\"@all\") affects everyone, including players.")
        @Config.Name("Seed Bomb Affects Entities")
        public String[] seedBombAffectsEntities = {"@all"};

        @Config.Comment("Blocks/species spawned when a entity affected with Pale Lung dies.")
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

        @Config.Comment("Flora spawned when a Reaping Willow dies.")
        @Config.Name("Reaping Willow Flora Pool")
        public String[] reapingWillowFloraPool = {
                "palebloom:pollenhead,100"
        };

        @Config.Comment("How many flora placement attempts for both Reaping Willow & Pale Lung entity deaths.")
        @Config.Name("Seed Bomb Flora Count")
        @Config.RangeInt(min = 0, max = 32)
        public int seedBombFloraCount = 2;

        @Config.Comment("Mutes the explosion sound from the pale lung, reaping willow and seed bombs")
        @Config.Name("Silence Seed Bomb Explosion Sound")
        public boolean seedBombSilent = true;

        @Config.Comment("Spawns upward-floating Cloud particles when a moss-growth event occurs.")
        @Config.Name("Seed Bomb Death Particles")
        public boolean seedBombDeathParticles = true;

        @Config.Comment("Makes the moss-spread spherical instead of a disc")
        @Config.Name("Seed Bomb Vertical Moss Spread")
        public boolean seedBombVerticalMoss = true;

        @Config.Comment("Setting this option to true replaces the wither effect with the pale lung effect for both Pale Lung Seed Bombs and Reaping Willow's own on-death explosion (needs \"Enable Seed Bomb\" and/or \"Enable RLCraft Dregora Options\" enabled respectively for either source to actually be tracked).")
        @Config.Name("Seed Bomb Converts Wither To Pale Lung")
        public boolean seedBombConvertsWither = true;
    }

    @Config.Comment("Miscellaneous options - not tied to any specific feature section.")
    @Config.Name("Miscellaneous")
    public static final MiscellaneousConfig miscellaneous = new MiscellaneousConfig();

    public static class MiscellaneousConfig {

        @Config.Comment("Fixed a bug where harvesting Incense Thorns drops the item twice.")
        @Config.Name("Fix Incense Thorns Double Drop")
        public boolean fixIncenseThornsDoubleDrop = true;
    }

    @Config.Comment("Options specifically for replicating the RLCraft Dregora modpack experience.")
    @Config.Name("RLCraft Dregora Options")
    public static final RLCraftDregoraConfig rlcraftDregora = new RLCraftDregoraConfig();

    public static class RLCraftDregoraConfig {

        @Config.Comment("When set to true, disables all items in-game listed in the crafting blacklist.")
        @Config.Name("Enable the crafting blacklist")
        public boolean enableItemBlacklist = true;

        @Config.Comment("Removed the crafting recipe for listed items.")
        @Config.Name("Crafting Blacklist")
        public String[] itemBlacklist = {
                "palebloom:mannequin",
                "palebloom:creaking_heart",
                "palebloom:reaping_willow_sapling",
                "palebloom:seed_bomb",
                "palebloom:pale_moss_cloak"
        };

        @Config.Comment("Allows Pale and the Dark Oak Dynamic Trees to grow underground.")
        @Config.Name("Enable Underground Growth")
        public boolean enableUndergroundGrowth = true;

        @Config.Comment("Makes Reaping Willow always aggressive toward players on sight.")
        @Config.Name("Reaping Willow Always Aggressive")
        public boolean reapingWillowAggressive = true;

        @Config.Comment("Allow Reaping Willow to spawn naturally, in the Pale Garden biome only.")
        @Config.Name("Reaping Willow Natural Spawning")
        public boolean reapingWillowNaturalSpawn = true;

        @Config.Comment("Spawn weight for Reaping Willow, relative to other hostile mobs in the Pale Garden.")
        @Config.Name("Reaping Willow Spawn Weight")
        public int reapingWillowSpawnWeight = 150;

        @Config.Comment("Minimum group size when Reaping Willow spawns.")
        @Config.Name("Reaping Willow Min Group Size")
        public int reapingWillowMinGroupSize = 1;

        @Config.Comment("Maximum group size when Reaping Willow spawns.")
        @Config.Name("Reaping Willow Max Group Size")
        public int reapingWillowMaxGroupSize = 1;

        @Config.Comment("Allow Reaping Willow to spawn under direct open sky")
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

        @Config.Comment("On death, Reaping Willow explodes and spreads Pale Moss, applies Wither (or Pale Lung instead, if \"Seed Bomb Converts Wither To Pale Lung\" under Seed Bomb Options is enabled) to nearby non-pale creatures, converts nearby Creepers to Pale Creepers - no block destruction) and places a Pollenhead at the death location.")
        @Config.Name("Reaping Willow Explodes On Death")
        public boolean reapingWillowExplodeOnDeath = true;
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
