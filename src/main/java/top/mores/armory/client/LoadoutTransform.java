package top.mores.armory.client;

import com.tacz.guns.api.item.attachment.AttachmentType;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.mores.armory.Armory;
import top.mores.armory.client.ui.LoadoutScreen;

import java.util.Objects;

@Mod.EventBusSubscriber(modid = Armory.MODID, value = Dist.CLIENT)
public final class LoadoutTransform {
    private static final float TRANSFORM_SECONDS = 0.25f;

    private static float transformProgress = 1f;
    private static long transformTimestamp = -1L;
    private static AttachmentType oldType = AttachmentType.NONE;
    private static AttachmentType currentType = AttachmentType.NONE;

    private static float openingProgress = 0f;
    private static long openingTimestamp = -1L;

    private LoadoutTransform() {
    }

    public static void init() {
        transformProgress = 1f;
        transformTimestamp = System.currentTimeMillis();
        oldType = AttachmentType.NONE;
        currentType = AttachmentType.NONE;
        openingProgress = 0f;
        openingTimestamp = System.currentTimeMillis();
    }

    public static float getTransformProgress() {
        return transformProgress;
    }

    public static float getOpeningProgress() {
        return openingProgress;
    }

    public static AttachmentType getOldType() {
        return Objects.requireNonNullElse(oldType, AttachmentType.NONE);
    }

    public static AttachmentType getCurrentType() {
        return Objects.requireNonNullElse(currentType, AttachmentType.NONE);
    }

    public static boolean changeView(AttachmentType next) {
        if (transformProgress != 1f || openingProgress != 1f) {
            return false;
        }
        oldType = currentType;
        currentType = next;
        transformProgress = 0f;
        transformTimestamp = System.currentTimeMillis();
        return true;
    }

    @SubscribeEvent
    public static void onRenderTick(TickEvent.RenderTickEvent event) {
        if (openingTimestamp == -1L) {
            openingTimestamp = System.currentTimeMillis();
        }
        if (Minecraft.getInstance().screen instanceof LoadoutScreen) {
            openingProgress += (System.currentTimeMillis() - openingTimestamp) / (TRANSFORM_SECONDS * 1000f);
            if (openingProgress > 1f) openingProgress = 1f;
        } else {
            openingProgress -= (System.currentTimeMillis() - openingTimestamp) / (TRANSFORM_SECONDS * 1000f);
            if (openingProgress < 0f) openingProgress = 0f;
        }
        openingTimestamp = System.currentTimeMillis();

        if (transformTimestamp == -1L) {
            transformTimestamp = System.currentTimeMillis();
        }
        transformProgress += (System.currentTimeMillis() - transformTimestamp) / (TRANSFORM_SECONDS * 1000f);
        if (transformProgress > 1f) transformProgress = 1f;
        transformTimestamp = System.currentTimeMillis();
    }
}
