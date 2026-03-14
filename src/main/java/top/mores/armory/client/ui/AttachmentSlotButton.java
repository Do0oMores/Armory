package top.mores.armory.client.ui;

import com.tacz.guns.api.item.attachment.AttachmentType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class AttachmentSlotButton extends AbstractButton {
    private final AttachmentType type;
    private final Supplier<Boolean> selectedSupplier;
    private final Consumer<AttachmentType> onPressAction;

    public AttachmentSlotButton(int x, int y,
                                AttachmentType type,
                                Supplier<Boolean> selectedSupplier,
                                Consumer<AttachmentType> onPressAction) {
        super(x, y, 18, 18, Component.literal(shortName(type)));
        this.type = type;
        this.selectedSupplier = selectedSupplier;
        this.onPressAction = onPressAction;
    }

    private static String shortName(AttachmentType type) {
        String n = type.name();
        return n.length() <= 3 ? n : n.substring(0, 3);
    }

    @Override
    public void onPress() {
        onPressAction.accept(type);
    }

    @Override
    protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        int bg = selectedSupplier.get() ? 0xFFCC8B2F : (isHoveredOrFocused() ? 0xFF5A5A5A : 0xFF303030);
        g.fill(getX(), getY(), getX() + width, getY() + height, bg);
        g.drawCenteredString(
                net.minecraft.client.Minecraft.getInstance().font,
                shortName(type),
                getX() + width / 2,
                getY() + 5,
                0xFFFFFF
        );
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }
}
