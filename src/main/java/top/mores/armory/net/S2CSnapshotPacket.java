package top.mores.armory.net;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import top.mores.armory.client.ClientLoadoutState;
import top.mores.armory.data.ArmoryPreset;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

public record S2CSnapshotPacket(Set<ResourceLocation> unlockedGuns,
                                Set<ResourceLocation> unlockedAttachments,
                                ArmoryPreset currentPreset) {

    public static void encode(S2CSnapshotPacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.unlockedGuns.size());
        for (ResourceLocation rl : msg.unlockedGuns) {
            buf.writeResourceLocation(rl);
        }

        buf.writeVarInt(msg.unlockedAttachments.size());
        for (ResourceLocation rl : msg.unlockedAttachments) {
            buf.writeResourceLocation(rl);
        }

        msg.currentPreset.write(buf);
    }

    public static S2CSnapshotPacket decode(FriendlyByteBuf buf) {
        int gunSize = buf.readVarInt();
        Set<ResourceLocation> guns = new HashSet<>();
        for (int i = 0; i < gunSize; i++) {
            guns.add(buf.readResourceLocation());
        }

        int attachmentSize = buf.readVarInt();
        Set<ResourceLocation> attachments = new HashSet<>();
        for (int i = 0; i < attachmentSize; i++) {
            attachments.add(buf.readResourceLocation());
        }

        ArmoryPreset preset = ArmoryPreset.read(buf);
        return new S2CSnapshotPacket(guns, attachments, preset);
    }

    public static void handle(S2CSnapshotPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> Minecraft.getInstance().execute(() ->
                ClientLoadoutState.update(msg.unlockedGuns, msg.unlockedAttachments, msg.currentPreset)
        ));
        ctx.setPacketHandled(true);
    }
}
