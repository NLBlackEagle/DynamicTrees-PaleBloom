package nlblackeagle.dynamictreespalebloom.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Random;

// Targets vanilla BlockLeaves - must apply early, before that class loads.
// Must NOT statically reference ANY other mod's classes (DynamicTrees' or
// Pale Bloom's own) in this mixin's own bytecode - neither mod's jar is
// guaranteed to be on the classpath yet at this early point (that only
// happens once FML's mod-discovery phase runs, which is later). Every
// cross-mod touchpoint below goes through reflection instead, deferring
// resolution to actual method execution time, well after all mods are loaded.
@Mixin(BlockLeaves.class)
public class BlockLeavesMixin {

    @Inject(method = "randomDisplayTick", at = @At("HEAD"))
    private void dynamictreespalebloom$fallingLeafParticle(IBlockState stateIn, World worldIn, BlockPos pos, Random rand, CallbackInfo ci) {
        Block self = (Block) (Object) this;

        if (!self.getClass().getName().equals("com.ferreusveritas.dynamictrees.blocks.BlockDynamicLeaves")) return;

        int leafType;
        int spawnChance;

        try {
            Method getProperties = self.getClass().getMethod("getProperties", IBlockState.class);
            Object properties = getProperties.invoke(self, stateIn);

            Class<?> modContentClass = Class.forName("nlblackeagle.dynamictreespalebloom.ModContent");

            // Matches the real leafType/spawnChance values from each species'
            // actual BlockPaleLeaves constructor in Pale Bloom itself.
            if (properties == modContentClass.getField("paleOakLeavesProperties").get(null)) {
                leafType = 0;
                spawnChance = 64;
            } else if (properties == modContentClass.getField("paleBloomingOakLeavesProperties").get(null)) {
                leafType = 1;
                spawnChance = 64;
            } else if (properties == modContentClass.getField("paleBirchLeavesProperties").get(null)) {
                leafType = 2;
                spawnChance = 128;
            } else {
                return;
            }
        } catch (ReflectiveOperationException e) {
            return;
        }

        if (rand.nextInt(spawnChance) == 0 && !worldIn.getBlockState(pos.down()).getMaterial().blocksMovement()) {
            try {
                Class<?> paleBloomClass = Class.forName("com.sirsquidly.palebloom.paleBloom");
                Field proxyField = paleBloomClass.getField("proxy");
                Object proxy = proxyField.get(null);

                Method spawnParticle = proxy.getClass().getMethod("spawnParticle",
                        int.class, World.class, double.class, double.class, double.class,
                        double.class, double.class, double.class, int[].class);

                spawnParticle.invoke(proxy, 1, worldIn,
                        pos.getX() + 0.5 + (worldIn.rand.nextDouble() - 0.5), (double) pos.getY(),
                        pos.getZ() + 0.5 + (worldIn.rand.nextDouble() - 0.5), 0.0, 0.0, 0.0, new int[]{leafType});
            } catch (ReflectiveOperationException ignored) {
            }
        }
    }
}
