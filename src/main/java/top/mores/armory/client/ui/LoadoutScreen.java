package top.mores.armory.client.ui;

import com.tacz.guns.api.item.attachment.AttachmentType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import top.mores.armory.bridge.TaczBridge;
import top.mores.armory.catalog.LoadoutCatalog;
import top.mores.armory.client.ClientLoadoutState;
import top.mores.armory.client.LoadoutTransform;
import top.mores.armory.data.ArmoryPreset;
import top.mores.armory.net.C2SApplyPresetPacket;
import top.mores.armory.net.ArmoryNet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class LoadoutScreen extends Screen {
    private static final int OPTION_PAGE_SIZE = 8;

    private ArmoryPreset workingPreset;
    private AttachmentType currentSlot = AttachmentType.NONE;
    private int optionPage = 0;

    public LoadoutScreen() {
        super(Component.literal("TACZ 军械库"));
        this.workingPreset = ClientLoadoutState.currentPreset();
        LoadoutTransform.init();
    }

    @Override
    protected void init() {
        rebuildWidgets();
    }

    protected void rebuildWidgets() {
        clearWidgets();

        int leftX = 16;
        int gunY = 30;
        int i = 0;
        for (LoadoutCatalog.GunOption gun : visibleGuns()) {
            if (i >= 10) break;
            addRenderableWidget(Button.builder(gun.name(), b -> {
                workingPreset = workingPreset.withGun(gun.key());
                optionPage = 0;
                rebuildWidgets();
            }).bounds(leftX, gunY + i * 22, 120, 20).build());
            i++;
        }

        addSlotButtons();
        addAttachmentOptionButtons();

        addRenderableWidget(Button.builder(Component.literal("应用"), b -> {
            if (workingPreset.gunKey() != null) {
                ArmoryNet.CHANNEL.sendToServer(new C2SApplyPresetPacket(workingPreset));
                onClose();
            }
        }).bounds(width - 140, height - 28, 56, 20).build());

        addRenderableWidget(Button.builder(Component.literal("关闭"), b -> onClose())
                .bounds(width - 76, height - 28, 56, 20).build());

        if (currentSlot != AttachmentType.NONE) {
            addRenderableWidget(Button.builder(Component.literal("卸下槽位"), b -> {
                workingPreset = workingPreset.withAttachment(currentSlot, null);
                rebuildWidgets();
            }).bounds(width - 230, height - 28, 80, 20).build());
        }
    }

    private void addSlotButtons() {
        List<AttachmentType> types = Arrays.stream(AttachmentType.values())
                .filter(t -> t != AttachmentType.NONE)
                .sorted(Comparator.comparing(Enum::name))
                .toList();

        int centerX = width / 2;
        int startX = centerX - Math.min(types.size(), 8) * 10;
        int x = startX;
        int y = 18;
        int count = 0;

        for (AttachmentType type : types) {
            addRenderableWidget(new AttachmentSlotButton(
                    x, y,
                    type,
                    () -> currentSlot == type,
                    clicked -> {
                        if (currentSlot == clicked) {
                            currentSlot = AttachmentType.NONE;
                            LoadoutTransform.changeView(AttachmentType.NONE);
                        } else {
                            currentSlot = clicked;
                            LoadoutTransform.changeView(clicked);
                        }
                        optionPage = 0;
                        rebuildWidgets();
                    }
            ));
            x += 20;
            count++;
            if (count % 8 == 0) {
                x = startX;
                y += 20;
            }
        }
    }

    private void addAttachmentOptionButtons() {
        if (currentSlot == AttachmentType.NONE) return;

        List<LoadoutCatalog.AttachmentOption> options = visibleAttachments(currentSlot);
        int totalPages = Math.max(1, (options.size() + OPTION_PAGE_SIZE - 1) / OPTION_PAGE_SIZE);

        int from = optionPage * OPTION_PAGE_SIZE;
        int to = Math.min(options.size(), from + OPTION_PAGE_SIZE);

        int x = width - 200;
        int y = 42;

        for (int i = from; i < to; i++) {
            LoadoutCatalog.AttachmentOption option = options.get(i);
            int row = i - from;
            addRenderableWidget(Button.builder(option.name(), b -> {
                workingPreset = workingPreset.withAttachment(currentSlot, option.key());
                rebuildWidgets();
            }).bounds(x, y + row * 22, 160, 20).build());
        }

        addRenderableWidget(Button.builder(Component.literal("<"), b -> {
            if (optionPage > 0) {
                optionPage--;
                rebuildWidgets();
            }
        }).bounds(x, y + OPTION_PAGE_SIZE * 22 + 6, 20, 20).build());

        addRenderableWidget(Button.builder(Component.literal(">"), b -> {
            if (optionPage < totalPages - 1) {
                optionPage++;
                rebuildWidgets();
            }
        }).bounds(x + 26, y + OPTION_PAGE_SIZE * 22 + 6, 20, 20).build());
    }

    private List<LoadoutCatalog.GunOption> visibleGuns() {
        Set<ResourceLocation> unlocked = ClientLoadoutState.unlockedGuns();
        return LoadoutCatalog.guns().stream()
                .filter(g -> unlocked.contains(g.key()))
                .sorted(Comparator.comparing(g -> g.name().getString()))
                .toList();
    }

    private List<LoadoutCatalog.AttachmentOption> visibleAttachments(AttachmentType type) {
        Set<ResourceLocation> unlocked = ClientLoadoutState.unlockedAttachments();
        List<LoadoutCatalog.AttachmentOption> list = new ArrayList<>();

        for (ResourceLocation key : unlocked) {
            LoadoutCatalog.AttachmentOption option = LoadoutCatalog.getAttachment(key).orElse(null);
            if (option == null || option.type() != type) continue;
            if (!TaczBridge.previewCanUse(workingPreset, type, key)) continue;
            list.add(option);
        }

        list.sort(Comparator.comparing(a -> a.name().getString()));
        return list;
    }

    private ItemStack previewGun() {
        return TaczBridge.buildPreviewGun(workingPreset);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g);
        super.render(g, mouseX, mouseY, partialTick);

        g.drawString(font, title, 16, 8, 0xFFFFFF, false);

        int centerX = width / 2;
        int baseY = height / 2 - 24;

        ItemStack preview = previewGun();
        float open = LoadoutTransform.getOpeningProgress();
        float slotProgress = LoadoutTransform.getTransformProgress();

        float scale = Mth.lerp(open, 0.55f, 1.0f);
        float offsetX = Mth.lerp(slotProgress, -14f, 0f);

        g.pose().pushPose();
        g.pose().translate(centerX - 8 + offsetX, baseY, 0);
        g.pose().scale(scale * 2.2f, scale * 2.2f, 1f);
        if (!preview.isEmpty()) {
            g.renderItem(preview, 0, 0);
        }
        g.pose().popPose();

        String gunName = "未选择主武器";
        if (workingPreset.gunKey() != null) {
            LoadoutCatalog.GunOption gun = LoadoutCatalog.getGun(workingPreset.gunKey()).orElse(null);
            if (gun != null) gunName = gun.name().getString();
        }

        g.drawString(font, "武器: " + gunName, centerX - 90, height / 2 + 26, 0xFFFFFF, false);

        int infoY = height / 2 + 42;
        for (AttachmentType type : AttachmentType.values()) {
            if (type == AttachmentType.NONE) continue;
            ResourceLocation selected = workingPreset.getAttachment(type);
            String text = type.name() + ": " + (selected == null ? "-" : selected.getPath());
            g.drawString(font, text, centerX - 90, infoY, 0xD0D0D0, false);
            infoY += 12;
        }

        if (currentSlot != AttachmentType.NONE) {
            g.drawString(font, "当前编辑槽位: " + currentSlot.name(), width - 200, 24, 0xFFD47A, false);
        } else {
            g.drawString(font, "点击上方槽位开始编辑", width - 200, 24, 0xAAAAAA, false);
        }

        g.drawString(font, "左侧: 已解锁武器", 16, 18, 0xAAAAAA, false);
        g.drawString(font, "按 O 打开军械库", 16, height - 20, 0xAAAAAA, false);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
