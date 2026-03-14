package top.mores.armory.net;

import com.tacz.guns.api.item.attachment.AttachmentType;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import top.mores.armory.catalog.LoadoutCatalog;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public record S2CCatalogSyncPacket(LoadoutCatalog.CatalogSnapshot snapshot) {
    public static void encode(S2CCatalogSyncPacket msg, FriendlyByteBuf buf) {
        List<LoadoutCatalog.GunOption> guns = msg.snapshot.guns();
        buf.writeVarInt(guns.size());
        for (LoadoutCatalog.GunOption gun : guns) {
            buf.writeResourceLocation(gun.key());
            buf.writeUtf(gun.displayName());
            buf.writeResourceLocation(gun.gunId());
        }

        List<LoadoutCatalog.AttachmentOption> attachments = msg.snapshot.attachments();
        buf.writeVarInt(attachments.size());
        for (LoadoutCatalog.AttachmentOption attachment : attachments) {
            buf.writeResourceLocation(attachment.key());
            buf.writeUtf(attachment.displayName());
            buf.writeEnum(attachment.type());
            buf.writeResourceLocation(attachment.attachmentId());

            buf.writeBoolean(attachment.magBonus() != null);
            if (attachment.magBonus() != null) {
                buf.writeVarInt(attachment.magBonus());
            }

            buf.writeBoolean(attachment.magCapacityOverride() != null);
            if (attachment.magCapacityOverride() != null) {
                buf.writeVarInt(attachment.magCapacityOverride());
            }
        }
    }

    public static S2CCatalogSyncPacket decode(FriendlyByteBuf buf) {
        int gunSize = buf.readVarInt();
        List<LoadoutCatalog.GunOption> guns = new ArrayList<>();
        for (int i = 0; i < gunSize; i++) {
            ResourceLocation key = buf.readResourceLocation();
            String name = buf.readUtf();
            ResourceLocation gunId = buf.readResourceLocation();
            guns.add(new LoadoutCatalog.GunOption(key, name, gunId));
        }

        int attachmentSize = buf.readVarInt();
        List<LoadoutCatalog.AttachmentOption> attachments = new ArrayList<>();
        for (int i = 0; i < attachmentSize; i++) {
            ResourceLocation key = buf.readResourceLocation();
            String name = buf.readUtf();
            AttachmentType type = buf.readEnum(AttachmentType.class);
            ResourceLocation attachmentId = buf.readResourceLocation();

            Integer magBonus = buf.readBoolean() ? buf.readVarInt() : null;
            Integer magCapacityOverride = buf.readBoolean() ? buf.readVarInt() : null;

            attachments.add(new LoadoutCatalog.AttachmentOption(
                    key, name, type, attachmentId, magBonus, magCapacityOverride
            ));
        }

        return new S2CCatalogSyncPacket(new LoadoutCatalog.CatalogSnapshot(guns, attachments));
    }

    public static void handle(S2CCatalogSyncPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> Minecraft.getInstance().execute(() ->
                LoadoutCatalog.applySnapshot(msg.snapshot)
        ));
        ctx.setPacketHandled(true);
    }
}
