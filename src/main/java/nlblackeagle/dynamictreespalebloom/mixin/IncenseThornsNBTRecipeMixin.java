package nlblackeagle.dynamictreespalebloom.mixin;

import com.sirsquidly.palebloom.util.IncenseThornsNBTRecipe;
import nlblackeagle.dynamictreespalebloom.config.ForgeConfigHandler;
import nlblackeagle.dynamictreespalebloom.potion.ModPotions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Pale Bloom ships a family of crafting recipes (incense_thorns_poison.json,
 * _speed.json, _strength.json, etc.) that each bake a potion resource-location
 * STRING directly into the crafted item's NBT under a lowercase "potion" key, via
 * this IncenseThornsNBTRecipe class's constructor - completely independent of
 * TileIncenseThorn's own "Potion" (capitalized) NBT key and setPotion() method (see
 * TileIncenseThornMixin), and independent of the item's own tooltip rendering
 * (ItemIncenseBush#addInformation), which just re-parses whatever string is stored
 * here and looks it up in the shared cross-mod Potion registry.
 * <p>
 * This intercepts the constructor's potion-string parameter and swaps
 * "minecraft:poison" specifically for Pale Lung's own registry name, leaving every
 * other flavour (speed, strength, weakness, regeneration, slowness) completely
 * untouched. Since the tooltip just resolves whatever string ends up here through
 * the registry and localizes via the potion's own name, no separate tooltip fix is
 * needed - it'll show "Pale Lung" automatically once the NBT itself says so.
 */
@Mixin(value = IncenseThornsNBTRecipe.class)
public class IncenseThornsNBTRecipeMixin {

    @ModifyVariable(method = "<init>", at = @At("HEAD"), argsOnly = true)
    private static String dynamictreespalebloom$swapPoisonForPaleLung(String potionEffectIn) {
        if (!ForgeConfigHandler.featureToggles.enablePaleLung) {
            return potionEffectIn;
        }
        if (!ForgeConfigHandler.paleLung.replacePollenheadAndIncenseThornsPoison) {
            return potionEffectIn;
        }
        if (ModPotions.paleLung == null) {
            return potionEffectIn;
        }

        if ("minecraft:poison".equals(potionEffectIn)) {
            return ModPotions.paleLung.getRegistryName().toString();
        }

        return potionEffectIn;
    }
}
