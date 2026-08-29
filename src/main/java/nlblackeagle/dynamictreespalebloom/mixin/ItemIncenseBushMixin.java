package nlblackeagle.dynamictreespalebloom.mixin;

import com.sirsquidly.palebloom.common.item.itemblock.ItemIncenseBush;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import nlblackeagle.dynamictreespalebloom.config.ForgeConfigHandler;
import nlblackeagle.dynamictreespalebloom.potion.ModPotions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * ItemIncenseBush#addInformation shows "No effect" (grey) whenever the stack has no
 * "potion" NBT tag at all - which, now that Incense Thorns' crafting recipes are gone
 * (see "Disable Incense Thorns"), is basically only reachable via a bare creative-menu
 * item. Since Pale Lung is the intended default for this whole item now, this shows
 * "Pale Lung" instead - same bare-name, coloured style the tooltip already uses for a
 * real assigned potion (e.g. plain "Poison" in red, no extra wording), rather than a
 * longer sentence, to stay visually consistent with the existing tooltip.
 * <p>
 * Only touches the "no potion tag" branch - a stack that already has a real potion tag
 * (Pale Lung or otherwise) is left completely alone, so the original method still
 * handles that case normally.
 */
@Mixin(ItemIncenseBush.class)
public class ItemIncenseBushMixin {

    @Inject(method = {"addInformation"}, at = @At("HEAD"), cancellable = true)
    private void dynamictreespalebloom$noEffectShowsPaleLung(ItemStack stack, World world, List<String> tooltip, ITooltipFlag flag, CallbackInfo ci) {
        if (!ForgeConfigHandler.featureToggles.enablePaleLung) {
            return;
        }
        if (!ForgeConfigHandler.paleLung.replacePollenheadAndIncenseThornsPoison) {
            return;
        }
        if (ModPotions.paleLung == null) {
            return;
        }

        NBTTagCompound nbt = stack.getTagCompound();
        if (nbt != null && nbt.hasKey("potion")) {
            // Already has a real potion tag (Pale Lung or otherwise) - let the
            // original method handle it normally.
            return;
        }

        ci.cancel();
        tooltip.add(TextFormatting.RED + I18n.format(ModPotions.paleLung.getName()));
    }
}
