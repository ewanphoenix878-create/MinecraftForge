package com.jetsonmod.client;

import com.jetsonmod.JetsonMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;

/** Fuel tank UI, drawn with plain fills so it needs no GUI texture. */
public class JetsonScreen extends AbstractContainerScreen<JetsonMenu> {
    private static final int GAUGE_X = 38;
    private static final int GAUGE_Y = 22;
    private static final int GAUGE_W = 100;
    private static final int GAUGE_H = 10;

    public JetsonScreen(JetsonMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;

        // Vanilla-style panel
        g.fill(x, y, x + imageWidth, y + imageHeight, 0xFF000000);
        g.fill(x + 1, y + 1, x + imageWidth - 1, y + imageHeight - 1, 0xFFFFFFFF);
        g.fill(x + 3, y + 3, x + imageWidth - 1, y + imageHeight - 1, 0xFF555555);
        g.fill(x + 3, y + 3, x + imageWidth - 3, y + imageHeight - 3, 0xFFC6C6C6);

        for (Slot slot : menu.slots) {
            drawSlot(g, x + slot.x - 1, y + slot.y - 1);
        }

        // Fuel gauge
        float fill = menu.getFuelProgress();
        int gx = x + GAUGE_X;
        int gy = y + GAUGE_Y;
        g.fill(gx - 1, gy - 1, gx + GAUGE_W + 1, gy + GAUGE_H + 1, 0xFF373737);
        g.fill(gx, gy, gx + GAUGE_W, gy + GAUGE_H, 0xFF1E1E1E);
        int color = fill > 0.5F ? 0xFF3FD13F : fill > 0.2F ? 0xFFE8C43A : 0xFFE0453A;
        g.fill(gx, gy, gx + (int) (GAUGE_W * fill), gy + GAUGE_H, color);
        for (int i = 1; i < 4; i++) {
            int tx = gx + GAUGE_W * i / 4;
            g.fill(tx, gy, tx + 1, gy + 3, 0xFF000000);
        }

        // Small flame beside the fuel slot while burning
        if (menu.getBurnTime() > 0) {
            int fx = x + JetsonMenu.FUEL_SLOT_X - 14;
            int fy = y + JetsonMenu.FUEL_SLOT_Y + 4;
            g.fill(fx + 2, fy, fx + 6, fy + 9, 0xFFFF8A1E);
            g.fill(fx, fy + 3, fx + 8, fy + 9, 0xFFFF5A12);
            g.fill(fx + 3, fy + 4, fx + 5, fy + 9, 0xFFFFE070);
        }
    }

    private static void drawSlot(GuiGraphics g, int x, int y) {
        g.fill(x, y, x + 18, y + 18, 0xFF8B8B8B);
        g.fill(x, y, x + 17, y + 1, 0xFF373737);
        g.fill(x, y, x + 1, y + 17, 0xFF373737);
        g.fill(x + 1, y + 17, x + 18, y + 18, 0xFFFFFFFF);
        g.fill(x + 17, y + 1, x + 18, y + 18, 0xFFFFFFFF);
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        super.renderLabels(g, mouseX, mouseY);
        ItemStack reserve = menu.getSlot(0).getItem();
        int perItem = reserve.isEmpty() ? 0 : reserve.getBurnTime(RecipeType.SMELTING);
        int totalSeconds = (menu.getBurnTime() + perItem * reserve.getCount()) / 20;
        Component text = Component.literal("Flight time: " + (totalSeconds / 60) + "m " + (totalSeconds % 60) + "s");
        g.drawString(font, text, (imageWidth - font.width(text)) / 2, GAUGE_Y + GAUGE_H + 4, 0x404040, false);
    }
}
