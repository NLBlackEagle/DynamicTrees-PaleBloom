package nlblackeagle.dynamictreespalebloom.event;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import nlblackeagle.dynamictreespalebloom.potion.PaleLungSeedBomb;

/**
 * Advances {@link PaleLungSeedBomb}'s scheduled death-particle bursts once per world
 * tick, so they can be delayed to line up with the moment a real EntitySeedBomb
 * actually detonates rather than the tick it's spawned on.
 */
public class PaleLungSeedBombTickHandler {

    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        if (event.side.isClient()) {
            return;
        }

        PaleLungSeedBomb.tick(event.world);
    }
}
