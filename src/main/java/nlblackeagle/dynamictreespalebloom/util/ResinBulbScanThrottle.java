package nlblackeagle.dynamictreespalebloom.util;

import net.minecraft.world.World;

/**
 * Caps how many TileResinBulb "search the area for an active Creaking Heart" scans can
 * run on a single game tick, across every loaded Resin Bulb combined (not per-world -
 * see the getMinecraftServer() fallback below).
 * <p>
 * Without this, every loaded bulb fires its scan on the exact same tick, since they
 * all gate on the identical world-time modulo with no per-bulb offset - they're never
 * naturally staggered. With enough bulbs loaded at once that's a real stutter risk
 * even though any single bulb's own cost is modest. Bulbs that get throttled simply
 * skip that cycle entirely and try again on their next one; they don't queue or retry
 * sooner.
 */
public class ResinBulbScanThrottle {

    private static final int MAX_SCANS_PER_TICK = 10;

    private static long lastTick = Long.MIN_VALUE;
    private static int scansThisTick = 0;

    public static boolean tryAcquire(World world) {
        // Prefer the server's own global tick counter so the cap applies across every
        // dimension together, not just within whichever world a given bulb happens to
        // be in - getTotalWorldTime() is per-world and could let bulbs in different
        // dimensions each get their own independent budget otherwise. Falls back to
        // the world's own time if there's no server reference available (e.g. this
        // somehow runs client-side - see the isRemote note on this whole feature).
        long currentTick = world.getMinecraftServer() != null
                ? world.getMinecraftServer().getTickCounter()
                : world.getTotalWorldTime();

        if (currentTick != lastTick) {
            lastTick = currentTick;
            scansThisTick = 0;
        }

        if (scansThisTick >= MAX_SCANS_PER_TICK) {
            return false;
        }

        scansThisTick++;
        return true;
    }
}
