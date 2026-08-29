package nlblackeagle.dynamictreespalebloom.mixin;

import com.sirsquidly.palebloom.common.entity.EntityReapingWillow;
import com.sirsquidly.palebloom.common.entity.item.EntitySeedBomb;
import net.minecraft.entity.EntityLivingBase;
import nlblackeagle.dynamictreespalebloom.config.ForgeConfigHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Pale Bloom hardcodes EntitySeedBomb's own effect range to fixed values entirely
 * independent of anything this addon exposes as config - so raising "Seed Bomb Radius"
 * or "Reaping Willow Radius" never actually widened the native explosion's own effect
 * range, only this addon's own supplementary systems (flora scatter, Pale Lung spread,
 * particles).
 * <p>
 * Confirmed directly by decompiling the shipped palebloom-1.0.0.jar's private
 * explodeUnderwater() method (and WorldGenMoss) with CFR. There are actually TWO
 * separate hardcoded ranges in play, not one:
 * <p>
 * 1) The Wither/Creeper-conversion range: grows the entity's own bounding box by
 * (4.0, 2.0, 4.0), then further requires getDistanceSq(entity) &lt; 16.0 (i.e. actual
 * distance &lt; 4.0) before a Wither duration is computed via
 * (1.0 - sqrt(distanceSq) / 4.0) * 320 + 0.5.
 * <p>
 * 2) The Pale Moss ground-cover spread - the actual most visually obvious "explosion
 * size" - is a completely separate code path: {@code new WorldGenMoss(10, 1.0f)},
 * where WorldGenMoss's constructor is {@code WorldGenMoss(int radiusIn, float
 * carpetChanceIn)}. That {@code 10} is an int, spread over both X/Z as
 * {@code (maxRadius - 1) / 2} per axis, and was never touched by only modifying the
 * double constants above - this is almost certainly why the radius still looked
 * unchanged after the first pass.
 * <p>
 * This mixin scales all of it consistently to whichever radius actually applies -
 * Reaping Willow's own radius if this Seed Bomb's igniter is an EntityReapingWillow,
 * the general Seed Bomb radius otherwise (covers both this addon's own triggers and
 * any plain player-thrown "palebloom:seed_bomb" item, which also goes through this
 * same class).
 */
@Mixin(EntitySeedBomb.class)
public abstract class EntitySeedBombMixin {

    @Shadow
    private EntityLivingBase tntPlacedBy;

    // Covers the AABB grow's x/z (4.0) AND the Wither falloff divisor (also literally
    // 4.0 in the decompiled source) - both should scale together, and ModifyConstant
    // without an ordinal replaces every occurrence of a matching constant in the
    // method, which is exactly what's wanted here.
    @ModifyConstant(method = "explodeUnderwater", constant = @Constant(doubleValue = 4.0D), remap = false)
    private double dynamictreespalebloom$scaleHorizontalRadius(double original) {
        if (!ForgeConfigHandler.featureToggles.enableSeedBomb) {
            return original;
        }
        return resolveRadius();
    }

    @ModifyConstant(method = "explodeUnderwater", constant = @Constant(doubleValue = 2.0D), remap = false)
    private double dynamictreespalebloom$scaleVerticalRadius(double original) {
        if (!ForgeConfigHandler.featureToggles.enableSeedBomb) {
            return original;
        }
        // Keep the original 4.0 -> 2.0 (2:1) horizontal:vertical ratio.
        return resolveRadius() * 0.5D;
    }

    @ModifyConstant(method = "explodeUnderwater", constant = @Constant(doubleValue = 16.0D), remap = false)
    private double dynamictreespalebloom$scaleDistanceThreshold(double original) {
        if (!ForgeConfigHandler.featureToggles.enableSeedBomb) {
            return original;
        }
        double radius = resolveRadius();
        return radius * radius;
    }

    // The actual Pale Moss spread radius (WorldGenMoss's own maxRadius constructor
    // arg) - see class javadoc. This is the fix that most likely matters visually.
    @ModifyConstant(method = "explodeUnderwater", constant = @Constant(intValue = 10), remap = false)
    private int dynamictreespalebloom$scaleMossRadius(int original) {
        if (!ForgeConfigHandler.featureToggles.enableSeedBomb) {
            return original;
        }
        int radius = (int) Math.round(resolveRadius());
        // WorldGenMoss computes its per-axis radius as (maxRadius - 1) / 2, so solve
        // for the maxRadius that makes that equal our configured radius.
        return radius * 2 + 1;
    }

    // The ambient TOWN_AURA-style particle scatter range (dX/dY/dZ jitter), purely
    // cosmetic but scaled too for visual consistency with everything else here.
    @ModifyConstant(method = "explodeUnderwater", constant = @Constant(doubleValue = 6.0D), remap = false)
    private double dynamictreespalebloom$scaleParticleSpreadRange(double original) {
        if (!ForgeConfigHandler.featureToggles.enableSeedBomb) {
            return original;
        }
        return resolveRadius() * 2.0D;
    }

    @ModifyConstant(method = "explodeUnderwater", constant = @Constant(doubleValue = 3.0D), remap = false)
    private double dynamictreespalebloom$scaleParticleSpreadOffset(double original) {
        if (!ForgeConfigHandler.featureToggles.enableSeedBomb) {
            return original;
        }
        return resolveRadius();
    }

    private double resolveRadius() {
        if (this.tntPlacedBy instanceof EntityReapingWillow) {
            return ForgeConfigHandler.seedBomb.reapingWillowRadius;
        }
        return ForgeConfigHandler.seedBomb.seedBombRadius;
    }
}
