package nlblackeagle.dynamictreespalebloom.event;

import com.sirsquidly.palebloom.common.entity.EntityReapingWillow;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import nlblackeagle.dynamictreespalebloom.config.ForgeConfigHandler;

// Makes Reaping Willow always aggressive toward players on sight. It
// currently has no ability to spontaneously target a player at all - only
// EntityAIHurtByTarget is registered (retaliate if attacked first), no
// EntityAINearestAttackableTarget equivalent.
//
// Deliberately NOT a Mixin into initEntityAI() - that would require
// @Shadow-ing targetTasks, a field inherited from vanilla EntityLiving but
// not declared on EntityReapingWillow itself, on a remap=false third-party
// target. That's genuinely uncertain territory given the risk already seen
// once this session with a similarly-inherited method causing a cascading
// class-load failure. A normal LivingUpdateEvent handler using the public
// setAttackTarget() API achieves the same result far more safely, matching
// the same proven pattern as CreakingHeartValidityHandler.
public class ReapingWillowAggressionHandler {

    private static final double AGGRO_RANGE = 16.0D;

    @SubscribeEvent
    public void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        if (!ForgeConfigHandler.rlcraftDregora.reapingWillowAggressive) return;
        if (!(event.getEntityLiving() instanceof EntityReapingWillow)) return;

        EntityReapingWillow willow = (EntityReapingWillow) event.getEntityLiving();
        if (willow.world.isRemote) return;
        if (willow.getAttackTarget() != null) return;
        if (willow.ticksExisted % 20 != 0) return; // throttle to once per second

        World world = willow.world;
        EntityPlayer nearest = world.getClosestPlayerToEntity(willow, AGGRO_RANGE);
        if (nearest != null && !nearest.isCreative() && !nearest.isSpectator()) {
            willow.setAttackTarget(nearest);
        }
    }
}
