package nlblackeagle.dynamictreespalebloom.mixin;

import com.ferreusveritas.dynamictrees.items.Seed;
import com.ferreusveritas.dynamictrees.trees.Species;
import net.minecraft.item.ItemStack;
import nlblackeagle.dynamictreespalebloom.config.ForgeConfigHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;
import java.util.List;

// Part of the "Enable Underground Growth" RLCraft Dregora option. DT's
// Seed.shouldPlant() requires world.canBlockSeeSky(pos) before a tree can
// even begin planting, UNLESS hasForcePlant() returns true for that seed
// stack (a real, existing DT mechanic - normally only true when a seed
// item's NBT explicitly carries a "forceplant" flag, intended for
// admin/creative tools). Rather than overriding shouldPlant itself (which
// has more logic to replicate correctly), this targets the much smaller,
// self-contained hasForcePlant check directly, making it return true when
// the config option is enabled AND the seed belongs to one of our four
// target families - our three Pale Bloom species, plus vanilla Dark Oak
// specifically (not every Dynamic Trees species globally).
@Mixin(value = Seed.class, remap = false)
public class SeedMixin {

    private static final List<String> TARGET_FAMILIES = Arrays.asList(
            "dynamictreespalebloom:pale_oak",
            "dynamictreespalebloom:pale_blooming",
            "dynamictreespalebloom:pale_birch",
            "dynamictrees:darkoak"
    );

    @Inject(method = "hasForcePlant", at = @At("HEAD"), cancellable = true)
    private void dynamictreespalebloom$allowUndergroundPlanting(ItemStack seedStack, CallbackInfoReturnable<Boolean> cir) {
        if (!ForgeConfigHandler.rlcraftDregora.enableUndergroundGrowth) return;

        Seed self = (Seed) (Object) this;
        Species species = self.getSpecies(seedStack);
        if (species == null) return;

        if (TARGET_FAMILIES.contains(species.getFamily().getName().toString())) {
            cir.setReturnValue(true);
        }
    }
}
