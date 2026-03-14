package top.mores.armory.service;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.mores.armory.Armory;
import top.mores.armory.command.LoadoutCommands;
import top.mores.armory.config.LoadoutCatalogConfigLoader;

@Mod.EventBusSubscriber(modid = Armory.MODID)
public final class CommonEvents {
    private CommonEvents() {
    }

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        LoadoutCatalogConfigLoader.reload();
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        LoadoutCommands.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer sp) {
            LoadoutSyncService.syncAll(sp);
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer sp) {
            LoadoutSyncService.syncAll(sp);
        }
    }

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        if (!(event.getOriginal() instanceof net.minecraft.server.level.ServerPlayer oldPlayer)) return;
        if (!(event.getEntity() instanceof net.minecraft.server.level.ServerPlayer newPlayer)) return;

        CompoundTag oldRoot = oldPlayer.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        newPlayer.getPersistentData().put(Player.PERSISTED_NBT_TAG, oldRoot.copy());
    }
}