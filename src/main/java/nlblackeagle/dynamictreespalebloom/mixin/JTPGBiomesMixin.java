package nlblackeagle.dynamictreespalebloom.mixin;

import com.sirsquidly.palebloom.init.JTPGBiomes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(value = JTPGBiomes.class)
public class JTPGBiomesMixin {

    @ModifyConstant(method = "registerBiomes", constant = @Constant(intValue = 0, ordinal = 0))
    private static int dynamictreespalebloom$increasePaleGardenWeight(int original) {
        return 10;
    }
}