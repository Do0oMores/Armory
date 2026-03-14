package top.mores.armory.data;

import com.tacz.guns.api.item.attachment.AttachmentType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.Map;

public record ArmoryPreset(@Nullable ResourceLocation gunKey,
                           EnumMap<AttachmentType, ResourceLocation> attachments) {

    public ArmoryPreset {
        EnumMap<AttachmentType, ResourceLocation> safe = new EnumMap<>(AttachmentType.class);
        if (attachments != null) {
            safe.putAll(attachments);
        }
        attachments = safe;
    }

    public static ArmoryPreset empty() {
        return new ArmoryPreset(null, new EnumMap<>(AttachmentType.class));
    }

    public ArmoryPreset withGun(@Nullable ResourceLocation newGunKey) {
        return new ArmoryPreset(newGunKey, attachments);
    }

    public ArmoryPreset withAttachment(AttachmentType type, @Nullable ResourceLocation attachmentKey) {
        EnumMap<AttachmentType, ResourceLocation> copy = new EnumMap<>(attachments);
        if (attachmentKey == null) {
            copy.remove(type);
        } else {
            copy.put(type, attachmentKey);
        }
        return new ArmoryPreset(gunKey, copy);
    }

    @Nullable
    public ResourceLocation getAttachment(AttachmentType type) {
        return attachments.get(type);
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        if (gunKey != null) {
            tag.putString("gun", gunKey.toString());
        }

        CompoundTag at = new CompoundTag();
        for (Map.Entry<AttachmentType, ResourceLocation> e : attachments.entrySet()) {
            at.putString(e.getKey().name(), e.getValue().toString());
        }
        tag.put("attachments", at);
        return tag;
    }

    public static ArmoryPreset fromTag(CompoundTag tag) {
        ResourceLocation gun = null;
        if (tag.contains("gun")) {
            gun = ResourceLocation.tryParse(tag.getString("gun"));
        }

        EnumMap<AttachmentType, ResourceLocation> attachments = new EnumMap<>(AttachmentType.class);
        CompoundTag at = tag.getCompound("attachments");
        for (AttachmentType type : AttachmentType.values()) {
            if (type == AttachmentType.NONE) continue;
            if (!at.contains(type.name())) continue;

            ResourceLocation rl = ResourceLocation.tryParse(at.getString(type.name()));
            if (rl != null) {
                attachments.put(type, rl);
            }
        }
        return new ArmoryPreset(gun, attachments);
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeBoolean(gunKey != null);
        if (gunKey != null) {
            buf.writeResourceLocation(gunKey);
        }

        buf.writeVarInt(attachments.size());
        for (Map.Entry<AttachmentType, ResourceLocation> e : attachments.entrySet()) {
            buf.writeEnum(e.getKey());
            buf.writeResourceLocation(e.getValue());
        }
    }

    public static ArmoryPreset read(FriendlyByteBuf buf) {
        ResourceLocation gun = buf.readBoolean() ? buf.readResourceLocation() : null;

        int size = buf.readVarInt();
        EnumMap<AttachmentType, ResourceLocation> attachments = new EnumMap<>(AttachmentType.class);
        for (int i = 0; i < size; i++) {
            AttachmentType type = buf.readEnum(AttachmentType.class);
            ResourceLocation value = buf.readResourceLocation();
            attachments.put(type, value);
        }
        return new ArmoryPreset(gun, attachments);
    }
}
