package nlblackeagle.dynamictreespalebloom.mixin;

import com.sirsquidly.palebloom.common.blocks.BlockSuckerRoots;
import net.minecraft.entity.Entity;
import net.minecraft.util.DamageSource;
import nlblackeagle.dynamictreespalebloom.config.ForgeConfigHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * BlockSuckerRoots#onEntityWalk (confirmed by decompiling the shipped
 * palebloom-1.0.0.jar) does two separate things to entities standing in it: slows
 * their movement, and - only for the taller 2-layer variant, once per second - damages
 * them via a single {@code entityIn.attackEntityFrom(DamageSource.CACTUS, 1.0F)} call.
 * <p>
 * "Disable Sucker Roots Damage" redirects just that one call to a no-op when enabled,
 * leaving the movement slowdown completely untouched either way - this only ever
 * affects the damage, never whether entities get slowed down walking through it.
 */
@Mixin(value = BlockSuckerRoots.class, remap = false)
public class BlockSuckerRootsMixin {

    @Redirect(method = "func_180634_a", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;func_70097_a(Lnet/minecraft/util/DamageSource;F)Z"))
    private boolean dynamictreespalebloom$maybeSkipDamage(Entity entity, DamageSource source, float amount) {
        if (ForgeConfigHandler.miscellaneous.disableSuckerRootsDamage) {
            return false;
        }
        return entity.attackEntityFrom(source, amount);
    }
}
