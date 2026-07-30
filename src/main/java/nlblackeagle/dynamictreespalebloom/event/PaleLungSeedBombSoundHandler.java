package nlblackeagle.dynamictreespalebloom.event;

import net.minecraft.client.audio.ISound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import nlblackeagle.dynamictreespalebloom.config.ForgeConfigHandler;
import nlblackeagle.dynamictreespalebloom.potion.PaleLungSeedBomb;

/**
 * Mutes the vanilla "entity.generic.explode" sound specifically when it's playing near
 * a recent Pale Lung Seed Bomb detonation (see {@link PaleLungSeedBomb#isRecentDetonation}),
 * since EntitySeedBomb is structurally a near-clone of vanilla's own EntityTNTPrimed
 * (same FUSE data parameter, same tntPlacedBy field, same explodeUnderwater method) and
 * uses that exact sound on detonation - confirmed directly from the shipped
 * palebloom-1.0.0.jar's constant pool, which references net.minecraft.init.SoundEvents.
 * <p>
 * Matches on the sound's own registry name rather than Pale Bloom's Java field name, so
 * this doesn't depend on knowing SoundEvents' SRG/MCP mapping at all.
 * <p>
 * Only reliable in singleplayer/integrated-server - see the caveat on
 * {@link PaleLungSeedBomb#isRecentDetonation}.
 */
@SideOnly(Side.CLIENT)
public class PaleLungSeedBombSoundHandler {

    private static final ResourceLocation GENERIC_EXPLODE = new ResourceLocation("minecraft", "entity.generic.explode");

    @SubscribeEvent
    public void onPlaySound(PlaySoundEvent event) {
        if (!ForgeConfigHandler.seedBomb.seedBombSilent) {
            return;
        }

        ISound sound = event.getSound();
        if (sound == null || !GENERIC_EXPLODE.equals(sound.getSoundLocation())) {
            return;
        }

        if (PaleLungSeedBomb.isRecentDetonation(sound.getXPosF(), sound.getYPosF(), sound.getZPosF())) {
            event.setResultSound(null);
        }
    }
}
