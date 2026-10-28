package com.jetsonmod.client;

import com.jetsonmod.JetsonEntity;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/** Small fuel gauge shown above the hotbar while flying. */
public final class JetsonHud {
    private static final int WIDTH = 102;

    private JetsonHud() {
    }

    public static void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui || !(mc.player.getVehicle() instanceof JetsonEntity jetson)) {
            return;
        }

        int x = 8;
        int y = graphics.guiHeight() - 46;

        int burn = jetson.getBurnTime();
        int max = jetson.getMaxBurnTime();
        float fill = max <= 0 ? 0.0F : Math.min(1.0F, (float) burn / max);
        int color = fill > 0.5F ? 0xFF3FD13F : fill > 0.2F ? 0xFFE8C43A : 0xFFE0453A;

        graphics.fill(x - 2, y - 12, x + WIDTH + 24, y + 10, 0x90000000);
        Component label = burn > 0
                ? Component.translatable("hud.jetsonmod.fuel", (burn / 20) + "s")
                : Component.translatable("hud.jetsonmod.empty");
        graphics.drawString(mc.font, label, x, y - 10, burn > 0 ? 0xFFFFFF : 0xFF5555);

        graphics.fill(x, y, x + WIDTH, y + 8, 0xFF202020);
        graphics.fill(x + 1, y + 1, x + 1 + (int) ((WIDTH - 2) * fill), y + 7, color);

        ItemStack reserve = jetson.getFuelStack();
        if (!reserve.isEmpty()) {
            graphics.renderItem(reserve, x + WIDTH + 4, y - 9);
            graphics.renderItemDecorations(mc.font, reserve, x + WIDTH + 4, y - 9);
        }
    }
}
