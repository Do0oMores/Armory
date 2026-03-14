package top.mores.armory.net;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import top.mores.armory.data.ArmoryPreset;
import top.mores.armory.service.LoadoutApplyService;

import java.util.function.Supplier;

public record C2SApplyPresetPacket(ArmoryPreset preset) {
    public static void encode(C2SApplyPresetPacket msg, FriendlyByteBuf buf) {
        msg.preset.write(buf);
    }

    public static C2SApplyPresetPacket decode(FriendlyByteBuf buf) {
        return new C2SApplyPresetPacket(ArmoryPreset.read(buf));
    }

    public static void handle(C2SApplyPresetPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            if (ctx.getSender() != null) {
                LoadoutApplyService.apply(ctx.getSender(), msg.preset);
            }
        });
        ctx.setPacketHandled(true);
    }
}
