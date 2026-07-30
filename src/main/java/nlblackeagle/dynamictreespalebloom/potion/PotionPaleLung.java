package nlblackeagle.dynamictreespalebloom.potion;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import nlblackeagle.dynamictreespalebloom.DynamicTreesPaleBloom;
import nlblackeagle.dynamictreespalebloom.config.ForgeConfigHandler;

/**
 * Pale Lung - functions like vanilla Poison (periodic, non-lethal magic damage)
 * but ticks at a configurable fraction of Poison's speed (0.25x / 4x slower by default).
 * <p>
 * While this effect is active, {@link nlblackeagle.dynamictreespalebloom.event.PaleLungHudHandler}
 * takes over rendering of the hotbar health row so the hearts are drawn white instead of red.
 */
public class PotionPaleLung extends Potion {

    private static final ResourceLocation ICON_TEXTURE =
            new ResourceLocation(DynamicTreesPaleBloom.MODID, "textures/effects/pale_lung.png");

    public PotionPaleLung() {
        // isBadEffect = true, liquid/particle colour = a pale, sickly grey-green.
        super(true, 0xB9C2B4);
        this.setPotionName("effect.dynamictreespalebloom.pale_lung");
    }

    @Override
    public boolean isReady(int duration, int amplifier) {
        // Vanilla Poison ticks every (25 >> amplifier) ticks.
        int poisonInterval = 25 >> amplifier;
        if (poisonInterval <= 0) {
            poisonInterval = 1;
        }

        double speedMultiplier = ForgeConfigHandler.paleLung.tickSpeedMultiplier;
        if (speedMultiplier <= 0) {
            speedMultiplier = 0.25D;
        }

        int interval = Math.max(1, Math.round(poisonInterval / (float) speedMultiplier));
        return duration % interval == 0;
    }

    @Override
    public void performEffect(EntityLivingBase entity, int amplifier) {
        if (!isDamageAllowed(entity)) {
            // Still an active, ticking effect (hearts stay white, ambient particles
            // still show, etc.) - it just doesn't hurt this particular entity, per
            // the "Pale Lung Entity Damage List" config.
            return;
        }

        // Identical to vanilla Poison: damages the entity, but can never bring it below 1 HP.
        if (entity.getHealth() > 1.0F) {
            entity.attackEntityFrom(DamageSource.MAGIC, 1.0F);
        }
    }

    private boolean isDamageAllowed(EntityLivingBase entity) {
        boolean inList = PaleLungEntityMatcher.matchesAny(ForgeConfigHandler.paleLung.entityDamageList, entity);

        switch (ForgeConfigHandler.paleLung.entityDamageListMode) {
            case WHITELIST:
                return inList;
            case BLACKLIST:
            default:
                return !inList;
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderInventoryEffect(int x, int y, PotionEffect effect, Minecraft mc) {
        mc.getTextureManager().bindTexture(ICON_TEXTURE);
        GlStateManager.color(1F, 1F, 1F, 1F);
        Gui.drawModalRectWithCustomSizedTexture(x + 6, y + 7, 0, 0, 18, 18, 18F, 18F);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderHUDEffect(int x, int y, PotionEffect effect, Minecraft mc, float alpha) {
        mc.getTextureManager().bindTexture(ICON_TEXTURE);
        GlStateManager.color(1F, 1F, 1F, alpha);
        Gui.drawModalRectWithCustomSizedTexture(x + 3, y + 3, 0, 0, 18, 18, 18F, 18F);
    }
}

