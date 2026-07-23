package nlblackeagle.dynamictreespalebloom.event;

import com.sirsquidly.palebloom.common.entity.EntityReapingWillow;
import com.sirsquidly.palebloom.common.world.WorldPaleGarden;
import com.sirsquidly.palebloom.init.JTPGBiomes;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import nlblackeagle.dynamictreespalebloom.config.ForgeConfigHandler;

// Reaping Willow natural spawning, Pale Garden biome only (per design
// decision - not global underground spawning). Uses vanilla's own
// per-biome spawn list system (EntityRegistry.addSpawn) for the base
// weight/group-size/biome mechanics, then layers our own additional
// conditions (open-sky, night-only, max height, light level) on top via
// LivingSpawnEvent.CheckSpawn - a normal, safe Forge event, not a Mixin.
// EntityReapingWillow doesn't override getCanSpawnHere() itself (only
// inherits EntityMob's default), so a Mixin targeting it there would hit
// the same "method not declared on this class" trap that broke an earlier
// mixin on a different class - CheckSpawn avoids that risk entirely.
public class ReapingWillowSpawnHandler {

    public static void registerSpawns() {
        if (!ForgeConfigHandler.rlcraftDregora.reapingWillowNaturalSpawn) return;

        EntityRegistry.addSpawn(
                EntityReapingWillow.class,
                ForgeConfigHandler.rlcraftDregora.reapingWillowSpawnWeight,
                ForgeConfigHandler.rlcraftDregora.reapingWillowMinGroupSize,
                ForgeConfigHandler.rlcraftDregora.reapingWillowMaxGroupSize,
                EnumCreatureType.MONSTER,
                JTPGBiomes.PALE_GARDEN
        );
    }

    @SubscribeEvent
    public void onCheckSpawn(LivingSpawnEvent.CheckSpawn event) {
        if (!(event.getEntity() instanceof EntityReapingWillow)) return;

        World world = event.getWorld();
        BlockPos pos = new BlockPos(event.getX(), event.getY(), event.getZ());

        if (pos.getY() > ForgeConfigHandler.rlcraftDregora.reapingWillowMaxHeight) {
            event.setResult(Event.Result.DENY);
            return;
        }

        if (!ForgeConfigHandler.rlcraftDregora.reapingWillowSpawnUnderOpenSky && world.canBlockSeeSky(pos)) {
            event.setResult(Event.Result.DENY);
            return;
        }

        if (ForgeConfigHandler.rlcraftDregora.reapingWillowNightOnly && !WorldPaleGarden.isNight(world)) {
            event.setResult(Event.Result.DENY);
            return;
        }

        if (world.getLightFromNeighbors(pos) > ForgeConfigHandler.rlcraftDregora.reapingWillowMaxLightLevel) {
            event.setResult(Event.Result.DENY);
        }

        // Otherwise leave the result untouched (DEFAULT) - lets vanilla's
        // own remaining spawn validation (collision, getCanSpawnHere, etc.)
        // continue to apply normally rather than force-allowing.
    }
}
