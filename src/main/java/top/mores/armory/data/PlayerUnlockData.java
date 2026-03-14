package top.mores.armory.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class PlayerUnlockData {
    private final Set<ResourceLocation> unlockedGuns = new HashSet<>();
    private final Set<ResourceLocation> unlockedAttachments = new HashSet<>();
    private final Map<String, ArmoryPreset> presets = new HashMap<>();
    private String selectedPreset = "default";

    public Set<ResourceLocation> unlockedGuns() {
        return unlockedGuns;
    }

    public Set<ResourceLocation> unlockedAttachments() {
        return unlockedAttachments;
    }

    public Map<String, ArmoryPreset> presets() {
        return presets;
    }

    public String selectedPreset() {
        return selectedPreset;
    }

    public void setSelectedPreset(String selectedPreset) {
        this.selectedPreset = selectedPreset;
    }

    public ArmoryPreset getCurrentPreset() {
        return presets.getOrDefault(selectedPreset, ArmoryPreset.empty());
    }

    public void setCurrentPreset(ArmoryPreset preset) {
        presets.put(selectedPreset, preset);
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();

        ListTag guns = new ListTag();
        for (ResourceLocation rl : unlockedGuns) {
            guns.add(StringTag.valueOf(rl.toString()));
        }
        tag.put("guns", guns);

        ListTag attachments = new ListTag();
        for (ResourceLocation rl : unlockedAttachments) {
            attachments.add(StringTag.valueOf(rl.toString()));
        }
        tag.put("attachments", attachments);

        CompoundTag presetsTag = new CompoundTag();
        for (Map.Entry<String, ArmoryPreset> e : presets.entrySet()) {
            presetsTag.put(e.getKey(), e.getValue().toTag());
        }
        tag.put("presets", presetsTag);
        tag.putString("selectedPreset", selectedPreset);

        return tag;
    }

    public static PlayerUnlockData fromTag(CompoundTag tag) {
        PlayerUnlockData data = new PlayerUnlockData();

        ListTag guns = tag.getList("guns", Tag.TAG_STRING);
        for (Tag gunTag : guns) {
            ResourceLocation rl = ResourceLocation.tryParse(gunTag.getAsString());
            if (rl != null) {
                data.unlockedGuns.add(rl);
            }
        }

        ListTag attachments = tag.getList("attachments", Tag.TAG_STRING);
        for (Tag attachmentTag : attachments) {
            ResourceLocation rl = ResourceLocation.tryParse(attachmentTag.getAsString());
            if (rl != null) {
                data.unlockedAttachments.add(rl);
            }
        }

        CompoundTag presetsTag = tag.getCompound("presets");
        for (String key : presetsTag.getAllKeys()) {
            data.presets.put(key, ArmoryPreset.fromTag(presetsTag.getCompound(key)));
        }

        if (tag.contains("selectedPreset", Tag.TAG_STRING)) {
            data.selectedPreset = tag.getString("selectedPreset");
        }

        data.presets.putIfAbsent("default", ArmoryPreset.empty());
        return data;
    }
}
