package nlblackeagle.dynamictreespalebloom.potion;

import net.minecraft.entity.Entity;
import nlblackeagle.dynamictreespalebloom.config.ForgeConfigHandler;

/**
 * Shared whitelist/blacklist gate for which entities Pale Lung is allowed to affect at
 * all, driven by {@link ForgeConfigHandler.PaleLungConfig#entityList} and
 * {@link ForgeConfigHandler.PaleLungConfig#entityListMode} (matched via
 * {@link PaleLungEntityMatcher}, so registry names and @wildcards both work). Used
 * both to block new applications outright (see PaleLungImmunityHandler) and to decide
 * whether a nearby Wither-to-Pale-Lung conversion should happen for a given entity.
 * <p>
 * Every path that ever applies Pale Lung to an entity - Incense Thorns, Seed Bomb area
 * spread, Seed Bomb Wither conversion - ultimately fires PotionApplicableEvent, which
 * PaleLungImmunityHandler checks against this filter. That makes this the one
 * choke-point where the "Enable Pale Lung" master toggle can block the whole feature
 * without needing to be checked at every individual call site.
 */
public class PaleLungEntityFilter {

    public static boolean isAllowed(Entity entity) {
        if (!ForgeConfigHandler.featureToggles.enablePaleLung) {
            return false;
        }

        boolean inList = PaleLungEntityMatcher.matchesAny(ForgeConfigHandler.paleLung.entityList, entity);

        switch (ForgeConfigHandler.paleLung.entityListMode) {
            case WHITELIST:
                return inList;
            case BLACKLIST:
            default:
                return !inList;
        }
    }
}
