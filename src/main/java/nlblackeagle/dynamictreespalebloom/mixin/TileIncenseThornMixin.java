package nlblackeagle.dynamictreespalebloom.mixin;

import com.sirsquidly.palebloom.common.blocks.tileentity.TileIncenseThorn;
import com.sirsquidly.palebloom.common.world.WorldPaleGarden;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.Potion;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import nlblackeagle.dynamictreespalebloom.config.ForgeConfigHandler;
import nlblackeagle.dynamictreespalebloom.potion.ModPotions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Redirects TileIncenseThorn's stored potion from Poison to Pale Lung by intercepting
 * setPotion(Potion) directly, rather than the getPotion() getter as originally
 * attempted.
 * <p>
 * Confirmed by decompiling the shipped palebloom-1.0.0.jar with CFR:
 * TileIncenseThorn#effectNearbyEntities() - the method that actually applies the area
 * effect to nearby entities - reads the private {@code potionType} field DIRECTLY,
 * never calling {@code getPotion()} at all. A getter-only redirect (the original
 * approach) therefore never touched the real poison application - it only affected
 * external callers of {@code getPotion()}, like this addon's own
 * BlockIncenseThornMixin harvest-drop NBT preservation.
 * <p>
 * Intercepting {@code setPotion} instead means every internal use of the field (area
 * effect, particle colour via setupPotionColor, NBT save/load) sees the corrected
 * value, since the field itself now actually holds it from the moment it's set -
 * whether that's Pale Bloom's own natural-growth code calling
 * {@code setPotion(MobEffects.POISON)}, or this addon's own Seed Bomb flora scatter
 * calling {@code setPotion(...)} directly.
 */
@Mixin(value = TileIncenseThorn.class, remap = false)
public abstract class TileIncenseThornMixin extends TileEntity {

    @Shadow
    private Potion potionType;

    @Shadow
    public abstract void setPotion(Potion potion);

    @Inject(method = "setPotion", at = @At("HEAD"), cancellable = true)
    private void dynamictreespalebloom$swapPoisonForPaleLung(Potion potion, CallbackInfo ci) {
        if (!ForgeConfigHandler.featureToggles.enablePaleLung) {
            return;
        }
        if (!ForgeConfigHandler.paleLung.replacePollenheadAndIncenseThornsPoison) {
            return;
        }
        if (ModPotions.paleLung == null) {
            return;
        }

        if (potion == MobEffects.POISON) {
            this.potionType = ModPotions.paleLung;
            ci.cancel();
        }
    }

    /**
     * Decompiling the shipped palebloom-1.0.0.jar shows setPotion() is called from
     * exactly one place in the whole mod - BlockIncenseThorn#onBlockPlacedBy, and only
     * when the placed item stack has a "potion" NBT tag (i.e. was crafted via one of
     * the potion-flavour recipes). Naturally worldgen-spawned Incense Thorns never go
     * through that path at all, so their potionType field is just never assigned -
     * effectNearbyEntities() reads it directly and returns immediately when it's null,
     * meaning wild ones sit there completely inert (no aura, poison or otherwise)
     * regardless of any of this addon's settings.
     * <p>
     * Incense Thorns is meant to work like a naturally occurring hazard plant, so this
     * defaults any Incense Thorns still missing a potion to Poison the moment it first
     * tries to run its aura - routed through setPotion() above so the existing Pale
     * Lung swap (and any future logic added there) still applies on top of it.
     */
    @Inject(method = "effectNearbyEntities", at = @At("HEAD"))
    private void dynamictreespalebloom$defaultToPoisonWhenUnset(CallbackInfo ci) {
        if (!ForgeConfigHandler.miscellaneous.defaultUnsetIncenseThornsToPoison) {
            return;
        }

        if (this.potionType == null) {
            this.setPotion(MobEffects.POISON);
        }
    }

    /**
     * WorldPaleGarden.isNight(World) (confirmed via decompile) is purely a world-time
     * check - (world.getWorldTime() % 24000) against a fixed range - with no concept
     * of light level or sky visibility at all. TileIncenseThorn's own tick method
     * assigns isAwake straight from that single call, so a thorn planted in a cave
     * never runs its aura during the day no matter how dark it actually is down there.
     * <p>
     * This redirects that one call (isAwake's assignment, inside the tick method) to
     * also treat "no sky light reaches this position" as night, when the "Awaken
     * Underground During Day" toggle is on - the same simple canSeeSky check used
     * elsewhere in vanilla for "is this actually underground".
     */
    @Redirect(method = {"update", "func_73660_a"}, at = @At(value = "INVOKE", target = "Lcom/sirsquidly/palebloom/common/world/WorldPaleGarden;isNight(Lnet/minecraft/world/World;)Z"))
    private boolean dynamictreespalebloom$isNightOrUndergroundDay(World world) {
        if (WorldPaleGarden.isNight(world)) {
            return true;
        }
        if (!ForgeConfigHandler.paleLung.awakenUndergroundDuringDay) {
            return false;
        }
        return !world.canSeeSky(this.pos);
    }
}
