package top.mores.armory.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public final class PlayerUnlockStore {
    private static final String ROOT_KEY = "armory_data";

    private PlayerUnlockStore() {
    }

    public static PlayerUnlockData get(ServerPlayer player) {
        CompoundTag persisted = getPersistedRoot(player);
        if (!persisted.contains(ROOT_KEY, Tag.TAG_COMPOUND)) {
            PlayerUnlockData fresh = new PlayerUnlockData();
            fresh.setCurrentPreset(ArmoryPreset.empty());
            save(player, fresh);
            return fresh;
        }
        return PlayerUnlockData.fromTag(persisted.getCompound(ROOT_KEY));
    }

    public static void save(ServerPlayer player, PlayerUnlockData data) {
        CompoundTag persisted = getPersistedRoot(player);
        persisted.put(ROOT_KEY, data.toTag());
        player.getPersistentData().put(Player.PERSISTED_NBT_TAG, persisted);
    }

    private static CompoundTag getPersistedRoot(ServerPlayer player) {
        CompoundTag root = player.getPersistentData();
        if (!root.contains(Player.PERSISTED_NBT_TAG, Tag.TAG_COMPOUND)) {
            root.put(Player.PERSISTED_NBT_TAG, new CompoundTag());
        }
        return root.getCompound(Player.PERSISTED_NBT_TAG);
    }
}
