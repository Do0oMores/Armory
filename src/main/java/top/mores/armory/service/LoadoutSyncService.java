package top.mores.armory.service;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import top.mores.armory.catalog.LoadoutCatalog;
import top.mores.armory.data.PlayerUnlockData;
import top.mores.armory.data.PlayerUnlockStore;
import top.mores.armory.net.S2CCatalogSyncPacket;
import top.mores.armory.net.S2CSnapshotPacket;
import top.mores.armory.net.ArmoryNet;

public final class LoadoutSyncService {
    private LoadoutSyncService() {
    }

    public static void syncCatalog(ServerPlayer player) {
        ArmoryNet.sendTo(player, new S2CCatalogSyncPacket(LoadoutCatalog.snapshot()));
    }

    public static void syncSnapshot(ServerPlayer player) {
        PlayerUnlockData data = PlayerUnlockStore.get(player);
        ArmoryNet.sendTo(player, new S2CSnapshotPacket(
                data.unlockedGuns(),
                data.unlockedAttachments(),
                data.getCurrentPreset()
        ));
    }

    public static void syncAll(ServerPlayer player) {
        syncCatalog(player);
        syncSnapshot(player);
    }

    public static void syncAllPlayers(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            syncAll(player);
        }
    }
}
