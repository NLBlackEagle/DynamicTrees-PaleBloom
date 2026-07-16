package nlblackeagle.dynamictreespalebloom.mixin;

import com.ferreusveritas.dynamictrees.worldgen.BiomeDataBase;
import com.ferreusveritas.dynamictrees.worldgen.TreeGenerator;
import com.ferreusveritas.dynamictrees.systems.poissondisc.PoissonDisc;
import com.ferreusveritas.dynamictrees.api.worldgen.IGroundFinder;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;
import com.sirsquidly.palebloom.init.JTPGBiomes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = TreeGenerator.class, remap = false)
public class TreeGeneratorDebugMixin {

    @Inject(method = "makeTree", at = @At("RETURN"))
    private void dynamictreespalebloom$logMakeTree(World world, BiomeDataBase biomeDataBase, PoissonDisc circle, IGroundFinder groundFinder, SafeChunkBounds safeBounds, CallbackInfoReturnable<TreeGenerator.EnumGeneratorResult> cir) {
        Biome biome = world.getBiome(new BlockPos(circle.x, 0, circle.z));
        if (biome == JTPGBiomes.PALE_GARDEN) {
            System.out.println("[PaleBloomDT DEBUG] makeTree result in Pale Garden: " + cir.getReturnValue() + " at " + circle.x + "," + circle.z);
        }
    }
}