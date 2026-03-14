package top.mores.armory.catalog;

import com.tacz.guns.api.item.attachment.AttachmentType;
import com.tacz.guns.api.item.builder.AttachmentItemBuilder;
import com.tacz.guns.api.item.builder.GunItemBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.*;

public final class LoadoutCatalog {
    public record GunOption(ResourceLocation key, String displayName, ResourceLocation gunId) {
        public Component name() {
            return Component.literal(displayName);
        }

        public ItemStack createStack() {
            return GunItemBuilder.create()
                    .setId(gunId)
                    .build();
        }
    }

    public record AttachmentOption(ResourceLocation key,
                                   String displayName,
                                   AttachmentType type,
                                   ResourceLocation attachmentId,
                                   Integer magBonus,
                                   Integer magCapacityOverride) {
        public Component name() {
            return Component.literal(displayName);
        }

        public ItemStack createStack() {
            return AttachmentItemBuilder.create()
                    .setId(attachmentId)
                    .build();
        }
    }

    public record CatalogSnapshot(List<GunOption> guns, List<AttachmentOption> attachments) {}

    private static final Map<ResourceLocation, GunOption> GUNS = new LinkedHashMap<>();
    private static final Map<ResourceLocation, AttachmentOption> ATTACHMENTS = new LinkedHashMap<>();

    private LoadoutCatalog() {
    }

    public static synchronized void replaceAll(Collection<GunOption> guns,
                                               Collection<AttachmentOption> attachments) {
        GUNS.clear();
        ATTACHMENTS.clear();

        for (GunOption gun : guns) {
            GUNS.put(gun.key(), gun);
        }
        for (AttachmentOption attachment : attachments) {
            ATTACHMENTS.put(attachment.key(), attachment);
        }
    }

    public static synchronized Optional<GunOption> getGun(ResourceLocation key) {
        return Optional.ofNullable(GUNS.get(key));
    }

    public static synchronized Optional<AttachmentOption> getAttachment(ResourceLocation key) {
        return Optional.ofNullable(ATTACHMENTS.get(key));
    }

    public static synchronized List<GunOption> guns() {
        return List.copyOf(GUNS.values());
    }

    public static synchronized List<AttachmentOption> attachments() {
        return List.copyOf(ATTACHMENTS.values());
    }

    public static synchronized CatalogSnapshot snapshot() {
        return new CatalogSnapshot(guns(), attachments());
    }

    public static synchronized void applySnapshot(CatalogSnapshot snapshot) {
        replaceAll(snapshot.guns(), snapshot.attachments());
    }
}