package maxhyper.dynamictreesdefiledlands;

import fermiumbooter.FermiumRegistryAPI;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.spongepowered.asm.launch.MixinBootstrap;

import java.util.Map;

@IFMLLoadingPlugin.MCVersion("1.12.2")
public class DynamicTreesPaleBloomPlugin implements IFMLLoadingPlugin {

    public DynamicTreesPaleBloomPlugin() {
        MixinBootstrap.init();
        FermiumRegistryAPI.enqueueMixin(false, "mixins.dynamictreesdefiledlands.palebloom.json");
    }

    public String[] getASMTransformerClass() { return new String[0]; }
    public String getModContainerClass() { return null; }
    public String getSetupClass() { return null; }
    public void injectData(Map<String, Object> data) { }
    public String getAccessTransformerClass() { return null; }
}