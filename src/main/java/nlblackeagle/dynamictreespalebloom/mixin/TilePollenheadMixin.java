package nlblackeagle.dynamictreespalebloom.mixin;

import com.sirsquidly.palebloom.common.blocks.tileentity.TilePollenhead;
import com.sirsquidly.palebloom.common.world.WorldPaleGarden;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import nlblackeagle.dynamictreespalebloom.config.ForgeConfigHandler;
import nlblackeagle.dynamictreespalebloom.potion.ModPotions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * TilePollenhead#poisonNearbyEntities() (confirmed by decompiling the shipped
 * palebloom-1.0.0.jar) hardcodes a direct {@code MobEffects.POISON} reference with no
 * stored field or setter to redirect at all - unlike TileIncenseThorn, which at least
 * has a {@code potionType} field (see TileIncenseThornMixin). There's no clean
 * single-point swap available here, so this cancels the original method entirely and
 * reimplements its exact same logic (same AABB, same sphere-distance check, same
 * Creeper-conversion behaviour) whenever either of two independent things are wanted:
 * <ul>
 *   <li>swapping Poison for Pale Lung ("Replace Pollenhead & Incense Thorns Poison"
 *       under Pale Lung Potion Options, requires "Enable Pale Lung" too)</li>
 *   <li>fixing a real Pale Bloom bug where the original method uses {@code return}
 *       instead of {@code continue} in its pale-entity check, meaning finding a
 *       single pale entity nearby stops it from checking every other entity in range
 *       that tick ("Fix Pollenhead Pale Entity Check" under Miscellaneous)</li>
 * </ul>
 * These two are independent - you can have the bug fix without the Pale Lung swap,
 * the swap without the bug fix (keeping the original buggy behaviour, just with Pale
 * Lung instead of Poison), both, or neither (in which case the original native method
 * runs completely untouched).
 */
@Mixin(value = TilePollenhead.class, remap = false)
public abstract class TilePollenheadMixin extends TileEntity {

    // world/pos are inherited from vanilla TileEntity - they're NOT declared in
    // TilePollenhead itself (confirmed directly from a decompile of the actual target
    // class: its own fields are just storedResin, resinMinPull, resinPullQuantity,
    // isAwake, pollenDistanceXZ, pollenDistanceY, poisonDistance, hybridizeDistance).
    // @Shadow only searches the target class's OWN declared fields, never superclass
    // fields, no matter what alias is given - that's why the earlier @Shadow(aliases=
    // "field_145850_b"/"field_174879_c") attempt still failed identically. Extending
    // TileEntity here instead gives real, normal Java inheritance for world/pos
    // (Mixin discards this fake inheritance during the actual bytecode merge - it's
    // purely a compile-time trick so the field references below resolve correctly).
    // poisonDistance below still needs @Shadow, since that one genuinely is declared
    // directly on TilePollenhead itself.
    @Shadow
    public int poisonDistance;

    @Inject(method = "poisonNearbyEntities", at = @At("HEAD"), cancellable = true)
    private void dynamictreespalebloom$fixAndReplace(CallbackInfo ci) {
        boolean swapPotion = ForgeConfigHandler.featureToggles.enablePaleLung
                && ForgeConfigHandler.paleLung.replacePollenheadAndIncenseThornsPoison
                && ModPotions.paleLung != null;
        boolean fixBug = ForgeConfigHandler.miscellaneous.fixPollenheadPaleEntityCheckBug;

        if (!swapPotion && !fixBug) {
            // Neither toggle wants anything changed - let the original native method
            // run completely untouched.
            return;
        }

        ci.cancel();

        Potion potion = swapPotion ? ModPotions.paleLung : MobEffects.POISON;

        AxisAlignedBB area = new AxisAlignedBB(
                this.pos.add(-this.poisonDistance, -this.poisonDistance, -this.poisonDistance),
                this.pos.add(this.poisonDistance + 1, this.poisonDistance + 1, this.poisonDistance + 1));

        for (EntityLivingBase entity : this.world.getEntitiesWithinAABB(EntityLivingBase.class, area)) {
            if (WorldPaleGarden.isPaleEntity(entity)) {
                if (fixBug) {
                    continue;
                }
                // Preserve the original (buggy) behaviour when the fix is off.
                return;
            }

            int sphereCheck = (this.poisonDistance + 1) * (this.poisonDistance + 1);
            if (!(entity.getDistanceSq(this.pos) < (double) sphereCheck)) {
                continue;
            }

            entity.addPotionEffect(new PotionEffect(potion, 138, 0));

            if (entity instanceof EntityCreeper && entity.getHealth() < entity.getMaxHealth() / 2.0F) {
                WorldPaleGarden.convertCreeperToPale(this.world, entity);
            }
        }
    }
}
