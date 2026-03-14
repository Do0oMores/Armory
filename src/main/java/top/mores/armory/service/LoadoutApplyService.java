package top.mores.armory.service;

import com.tacz.guns.api.item.attachment.AttachmentType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import top.mores.armory.bridge.TaczBridge;
import top.mores.armory.catalog.LoadoutCatalog;
import top.mores.armory.data.ArmoryPreset;
import top.mores.armory.data.PlayerUnlockData;
import top.mores.armory.data.PlayerUnlockStore;

public final class LoadoutApplyService {
    private LoadoutApplyService() {
    }

    public static void apply(ServerPlayer player, ArmoryPreset preset) {
        PlayerUnlockData data = PlayerUnlockStore.get(player);

        if (preset.gunKey() == null) {
            player.sendSystemMessage(Component.literal("未选择主武器"));
            return;
        }

        if (!data.unlockedGuns().contains(preset.gunKey())) {
            player.sendSystemMessage(Component.literal("该武器尚未解锁"));
            return;
        }

        LoadoutCatalog.GunOption gunOption = LoadoutCatalog.getGun(preset.gunKey()).orElse(null);
        if (gunOption == null) {
            player.sendSystemMessage(Component.literal("武器目录不存在: " + preset.gunKey()));
            return;
        }

        for (var entry : preset.attachments().entrySet()) {
            AttachmentType slotType = entry.getKey();
            var attachmentKey = entry.getValue();

            if (!data.unlockedAttachments().contains(attachmentKey)) {
                player.sendSystemMessage(Component.literal("配件尚未解锁: " + attachmentKey));
                return;
            }

            LoadoutCatalog.AttachmentOption option = LoadoutCatalog.getAttachment(attachmentKey).orElse(null);
            if (option == null) {
                player.sendSystemMessage(Component.literal("配件目录不存在: " + attachmentKey));
                return;
            }

            if (option.type() != slotType) {
                player.sendSystemMessage(Component.literal("配件槽位不匹配: " + attachmentKey));
                return;
            }
        }

        ItemStack finalGun = TaczBridge.buildFinalGun(preset, true);
        if (finalGun.isEmpty() || !TaczBridge.isGun(finalGun)) {
            player.sendSystemMessage(Component.literal("构建最终枪械失败，请检查目录注册"));
            return;
        }

        player.getInventory().setItem(player.getInventory().selected, finalGun);
        data.setCurrentPreset(preset);
        PlayerUnlockStore.save(player, data);
        LoadoutSyncService.syncAll(player);

        player.sendSystemMessage(Component.literal("配置已应用"));
    }
}
