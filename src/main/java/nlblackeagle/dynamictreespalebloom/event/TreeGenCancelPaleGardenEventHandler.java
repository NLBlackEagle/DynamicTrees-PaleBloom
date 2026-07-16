package nlblackeagle.dynamictreespalebloom.event;

import com.ferreusveritas.dynamictrees.ModConfigs;
import com.ferreusveritas.dynamictrees.worldgen.BiomeDataBase;
import com.ferreusveritas.dynamictrees.worldgen.TreeGenerator;
import com.sirsquidly.palebloom.init.JTPGBiomes;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

// Adapted from the same pattern DefiledLands' addon uses - Pale Garden's tree
// placement (BiomePaleGarden.placeTrees) does actually fire the standard
// cancellable Forge event, contrary to what we assumed much earlier in this
// project. Cancelling it here stops Pale Bloom's own static trees so only our
// dynamic ones spawn, while checking DT's own worldgen is actually active for
// this dimension first, so we don't end up with zero trees if it isn't.
public class TreeGenCancelPaleGardenEventHandler {

    @SubscribeEvent(priority = EventPriority.NORMAL, receiveCanceled = true)
    public void onEvent(DecorateBiomeEvent.Decorate event) {
        int dimensionId = event.getWorld().provider.getDimension();
        BiomeDataBase dbase = TreeGenerator.getTreeGenerator().getBiomeDataBase(dimensionId);

        if (dbase != TreeGenerator.DIMENSIONBLACKLISTED && !ModConfigs.dimensionBlacklist.contains(dimensionId)) {
            Biome biome = event.getWorld().getBiome(event.getPos());
            if (biome == JTPGBiomes.PALE_GARDEN && event.getType() == DecorateBiomeEvent.Decorate.EventType.TREE) {
                event.setResult(Event.Result.DENY);
            }
        }
    }
}
