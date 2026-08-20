package nlblackeagle.dynamictreespalebloom.event;

import com.sirsquidly.palebloom.common.entity.EntityReapingWillow;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import nlblackeagle.dynamictreespalebloom.config.ForgeConfigHandler;

// Adds configurable extra item drops to Reaping Willow, on top of its
// normal palebloom loot table (2x Amber Valve, 38-50x Pale Oak Log). Uses
// LivingDropsEvent rather than overriding palebloom's loot table json, so
// the drop list stays config-driven without needing a resource override.
public class ReapingWillowDropsHandler {

    @SubscribeEvent
    public void onDrops(LivingDropsEvent event) {
        if (!ForgeConfigHandler.featureToggles.enableRLCraftDregora) return;
        if (!ForgeConfigHandler.rlcraftDregora.enableReapingWillowExtraDrops) return;
        if (!(event.getEntityLiving() instanceof EntityReapingWillow)) return;

        EntityReapingWillow willow = (EntityReapingWillow) event.getEntityLiving();

        for (String entry : ForgeConfigHandler.rlcraftDregora.reapingWillowExtraDrops) {
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(entry));
            if (item == null) continue;

            event.getDrops().add(new EntityItem(willow.world, willow.posX, willow.posY, willow.posZ, new ItemStack(item)));
        }
    }
}