package nlblackeagle.dynamictreespalebloom.potion;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import nlblackeagle.dynamictreespalebloom.config.ForgeConfigHandler;

/**
 * Shared whitelist/blacklist gate for which entities Pale Lung is allowed to affect at
 * all, driven by {@link ForgeConfigHandler.PaleLungConfig#entityList} and
 * {@link ForgeConfigHandler.PaleLungConfig#entityListMode}. Used both to block new
 * applications outright (see PaleLungImmunityHandler) and to decide whether a nearby
 * Wither-to-Pale-Lung conversion should happen for a given entity.
 */
public class PaleLungEntityFilter {

    public static boolean isAllowed(Entity entity) {
        ResourceLocation key = keyFor(entity);

        boolean inList = false;
        if (key != null) {
            for (String entry : ForgeConfigHandler.paleLung.entityList) {
                if (key.toString().equals(entry)) {
                    inList = true;
                    break;
                }
            }
        }

        switch (ForgeConfigHandler.paleLung.entityListMode) {
            case WHITELIST:
                return inList;
            case BLACKLIST:
            default:
                return !inList;
        }
    }

    private static ResourceLocation keyFor(Entity entity) {
        if (entity instanceof EntityPlayer) {
            // Players aren't registered in EntityList (EntityList.getKey returns null
            // for them), so give them a stable, guessable key of their own.
            return new ResourceLocation("minecraft", "player");
        }
        return EntityList.getKey(entity);
    }
}
