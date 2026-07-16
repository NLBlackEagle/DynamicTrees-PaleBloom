package nlblackeagle.dynamictreespalebloom.event;

import com.sirsquidly.palebloom.common.entity.EntityCreaking;
import com.sirsquidly.palebloom.init.JTPGBlocks;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import nlblackeagle.dynamictreespalebloom.ModContent;

public class CreakingHeartValidityHandler {

    @SubscribeEvent
    public void onCreakingUpdate(LivingEvent.LivingUpdateEvent event) {
        if (!(event.getEntityLiving() instanceof EntityCreaking)) return;

        EntityCreaking creaking = (EntityCreaking) event.getEntityLiving();
        World world = creaking.world;

        if (world.isRemote || !creaking.spawnedByHeart()) return;
        if (world.getTotalWorldTime() % 20L != 0L) return;

        BlockPos heartPos = creaking.getHeartPos();
        Block block = world.getBlockState(heartPos).getBlock();

        boolean validHeart = block == JTPGBlocks.CREAKING_HEART
                || block == ModContent.paleOakBranchCreakingHeart;

        if (!validHeart) {
            creaking.preformTwitchingDeath(1);
        }
    }
}
