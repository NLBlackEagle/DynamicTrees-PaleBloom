package nlblackeagle.dynamictreespalebloom.mixin;

import com.sirsquidly.palebloom.common.blocks.tileentity.TileIncenseThorn;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.Potion;
import nlblackeagle.dynamictreespalebloom.config.ForgeConfigHandler;
import nlblackeagle.dynamictreespalebloom.potion.ModPotions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Redirects {@code TileIncenseThorn#getPotion()} from vanilla Poison to Pale Lung.
 * <p>
 * This swaps at the getter rather than hooking whatever internally applies the effect
 * on collision (which we don't have visibility into, since Pale Bloom's source isn't
 * available in this project) - every consumer of the tile's stored potion, including
 * whatever applies it on touch AND this addon's own harvest-drop NBT preservation in
 * {@link BlockIncenseThornMixin}, reads it through this same getter, so redirecting it
 * here covers all of them without needing to know the exact collision-handling method.
 */
@Mixin(value = TileIncenseThorn.class, remap = false)
public class TileIncenseThornMixin {

    @Inject(method = "getPotion", at = @At("RETURN"), cancellable = true)
    private void dynamictreespalebloom$swapPoisonForPaleLung(CallbackInfoReturnable<Potion> cir) {
        if (!ForgeConfigHandler.paleLung.replaceIncenseThornsPoison) {
            return;
        }
        if (ModPotions.paleLung == null) {
            return;
        }

        if (cir.getReturnValue() == MobEffects.POISON) {
            cir.setReturnValue(ModPotions.paleLung);
        }
    }
}
