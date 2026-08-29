package nlblackeagle.dynamictreespalebloom.mixin;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.sirsquidly.palebloom.common.blocks.BlockCreakingHeart;
import com.sirsquidly.palebloom.common.blocks.tileentity.TileCreakingHeart;
import com.sirsquidly.palebloom.common.world.WorldPaleGarden;
import com.sirsquidly.palebloom.config.ConfigCache;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import nlblackeagle.dynamictreespalebloom.blocks.BlockBranchCreakingHeart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TileCreakingHeart.class)
public class TileCreakingHeartMixin {

    @Inject(method = "preformHitReact", at = @At("HEAD"), cancellable = true, remap = false)
    private void dynamictreespalebloom$handleHitReactBranchVariant(net.minecraft.util.DamageSource source, CallbackInfo ci) {
        TileCreakingHeart self = (TileCreakingHeart) (Object) this;
        World world = self.getWorld();
        if (world == null) return;
        BlockPos pos = self.getPos();
        IBlockState state = world.getBlockState(pos);

        if (!(state.getBlock() instanceof BlockBranchCreakingHeart)) return; // real heart - let vanilla logic run

        world.playSound(null, pos, com.sirsquidly.palebloom.init.JTPGSounds.BLOCK_CREAKING_HEART_TRAIL,
                net.minecraft.util.SoundCategory.BLOCKS, 1.0F, 1.0F);

        if (!world.isRemote && WorldPaleGarden.isNight(world) && source.getTrueSource() instanceof net.minecraft.entity.player.EntityPlayer
                && self.ticksExisted >= self.lastCreakingHurtTime) {
            self.lastCreakingHurtTime = self.ticksExisted + self.resinPlacementCooldown;

            boolean natural = state.getValue(BlockCreakingHeart.NATURAL);
            if ((natural && !ConfigCache.crkHrt_genResinNatural) || (!natural && !ConfigCache.crkHrt_genResinUnnatural)) {
                ci.cancel();
                return;
            }

            dynamictreespalebloom$tryPlaceResinForBranch(world, pos, world.rand);
        }

        ci.cancel();
    }

    private static void dynamictreespalebloom$tryPlaceResinForBranch(World world, BlockPos pos, java.util.Random rand) {
        int resinRange = 3;
        int placements = rand.nextInt(2) + 2;
        java.util.List<BlockPos> goodPositions = new java.util.ArrayList<>();

        for (BlockPos checkPos : BlockPos.getAllInBoxMutable(pos.add(-resinRange, -resinRange, -resinRange), pos.add(resinRange, resinRange, resinRange))) {
            if (Math.abs(checkPos.getX() - pos.getX()) + Math.abs(checkPos.getY() - pos.getY()) + Math.abs(checkPos.getZ() - pos.getZ()) <= resinRange) {
                goodPositions.add(new BlockPos(checkPos));
            }
        }

        java.util.Collections.shuffle(goodPositions, rand);

        for (BlockPos currentPos : goodPositions) {
            for (net.minecraft.util.EnumFacing facing : net.minecraft.util.EnumFacing.values()) {
                BlockPos outerPos = currentPos.offset(facing);
                IBlockState adjacentState = world.getBlockState(outerPos);
                net.minecraft.block.Block adjBlock = adjacentState.getBlock();

                boolean isOurTrunk = adjBlock == nlblackeagle.dynamictreespalebloom.ModContent.paleOakBranchBlock
                        || (nlblackeagle.dynamictreespalebloom.ModContent.paleOakBranchBlock instanceof com.ferreusveritas.dynamictrees.blocks.BlockBranchThick
                        && adjBlock == ((com.ferreusveritas.dynamictrees.blocks.BlockBranchThick) nlblackeagle.dynamictreespalebloom.ModContent.paleOakBranchBlock).otherBlock)
                        || adjBlock == nlblackeagle.dynamictreespalebloom.ModContent.paleOakBranchCreakingHeart;

                if (isOurTrunk) {
                    IBlockState targetState = world.getBlockState(currentPos);
                    if (targetState.getBlock().isAir(targetState, world, currentPos)) {
                        world.setBlockState(currentPos, com.sirsquidly.palebloom.common.blocks.BlockResinClump.getBlockState(new net.minecraft.util.EnumFacing[]{facing}));
                        world.playSound(null, pos, com.sirsquidly.palebloom.init.JTPGSounds.BLOCK_RESIN_PLACE, net.minecraft.util.SoundCategory.BLOCKS, 1.0F, 1.0F);
                        placements--;
                    } else if (targetState.getBlock() instanceof com.sirsquidly.palebloom.common.blocks.BlockResinClump) {
                        net.minecraft.util.EnumFacing[] existingFacings = com.sirsquidly.palebloom.common.blocks.BlockResinClump.getFacings(targetState);
                        if (!org.apache.commons.lang3.ArrayUtils.contains(existingFacings, facing)) {
                            world.setBlockState(currentPos, com.sirsquidly.palebloom.common.blocks.BlockResinClump.getBlockState((net.minecraft.util.EnumFacing[]) org.apache.commons.lang3.ArrayUtils.add(existingFacings, facing)));
                            world.playSound(null, pos, com.sirsquidly.palebloom.init.JTPGSounds.BLOCK_RESIN_PLACE, net.minecraft.util.SoundCategory.BLOCKS, 1.0F, 1.0F);
                            placements--;
                        }
                    }
                    if (placements <= 0) return;
                }
            }
        }
    }

    @Inject(method = {"update", "func_73660_a"}, at = @At("HEAD"), cancellable = true)
    private void dynamictreespalebloom$handleBranchVariant(CallbackInfo ci) {
        TileCreakingHeart self = (TileCreakingHeart) (Object) this;
        World world = self.getWorld();
        if (world == null) return;

        BlockPos pos = self.getPos();
        IBlockState state = world.getBlockState(pos);

        if (!(state.getBlock() instanceof BlockBranchCreakingHeart)) return; // real heart - let vanilla logic run untouched

        // Particles/comparator are cosmetic and fine to run on both sides.
        if (self.ticksExisted++ < self.lastCreakingHurtTime) {
            self.spawnTrailParticles(3);
        }

        if (world.getTotalWorldTime() % 5L == 0L && !world.isRemote) {
            int comparatorSignal = self.getCreaking() != null
                    ? (int) (15 - Math.floor(self.getCreakingDistance() / 32 * 15)) : 0;
            if (comparatorSignal != self.getComparatorOutput()) {
                self.setComparatorOutput(comparatorSignal);
                world.updateComparatorOutputLevel(pos, world.getBlockState(pos).getBlock());
            }
        }

        // Everything below decides/reads the authoritative heart state - server
        // only, so the client never computes its own answer from a possibly-stale
        // local view of the tree and causes state to flicker against the server.
        if (world.getTotalWorldTime() % 20L == 0L && !world.isRemote) {
            BlockCreakingHeart.EnumHeartState newState = dynamictreespalebloom$getCurrentHeartState(world, pos, state);

            if (state.getValue(BlockCreakingHeart.HEART_STATE) != newState
                    && (newState != BlockCreakingHeart.EnumHeartState.UPROOTED || self.getCreakingUUID() == null)) {
                world.setBlockState(pos, state.withProperty(BlockCreakingHeart.HEART_STATE, newState), 2);

                if (newState == BlockCreakingHeart.EnumHeartState.UPROOTED) {
                    ci.cancel();
                    return;
                }
            }

            if (!ConfigCache.crk_enabled) {
                ci.cancel();
                return;
            }

            if (self.getCreakingUUID() == null) {
                if (newState == BlockCreakingHeart.EnumHeartState.AWAKE) self.summonCreaking(8);
            } else {
                self.checkCreaking(WorldPaleGarden.isNight(world));
            }
        }

        ci.cancel();
    }

    private static BlockCreakingHeart.EnumHeartState dynamictreespalebloom$getCurrentHeartState(World world, BlockPos pos, IBlockState state) {
        boolean supported = TreeHelper.isBranch(world.getBlockState(pos.up()))
                && TreeHelper.isBranch(world.getBlockState(pos.down()));

        if (supported) {
            return WorldPaleGarden.isNight(world)
                    ? BlockCreakingHeart.EnumHeartState.AWAKE
                    : BlockCreakingHeart.EnumHeartState.DORMANT;
        }
        return BlockCreakingHeart.EnumHeartState.UPROOTED;
    }
}