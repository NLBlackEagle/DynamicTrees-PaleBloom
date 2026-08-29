package nlblackeagle.dynamictreespalebloom.mixin;

import com.sirsquidly.palebloom.compat.CompatJEI;
import nlblackeagle.dynamictreespalebloom.config.ForgeConfigHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * CompatJEI#addInformation(IModRegistry) registers exactly four JEI info-board
 * descriptions, each as a separate hardcoded String literal passed to
 * registry.addIngredientInfo(...): "jei.palebloom.incense_thorns.desc",
 * "jei.palebloom.pale_oak_hollow.desc", "jei.palebloom.pollenhead.desc", and
 * "jei.palebloom.resin_bulb.desc" (confirmed directly from decompiling the shipped
 * palebloom-1.0.0.jar - this is the complete list, nothing else in the jar calls
 * addIngredientInfo).
 * <p>
 * When "Enable Dregora Lang Keys" is on, each of these four gets prefixed with
 * "dregora." before JEI ever looks it up, so this addon's own lang file can own that
 * text entirely via "dregora.<original key>" entries instead of needing to keep
 * shadowing Pale Bloom's exact original key names.
 */
@Mixin(CompatJEI.class)
public class CompatJEIMixin {

    @ModifyConstant(method = "addInformation", constant = @Constant(stringValue = "jei.palebloom.incense_thorns.desc"), remap = false)
    private String dynamictreespalebloom$dregoraIncenseThorns(String original) {
        return dregoraKey(original);
    }

    @ModifyConstant(method = "addInformation", constant = @Constant(stringValue = "jei.palebloom.pale_oak_hollow.desc"), remap = false)
    private String dynamictreespalebloom$dregoraPaleOakHollow(String original) {
        return dregoraKey(original);
    }

    @ModifyConstant(method = "addInformation", constant = @Constant(stringValue = "jei.palebloom.pollenhead.desc"), remap = false)
    private String dynamictreespalebloom$dregoraPollenhead(String original) {
        return dregoraKey(original);
    }

    @ModifyConstant(method = "addInformation", constant = @Constant(stringValue = "jei.palebloom.resin_bulb.desc"), remap = false)
    private String dynamictreespalebloom$dregoraResinBulb(String original) {
        return dregoraKey(original);
    }

    private static String dregoraKey(String original) {
        if (!ForgeConfigHandler.featureToggles.enableRLCraftDregora) {
            return original;
        }
        if (!ForgeConfigHandler.rlcraftDregora.enableDregoraLangKeys) {
            return original;
        }
        return "dregora." + original;
    }
}
