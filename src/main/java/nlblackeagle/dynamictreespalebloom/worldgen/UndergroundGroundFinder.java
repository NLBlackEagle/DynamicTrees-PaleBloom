package nlblackeagle.dynamictreespalebloom.worldgen;

import com.ferreusveritas.dynamictrees.worldgen.WorldGeneratorTrees;
import net.minecraft.util.math.BlockPos;

// WorldGeneratorTrees.GroundFinder's cave-pocket scan (findSubterraneanGround
// -> findSubterraneanLayerHeights) is hardcoded to y 0-128 via its protected
// inNetherRange(BlockPos) check - there's no config or parameter for it, since
// DT only ever uses it for the Nether. Overriding that one check here (all the
// scanning logic that calls it is protected, non-final, and calls it virtually
// via "this") narrows the underground tree spawner's search range to y 0-70
// without needing to reimplement any of the actual cave-finding logic.
public class UndergroundGroundFinder extends WorldGeneratorTrees.GroundFinder {

    private static final int MAX_Y = 70;

    @Override
    protected boolean inNetherRange(BlockPos pos) {
        return pos.getY() >= 0 && pos.getY() <= MAX_Y;
    }
}
