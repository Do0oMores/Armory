package top.mores.armory.bridge;

import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IAttachment;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.attachment.AttachmentType;
import com.tacz.guns.api.item.builder.AttachmentItemBuilder;
import com.tacz.guns.api.item.builder.GunItemBuilder;
import com.tacz.guns.api.item.gun.FireMode;
import com.tacz.guns.resource.index.CommonGunIndex;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import top.mores.armory.catalog.LoadoutCatalog;
import top.mores.armory.data.ArmoryPreset;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

public final class TaczBridge {
    private TaczBridge() {
    }

    public static boolean isGun(ItemStack stack) {
        return IGun.getIGunOrNull(stack) != null;
    }

    public static boolean isAttachment(ItemStack stack) {
        return IAttachment.getIAttachmentOrNull(stack) != null;
    }

    @Nullable
    public static IGun getGun(ItemStack stack) {
        return IGun.getIGunOrNull(stack);
    }

    public static boolean canInstall(ItemStack gunStack, AttachmentType expectedType, ItemStack attachmentStack) {
        IGun iGun = IGun.getIGunOrNull(gunStack);
        IAttachment iAttachment = IAttachment.getIAttachmentOrNull(attachmentStack);
        if (iGun == null || iAttachment == null) {
            return false;
        }
        if (iAttachment.getType(attachmentStack) != expectedType) {
            return false;
        }
        if (!iGun.allowAttachmentType(gunStack, expectedType)) {
            return false;
        }
        return iGun.allowAttachment(gunStack, attachmentStack);
    }

    public static void setAttachmentLock(ItemStack gunStack, boolean locked) {
        IGun iGun = IGun.getIGunOrNull(gunStack);
        if (iGun != null) {
            iGun.setAttachmentLock(gunStack, locked);
        }
    }

    public static ItemStack buildPreviewGun(ArmoryPreset preset) {
        if (preset.gunKey() == null) return ItemStack.EMPTY;

        LoadoutCatalog.GunOption gunOption = LoadoutCatalog.getGun(preset.gunKey()).orElse(null);
        if (gunOption == null) return ItemStack.EMPTY;

        ResourceLocation gunId = gunOption.gunId();
        FireMode initialFireMode = resolveInitialFireMode(gunId);
        int baseAmmo = resolveBaseAmmo(gunId);

        ItemStack gun = GunItemBuilder.create()
                .setId(gunId)
                .setFireMode(initialFireMode)
                .setAmmoCount(baseAmmo)
                .setAmmoInBarrel(false)
                .build();

        if (gun.isEmpty()) return ItemStack.EMPTY;

        IGun iGun = IGun.getIGunOrNull(gun);
        if (iGun == null) return ItemStack.EMPTY;

        for (Map.Entry<AttachmentType, ResourceLocation> e : preset.attachments().entrySet()) {
            LoadoutCatalog.AttachmentOption option = LoadoutCatalog.getAttachment(e.getValue()).orElse(null);
            if (option == null) continue;

            ItemStack attachment = AttachmentItemBuilder.create()
                    .setId(option.attachmentId())
                    .build();

            if (attachment.isEmpty()) continue;

            if (canInstall(gun, e.getKey(), attachment)) {
                iGun.installAttachment(gun, attachment);
            }
        }

        int finalAmmo = resolveFinalMagazineCapacity(preset, baseAmmo);
        iGun.setCurrentAmmoCount(gun, finalAmmo);

        return gun;
    }

    public static boolean previewCanUse(ArmoryPreset preset, AttachmentType slotType, ResourceLocation attachmentKey) {
        ItemStack gun = buildPreviewGun(removeSlot(preset, slotType));
        if (gun.isEmpty()) return false;

        LoadoutCatalog.AttachmentOption option = LoadoutCatalog.getAttachment(attachmentKey).orElse(null);
        if (option == null) return false;

        ItemStack attachment = AttachmentItemBuilder.create()
                .setId(option.attachmentId())
                .build();

        return !attachment.isEmpty() && canInstall(gun, slotType, attachment);
    }

    public static ItemStack buildFinalGun(ArmoryPreset preset, boolean lockEditing) {
        ItemStack gun = buildPreviewGun(preset);
        if (!gun.isEmpty() && lockEditing) {
            setAttachmentLock(gun, true);
        }
        return gun;
    }

    private static FireMode resolveInitialFireMode(ResourceLocation gunId) {
        CommonGunIndex index = TimelessAPI.getCommonGunIndex(gunId).orElse(null);
        if (index == null) {
            return FireMode.SEMI;
        }

        Set<FireMode> modes = (Set<FireMode>) index.getGunData().getFireModeSet();
        if (modes == null || modes.isEmpty()) {
            return FireMode.SEMI;
        }

        if (modes.contains(FireMode.SEMI)) return FireMode.SEMI;
        if (modes.contains(FireMode.AUTO)) return FireMode.AUTO;
        if (modes.contains(FireMode.BURST)) return FireMode.BURST;

        return modes.iterator().next();
    }

    private static int resolveBaseAmmo(ResourceLocation gunId) {
        CommonGunIndex index = TimelessAPI.getCommonGunIndex(gunId).orElse(null);
        if (index == null) {
            return 0;
        }
        return Math.max(index.getGunData().getAmmoAmount(), 0);
    }

    private static int resolveFinalMagazineCapacity(ArmoryPreset preset, int baseAmmo) {
        LoadoutCatalog.AttachmentOption extMag = findExtendedMagOption(preset);
        if (extMag == null) {
            return baseAmmo;
        }

        if (extMag.magCapacityOverride() != null) {
            return Math.max(extMag.magCapacityOverride(), 0);
        }

        if (extMag.magBonus() != null) {
            return Math.max(baseAmmo + extMag.magBonus(), 0);
        }

        // 没配额外规则时回退到基础容量
        return baseAmmo;
    }

    @Nullable
    private static LoadoutCatalog.AttachmentOption findExtendedMagOption(ArmoryPreset preset) {
        for (Map.Entry<AttachmentType, ResourceLocation> e : preset.attachments().entrySet()) {
            if (e.getKey() != AttachmentType.EXTENDED_MAG) continue;
            return LoadoutCatalog.getAttachment(e.getValue()).orElse(null);
        }
        return null;
    }

    private static ArmoryPreset removeSlot(ArmoryPreset preset, AttachmentType slotType) {
        EnumMap<AttachmentType, ResourceLocation> map = new EnumMap<>(preset.attachments());
        map.remove(slotType);
        return new ArmoryPreset(preset.gunKey(), map);
    }
}