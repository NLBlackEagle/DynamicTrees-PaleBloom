package nlblackeagle.dynamictreespalebloom.event;

import com.sirsquidly.palebloom.common.blocks.BlockPollenhead;
import com.sirsquidly.palebloom.common.blocks.IGardenState;
import com.sirsquidly.palebloom.common.entity.EntityReapingWillow;
import com.sirsquidly.palebloom.common.entity.item.EntitySeedBomb;
import com.sirsquidly.palebloom.init.JTPGBlocks;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import nlblackeagle.dynamictreespalebloom.config.ForgeConfigHandler;
import nlblackeagle.dynamictreespalebloom.potion.PaleLungSeedBomb;

// On death, Reaping Willow explodes like a real Seed Bomb and places a
// Pollenhead. Rather than reimplementing EntitySeedBomb's explosion logic
// ourselves (its actual effect is private, and genuinely not a normal
// explosion at all - it spreads Pale Moss, applies Wither to nearby
// non-pale creatures based on distance, and converts nearby Creepers to
// Pale Creepers, with no block destruction), this spawns a real
// EntitySeedBomb at the death location with its fuse forced to zero, so it
// detonates on its own very next tick using the mod's own real logic
// natively.
public class ReapingWillowDeathHandler {

    @SubscribeEvent
    public void onDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof EntityReapingWillow)) return;
        if (!ForgeConfigHandler.rlcraftDregora.reapingWillowExplodeOnDeath) return;

        Entity entity = event.getEntity();
        World world = entity.world;
        if (world.isRemote) return;

        BlockPos pos = entity.getPosition();

        if (world.isAirBlock(pos) && world.isAirBlock(pos.up())) {
            ((BlockPollenhead) JTPGBlocks.POLLENHEAD).placeDoubleAt(world, pos, IGardenState.EnumLucidityState.DORMANT, 2);
        }

        EntitySeedBomb seedBomb = new EntitySeedBomb(world, entity.posX, entity.posY, entity.posZ, (EntityReapingWillow) entity);
        seedBomb.setFuse(0);
        world.spawnEntity(seedBomb);

        PaleLungSeedBomb.trigger(world, pos, entity, ForgeConfigHandler.seedBomb.reapingWillowFloraPool, ForgeConfigHandler.seedBomb.reapingWillowRadius);
    }
}
