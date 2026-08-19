package nlblackeagle.dynamictreespalebloom.mixin;

import com.sirsquidly.palebloom.common.blocks.tileentity.TileIncenseThorn;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.Potion;
import nlblackeagle.dynamictreespalebloom.config.ForgeConfigHandler;
import nlblackeagle.dynamictreespalebloom.potion.ModPotions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Redirects TileIncenseThorn's stored potion from Poison to Pale Lung by intercepting
 * setPotion(Potion) directly, rather than the getPotion() getter as originally
 * attempted.
 * <p>
 * Confirmed by decompiling the shipped palebloom-1.0.0.jar with CFR:
 * TileIncenseThorn#effectNearbyEntities() - the method that actually applies the area
 * effect to nearby entities - reads the private {@code potionType} field DIRECTLY,
 * never calling {@code getPotion()} at all. A getter-only redirect (the original
 * approach) therefore never touched the real poison application - it only affected
 * external callers of {@code getPotion()}, like this addon's own
 * BlockIncenseThornMixin harvest-drop NBT preservation.
 * <p>
 * Intercepting {@code setPotion} instead means every internal use of the field (area
 * effect, particle colour via setupPotionColor, NBT save/load) sees the corrected
 * value, since the field itself now actually holds it from the moment it's set -
 * whether that's Pale Bloom's own natural-growth code calling
 * {@code setPotion(MobEffects.POISON)}, or this addon's own Seed Bomb flora scatter
 * calling {@code setPotion(...)} directly.
 */
@Mixin(value = TileIncenseThorn.class)
public abstract class TileIncenseThornMixin {

    @Shadow
    private Potion potionType;

    @Inject(method = "setPotion", at = @At("HEAD"), cancellable = true)
    private void dynamictreespalebloom$swapPoisonForPaleLung(Potion potion, CallbackInfo ci) {
        if (!ForgeConfigHandler.featureToggles.enablePaleLung) {
            return;
        }
        if (!ForgeConfigHandler.paleLung.replacePollenheadAndIncenseThornsPoison) {
            return;
        }
        if (ModPotions.paleLung == null) {
            return;
        }

        if (potion == MobEffects.POISON) {
            this.potionType = ModPotions.paleLung;
            ci.cancel();
        }
    }
}
