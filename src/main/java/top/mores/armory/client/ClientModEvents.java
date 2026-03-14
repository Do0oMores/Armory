package top.mores.armory.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;
import top.mores.armory.Armory;
import top.mores.armory.client.ui.LoadoutScreen;

@Mod.EventBusSubscriber(modid = Armory.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
final class ClientKeyRegister {
    static final KeyMapping OPEN_LOADOUT = new KeyMapping(
            "key.taczloadout.open",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_O,
            "key.categories.taczloadout"
    );

    @SubscribeEvent
    public static void onRegisterKey(RegisterKeyMappingsEvent event) {
        event.register(OPEN_LOADOUT);
    }
}

@Mod.EventBusSubscriber(modid = Armory.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ClientModEvents {
    private ClientModEvents() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        while (ClientKeyRegister.OPEN_LOADOUT.consumeClick()) {
            mc.setScreen(new LoadoutScreen());
        }
    }
}
