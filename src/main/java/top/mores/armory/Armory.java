package top.mores.armory;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import top.mores.armory.net.ArmoryNet;

@Mod(Armory.MODID)
public class Armory {
    public static final String MODID = "armory";

    public Armory() {
        ArmoryNet.register();
        MinecraftForge.EVENT_BUS.register(this);
    }
}
