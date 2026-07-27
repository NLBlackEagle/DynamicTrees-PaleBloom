package nlblackeagle.dynamictreespalebloom.event;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import nlblackeagle.dynamictreespalebloom.DynamicTreesPaleBloom;
import nlblackeagle.dynamictreespalebloom.potion.ModPotions;

/**
 * While the player has Pale Lung active, replaces the vanilla hotbar health row
 * with a version drawn from {@code textures/gui/pale_lung_hearts.png} (white hearts)
 * instead of the normal red ones.
 * <p>
 * NOTE: this is a simplified re-implementation of GuiIngame#renderHealth - it covers
 * the common case (single/multi row of hearts, shake at critical health, the 3x flash
 * on taking damage) but does not attempt to replicate every vanilla nuance (hardcore
 * hearts, absorption hearts, or shifting position for boss bars / jump bars).
 */
@SideOnly(Side.CLIENT)
public class PaleLungHudHandler {

    private static final ResourceLocation HEARTS_TEXTURE =
            new ResourceLocation(DynamicTreesPaleBloom.MODID, "textures/gui/pale_lung_hearts.png");

    private static final int ICON_SIZE = 9;
    private static final float TEX_WIDTH = 36F;
    private static final float TEX_HEIGHT = 9F;

    private static final int ICON_CONTAINER = 0;
    private static final int ICON_HALF = 1;
    private static final int ICON_FULL = 2;
    private static final int ICON_FLASH = 3;

    // Tracks the player's last-seen health so we can detect a drop and kick off the
    // damage-flash window, the same way vanilla's healthUpdateCounter/updateCounter do.
    private int lastHealth = Integer.MIN_VALUE;
    private int flashEndTick = Integer.MIN_VALUE;

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Pre event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.HEALTH) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        if (!(mc.getRenderViewEntity() instanceof EntityPlayer)) {
            return;
        }

        EntityPlayer player = (EntityPlayer) mc.getRenderViewEntity();
        if (ModPotions.paleLung == null || !player.isPotionActive(ModPotions.paleLung)) {
            return;
        }

        event.setCanceled(true);
        renderPaleHealth(mc, player, new ScaledResolution(mc));
    }

    private void renderPaleHealth(Minecraft mc, EntityPlayer player, ScaledResolution res) {
        int health = MathHelper.ceil(player.getHealth());
        boolean shake = health <= 4;

        int width = res.getScaledWidth();
        int height = res.getScaledHeight();
        int left = width / 2 - 91;
        int top = height - 39;

        int maxHealth = MathHelper.ceil(player.getMaxHealth());
        int maxHearts = MathHelper.ceil(maxHealth / 2.0F);
        int heartsPerRow = 10;

        int currentTick = player.ticksExisted;
        if (lastHealth == Integer.MIN_VALUE) {
            lastHealth = health;
        }
        if (health < lastHealth) {
            // Just took damage - flash for 20 ticks (1s), same window vanilla uses.
            flashEndTick = currentTick + 20;
        }
        lastHealth = health;

        // Toggles on/off every 3 ticks within the flash window, giving ~3 flashes total
        // over the 20-tick window - matches vanilla Poison's own heart flash cadence.
        boolean flashing = flashEndTick != Integer.MIN_VALUE
                && currentTick < flashEndTick
                && ((flashEndTick - currentTick) / 3) % 2 == 1;

        // Vanilla re-seeds a Random once per tick (not per render frame) and bounces each
        // heart up by 0-1px on Y when critical - that per-tick reseed is what makes the
        // vanilla shake read as fast/twitchy rather than a smooth wobble.
        java.util.Random shakeRand = new java.util.Random();
        shakeRand.setSeed((long) player.ticksExisted * 312871L);

        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        mc.getTextureManager().bindTexture(HEARTS_TEXTURE);
        GlStateManager.color(1F, 1F, 1F, 1F);

        for (int i = 0; i < maxHearts; i++) {
            int row = i / heartsPerRow;
            int col = i % heartsPerRow;
            int x = left + col * 8;
            int y = top - row * 10;

            if (shake) {
                y -= shakeRand.nextInt(2);
            }

            drawIcon(x, y, flashing ? ICON_FLASH : ICON_CONTAINER);

            int heartValue = health - i * 2;
            if (heartValue >= 2) {
                drawIcon(x, y, ICON_FULL);
            } else if (heartValue == 1) {
                drawIcon(x, y, ICON_HALF);
            }
        }

        // Restore vanilla's own GUI texture + a neutral draw colour so whatever renders
        // next (food, armor, air, etc.) never has a chance to inherit state left behind
        // by our custom texture/draw calls above.
        mc.getTextureManager().bindTexture(Gui.ICONS);
        GlStateManager.color(1F, 1F, 1F, 1F);
    }

    private void drawIcon(int x, int y, int index) {
        Gui.drawModalRectWithCustomSizedTexture(x, y, index * ICON_SIZE, 0, ICON_SIZE, ICON_SIZE, TEX_WIDTH, TEX_HEIGHT);
    }
}
