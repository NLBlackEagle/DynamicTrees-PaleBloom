package nlblackeagle.dynamictreespalebloom.mixin;

import com.sirsquidly.palebloom.common.blocks.tileentity.TileResinBulb;
import com.sirsquidly.palebloom.common.world.WorldPaleGarden;
import com.sirsquidly.palebloom.config.ConfigCache;
import com.sirsquidly.palebloom.config.ConfigParser;
import com.sirsquidly.palebloom.init.JTPGSounds;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import nlblackeagle.dynamictreespalebloom.config.ForgeConfigHandler;
import nlblackeagle.dynamictreespalebloom.util.ResinBulbScanThrottle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

/**
 * Two independent TileResinBulb fixes (confirmed by decompiling the shipped
 * palebloom-1.0.0.jar):
 * <p>
 * 1) tryActiveHeartHarvest's nightly Creaking Heart scan is a brute-force 21x21x21
 * (radius 10) block scan, calling getTileEntity() on every position, every 300 ticks
 * (15s) - and it runs unconditionally even when the bulb is already at max resin
 * (where the result would just be discarded), with every loaded bulb firing on the
 * exact same tick since none of them are staggered. "Optimize Resin Bulb Heart
 * Search" skips the scan when already maxed, shrinks the radius to 8 (17x17x17),
 * doubles the interval to 600 ticks (30s), and caps concurrent scans to 10/tick via
 * {@link ResinBulbScanThrottle}. The scan itself already returns immediately on
 * finding a match - that part needed no change.
 * <p>
 * 2) tryResinHarvest's daytime plant search has a real axis-mixup bug: it applies
 * {@code bulbCheckDistanceXZ} (8) to both the X *and* Y offsets, and
 * {@code bulbCheckDistanceY} (10) to the Z offset - clearly backwards given the field
 * names ("XZ" = horizontal, "Y" = vertical). "Fix Resin Bulb Search Area Bug" cancels
 * the original method and reimplements it identically, just with XZ correctly
 * governing X/Z and Y governing Y.
 * <p>
 * 3) "Creaking Heart Resin Charge Multiplier" scales tryActiveHeartHarvest's resin
 * grant (ConfigCache.rsnBlb_creakingHeartResinReap, Pale Bloom's own config value) by
 * redirecting the field read itself, so a successful nightly charge is worth more
 * without touching the search radius, interval, or the daytime harvest amount.
 */
@Mixin(value = TileResinBulb.class, remap = false)
public abstract class TileResinBulbMixin extends TileEntity {

    @Shadow
    public int maxResin;

    @Shadow
    public int storedResin;

    @Shadow
    public int bulbCheckDistanceXZ;

    @Shadow
    public int bulbCheckDistanceY;

    @Inject(method = "tryActiveHeartHarvest", at = @At("HEAD"), cancellable = true)
    private void dynamictreespalebloom$optimizeScan(World world, BlockPos pos, CallbackInfo ci) {
        if (!ForgeConfigHandler.miscellaneous.optimizeResinBulbHeartSearch) {
            return;
        }

        if (this.storedResin >= this.maxResin) {
            ci.cancel();
            return;
        }

        if (!ResinBulbScanThrottle.tryAcquire(world)) {
            ci.cancel();
        }
    }

    @ModifyConstant(method = "tryActiveHeartHarvest", constant = @Constant(intValue = 10))
    private int dynamictreespalebloom$shrinkRadius(int original) {
        if (!ForgeConfigHandler.miscellaneous.optimizeResinBulbHeartSearch) {
            return original;
        }
        return 8;
    }

    // Redirects the field read itself (rather than wrapping setStoredResin, whose
    // argument is already the post-min()-clamped total) so the multiplier scales just
    // the reap amount, leaving the max-resin clamp it feeds into untouched.
    @Redirect(method = "tryActiveHeartHarvest", at = @At(value = "FIELD", target = "Lcom/sirsquidly/palebloom/config/ConfigCache;rsnBlb_creakingHeartResinReap:I"))
    private int dynamictreespalebloom$buffCreakingHeartResinReap() {
        return (int) Math.round(ConfigCache.rsnBlb_creakingHeartResinReap * ForgeConfigHandler.miscellaneous.creakingHeartResinChargeMultiplier);
    }

    @ModifyConstant(method = {"update", "func_73660_a"}, constant = @Constant(longValue = 300L))
    private long dynamictreespalebloom$slowInterval(long original) {
        if (!ForgeConfigHandler.miscellaneous.optimizeResinBulbHeartSearch) {
            return original;
        }
        return 600L;
    }

    @Inject(method = "tryResinHarvest", at = @At("HEAD"), cancellable = true)
    private void dynamictreespalebloom$fixSearchAxisBug(World world, BlockPos pos, Random random, CallbackInfo ci) {
        if (!ForgeConfigHandler.miscellaneous.fixResinBulbSearchAreaBug) {
            return;
        }

        ci.cancel();

        // XZ governs X and Z, Y governs Y - matching what the field names promise,
        // unlike the original (XZ on X and Y, Y on Z).
        int offsetX = world.rand.nextInt(this.bulbCheckDistanceXZ * 2) - this.bulbCheckDistanceXZ;
        int offsetZ = world.rand.nextInt(this.bulbCheckDistanceXZ * 2) - this.bulbCheckDistanceXZ;
        int offsetY = world.rand.nextInt(this.bulbCheckDistanceY * 2) - this.bulbCheckDistanceY;
        BlockPos checkPos = pos.add(offsetX, offsetY, offsetZ);

        int index = ConfigParser.blockResinBulbCollectFROM.indexOf(world.getBlockState(checkPos));
        if (index < 0) {
            return;
        }

        int resinCollected = ConfigParser.blockResinBulbCollectQUANITTY.get(index);
        this.storedResin = Math.min(this.maxResin, this.storedResin + resinCollected);
        this.markDirty();
        world.playSound(null, pos, JTPGSounds.BLOCK_RESIN_PLACE, SoundCategory.BLOCKS, 0.25F, world.rand.nextFloat() * 0.4F + 0.8F);
        WorldPaleGarden.spawnParticles(world, checkPos, pos, 1, 2);
    }
}
