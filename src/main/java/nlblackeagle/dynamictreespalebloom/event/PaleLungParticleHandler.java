package nlblackeagle.dynamictreespalebloom.event;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumParticleTypes;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import nlblackeagle.dynamictreespalebloom.config.ForgeConfigHandler;
import nlblackeagle.dynamictreespalebloom.potion.ModPotions;

import java.util.Random;

/**
 * While an entity is affected by Pale Lung, occasionally emits a white ambient
 * particle around it. Hooks the same per-entity tick vanilla uses for its own ambient
 * potion particles, so this naturally covers every nearby affected entity (players and
 * mobs alike), not just the local player.
 * <p>
 * Uses EnumParticleTypes.SPELL_MOB, which - like any potion "spell" particle - takes
 * its RGB colour from the speed/offset parameters passed to spawnParticle rather than
 * from the particle texture itself, so it can be recoloured white directly.
 */
@SideOnly(Side.CLIENT)
public class PaleLungParticleHandler {

    private static final Random RAND = new Random();

    @SubscribeEvent
    public void onLivingUpdate(LivingUpdateEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        if (!entity.world.isRemote) {
            return;
        }
        if (ModPotions.paleLung == null || !entity.isPotionActive(ModPotions.paleLung)) {
            return;
        }

        double chance = ForgeConfigHandler.paleLung.ambientParticleChance;
        if (chance <= 0 || RAND.nextDouble() > chance) {
            return;
        }

        double x = entity.posX + (RAND.nextDouble() - 0.5) * entity.width;
        double y = entity.posY + RAND.nextDouble() * entity.height;
        double z = entity.posZ + (RAND.nextDouble() - 0.5) * entity.width;

        entity.world.spawnParticle(EnumParticleTypes.SPELL_MOB, x, y, z, 1.0D, 1.0D, 1.0D);
    }
}
