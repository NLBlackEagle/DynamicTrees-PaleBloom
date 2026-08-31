package nlblackeagle.dynamictreespalebloom.event;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import nlblackeagle.dynamictreespalebloom.config.ForgeConfigHandler;
import nlblackeagle.dynamictreespalebloom.potion.ModPotions;

/**
 * Makes any Palebloom weapon (an {@link ItemSword} registered under the "palebloom"
 * domain - currently just the Cultivar Sword, but this picks up any future Palebloom
 * weapon automatically without needing its own entry here) inflict Pale Lung on
 * whatever it hits in melee, alongside its normal damage.
 * <p>
 * Gated behind both the "Enable Pale Lung" master switch and its own "Weapons Inflict
 * Pale Lung" toggle. Whether the application actually sticks (entity list, Respiration
 * immunity) is still fully handled by {@link PaleLungImmunityHandler}, same as every
 * other Pale Lung source - this only decides whether to attempt it.
 */
public class PaleLungWeaponHandler {

    @SubscribeEvent
    public void onLivingHurt(LivingHurtEvent event) {
        if (!ForgeConfigHandler.featureToggles.enablePaleLung || !ForgeConfigHandler.paleLung.weaponsInflictPaleLung) {
            return;
        }
        if (ModPotions.paleLung == null) {
            return;
        }

        Entity source = event.getSource().getTrueSource();
        if (!(source instanceof EntityLivingBase)) {
            return;
        }

        EntityLivingBase attacker = (EntityLivingBase) source;
        if (attacker.world.isRemote) {
            return;
        }

        ItemStack held = attacker.getHeldItemMainhand();
        if (held.isEmpty() || !(held.getItem() instanceof ItemSword)) {
            return;
        }

        if (held.getItem().getRegistryName() == null
                || !"palebloom".equals(held.getItem().getRegistryName().getResourceDomain())) {
            return;
        }

        event.getEntityLiving().addPotionEffect(new PotionEffect(ModPotions.paleLung,
                ForgeConfigHandler.paleLung.weaponPaleLungDuration,
                ForgeConfigHandler.paleLung.weaponPaleLungAmplifier));
    }
}
