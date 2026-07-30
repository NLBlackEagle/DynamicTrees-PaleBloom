package nlblackeagle.dynamictreespalebloom.event;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Enchantments;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.PotionEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import nlblackeagle.dynamictreespalebloom.config.ForgeConfigHandler;
import nlblackeagle.dynamictreespalebloom.potion.ModPotions;
import nlblackeagle.dynamictreespalebloom.potion.PaleLungEntityFilter;
import nlblackeagle.dynamictreespalebloom.potion.PaleLungSeedBomb;

/**
 * Gatekeeps Pale Lung: blocks it from ever being applied to entities excluded by the
 * whitelist/blacklist config, grants immunity to anyone wearing a Respiration helmet,
 * and - near a recent Pale Lung Seed Bomb detonation specifically - intercepts and
 * replaces any Wither the native explosion would otherwise apply with Pale Lung
 * instead (still subject to the entity list).
 */
public class PaleLungImmunityHandler {

    @SubscribeEvent
    public void onPotionApplicable(PotionEvent.PotionApplicableEvent event) {
        EntityLivingBase entity = event.getEntityLiving();

        if (ModPotions.paleLung != null && event.getPotionEffect().getPotion() == ModPotions.paleLung) {
            if (!PaleLungEntityFilter.isAllowed(entity)) {
                event.setResult(Event.Result.DENY);
                return;
            }
            if (ForgeConfigHandler.paleLung.respirationGrantsImmunity && hasRespiration(entity)) {
                event.setResult(Event.Result.DENY);
            }
            return;
        }

        if (ForgeConfigHandler.seedBomb.seedBombConvertsWither
                && ModPotions.paleLung != null
                && event.getPotionEffect().getPotion() == MobEffects.WITHER
                && PaleLungEntityFilter.isAllowed(entity)
                && PaleLungSeedBomb.isRecentDetonation(entity.posX, entity.posY, entity.posZ)) {
            event.setResult(Event.Result.DENY);
            PotionEffect witherEffect = event.getPotionEffect();
            entity.addPotionEffect(new PotionEffect(ModPotions.paleLung, witherEffect.getDuration(), witherEffect.getAmplifier()));
        }
    }

    @SubscribeEvent
    public void onLivingUpdate(LivingUpdateEvent event) {
        if (!ForgeConfigHandler.paleLung.respirationGrantsImmunity) {
            return;
        }
        if (ModPotions.paleLung == null) {
            return;
        }

        EntityLivingBase entity = event.getEntityLiving();
        if (entity.world.isRemote) {
            return;
        }

        if (entity.isPotionActive(ModPotions.paleLung) && hasRespiration(entity)) {
            entity.removePotionEffect(ModPotions.paleLung);
        }
    }

    private boolean hasRespiration(EntityLivingBase entity) {
        ItemStack helmet = entity.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
        return !helmet.isEmpty() && EnchantmentHelper.getEnchantmentLevel(Enchantments.RESPIRATION, helmet) > 0;
    }
}
