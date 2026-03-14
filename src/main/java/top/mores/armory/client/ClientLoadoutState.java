package top.mores.armory.client;

import net.minecraft.resources.ResourceLocation;
import top.mores.armory.data.ArmoryPreset;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class ClientLoadoutState {
    private static final Set<ResourceLocation> UNLOCKED_GUNS = new HashSet<>();
    private static final Set<ResourceLocation> UNLOCKED_ATTACHMENTS = new HashSet<>();
    private static ArmoryPreset CURRENT_PRESET = ArmoryPreset.empty();

    private ClientLoadoutState() {
    }

    public static void update(Set<ResourceLocation> guns,
                              Set<ResourceLocation> attachments,
                              ArmoryPreset preset) {
        UNLOCKED_GUNS.clear();
        UNLOCKED_GUNS.addAll(guns);

        UNLOCKED_ATTACHMENTS.clear();
        UNLOCKED_ATTACHMENTS.addAll(attachments);

        CURRENT_PRESET = preset;
    }

    public static Set<ResourceLocation> unlockedGuns() {
        return Collections.unmodifiableSet(UNLOCKED_GUNS);
    }

    public static Set<ResourceLocation> unlockedAttachments() {
        return Collections.unmodifiableSet(UNLOCKED_ATTACHMENTS);
    }

    public static ArmoryPreset currentPreset() {
        return CURRENT_PRESET;
    }
}
