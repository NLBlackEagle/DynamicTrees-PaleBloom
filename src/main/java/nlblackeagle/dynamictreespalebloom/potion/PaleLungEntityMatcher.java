package nlblackeagle.dynamictreespalebloom.potion;

import net.minecraft.entity.Entity;

import net.minecraft.entity.EntityList;

import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityAmbientCreature;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

import java.util.Locale;

/**
 * Shared matcher for this addon's various entity list configs (Pale Lung Entity List,
 * Pale Lung Entity Damage List, Seed Bomb Affects Entities). Each entry in a list can
 * be either:
 * <ul>
 *   <li>a literal entity registry name, e.g. {@code "minecraft:zombie"},
 *       {@code "minecraft:player"}</li>
 *   <li>{@code "@all"} - matches every entity</li>
 *   <li>{@code "@player"} - matches EntityPlayer specifically</li>
 *   <li>{@code "@hostile"} - matches entities implementing IMob</li>
 *   <li>{@code "@passive"} - matches anything that's neither IMob nor EntityPlayer.
 *       This is a simple elimination heuristic rather than a perfect classification -
 *       not every modded entity declares itself via a common "passive" interface, but
 *       everything that isn't hostile or a player reasonably counts as passive here
 *       (this also covers {@code @ambient} creatures as a subset)</li>
 *   <li>{@code "@ambient"} - matches EntityAmbientCreature (vanilla bats, and anything
 *       else extending that class)</li>
 *   <li>{@code "@<anything else>"} - treated as a modid filter, matching any entity
 *       whose registry namespace equals that word - covers e.g. {@code "@palebloom"},
 *       {@code "@srparasites"}</li>
 * </ul>
 */
public class PaleLungEntityMatcher {

    public static boolean matchesAny(String[] patterns, Entity entity) {
        if (patterns == null || entity == null) {
            return false;
        }
        for (String pattern : patterns) {
            if (matches(pattern, entity)) {
                return true;
            }
        }
        return false;
    }

    public static boolean matches(String pattern, Entity entity) {
        if (pattern == null || pattern.isEmpty() || entity == null) {
            return false;
        }

        if (pattern.charAt(0) == '@') {
            String keyword = pattern.substring(1).toLowerCase(Locale.ENGLISH);
            switch (keyword) {
                case "all":
                    return true;
                case "player":
                    return entity instanceof EntityPlayer;
                case "hostile":
                    return entity instanceof IMob;
                case "passive":
                    return !(entity instanceof IMob) && !(entity instanceof EntityPlayer);
                case "ambient":
                    return entity instanceof EntityAmbientCreature;
                default:
                    // Any other @word is treated as a modid filter.
                    ResourceLocation key = keyFor(entity);
                    return key != null && key.getResourceDomain().equalsIgnoreCase(keyword);
            }
        }

        ResourceLocation key = keyFor(entity);
        return key != null && key.toString().equals(pattern);
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
