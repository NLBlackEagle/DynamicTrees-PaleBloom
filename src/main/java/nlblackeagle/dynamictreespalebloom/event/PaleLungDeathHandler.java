package nlblackeagle.dynamictreespalebloom.event;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import nlblackeagle.dynamictreespalebloom.config.ForgeConfigHandler;
import nlblackeagle.dynamictreespalebloom.potion.ModPotions;
import nlblackeagle.dynamictreespalebloom.potion.PaleLungSeedBomb;

/**
 * When an entity dies while affected by Pale Lung, spawns a real Seed Bomb (see
 * {@link PaleLungSeedBomb#spawnNative}), scatters configured flora (guaranteed at the
 * death spot itself, plus more scattered around it), and spreads Pale Lung to nearby
 * living entities - the same "seed bomb" flourish Reaping Willow gets on death (see
 * {@link ReapingWillowDeathHandler}), minus the hardcoded Pollenhead - the configured
 * flora pool is what shows up at the death spot instead.
 */
public class PaleLungDeathHandler {

    @SubscribeEvent
    public void onDeath(LivingDeathEvent event) {
        if (!ForgeConfigHandler.featureToggles.enableSeedBomb) {
            return;
        }
        if (!ForgeConfigHandler.seedBomb.seedBombOnDeath) {
            return;
        }
        if (ModPotions.paleLung == null) {
            return;
        }

        EntityLivingBase entity = event.getEntityLiving();
        if (entity == null || !entity.isPotionActive(ModPotions.paleLung)) {
            return;
        }

        World world = entity.world;
        if (world.isRemote) {
            return;
        }

        BlockPos pos = entity.getPosition();

        PaleLungSeedBomb.spawnNative(world, entity, ForgeConfigHandler.seedBomb.seedBombRadius);
        PaleLungSeedBomb.trigger(world, pos, entity, ForgeConfigHandler.seedBomb.seedBombFloraPool, ForgeConfigHandler.seedBomb.seedBombRadius);
    }
}
