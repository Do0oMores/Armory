package top.mores.armory.net;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import top.mores.armory.Armory;

public final class ArmoryNet {
    private static final String PROTOCOL = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Armory.MODID, "main"),
            () -> PROTOCOL,
            PROTOCOL::equals,
            PROTOCOL::equals
    );

    private static int id = 0;

    private ArmoryNet() {
    }

    public static void register() {
        CHANNEL.registerMessage(id++, C2SApplyPresetPacket.class,
                C2SApplyPresetPacket::encode,
                C2SApplyPresetPacket::decode,
                C2SApplyPresetPacket::handle);

        CHANNEL.registerMessage(id++, S2CSnapshotPacket.class,
                S2CSnapshotPacket::encode,
                S2CSnapshotPacket::decode,
                S2CSnapshotPacket::handle);

        CHANNEL.registerMessage(id++, S2CCatalogSyncPacket.class,
                S2CCatalogSyncPacket::encode,
                S2CCatalogSyncPacket::decode,
                S2CCatalogSyncPacket::handle);
    }

    public static void sendTo(ServerPlayer player, Object packet) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }
}
