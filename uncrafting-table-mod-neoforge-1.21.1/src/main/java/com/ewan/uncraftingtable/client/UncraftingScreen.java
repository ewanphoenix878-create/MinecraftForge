package com.ewan.uncraftingtable.client;

import java.util.List;

import com.ewan.uncraftingtable.UncraftingMenu;
import com.ewan.uncraftingtable.UncraftingTableMod;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;

public class UncraftingScreen extends AbstractContainerScreen<UncraftingMenu> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(UncraftingTableMod.MOD_ID, "textures/gui/container/uncrafting_table.png");

    private Button uncraftButton;
    private Button prevButton;
    private Button nextButton;

    public UncraftingScreen(UncraftingMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        uncraftButton = addRenderableWidget(Button.builder(Component.translatable("gui.uncraftingtable.uncraft"),
                        button -> press(Screen.hasShiftDown() ? UncraftingMenu.BUTTON_UNCRAFT_ALL : UncraftingMenu.BUTTON_UNCRAFT))
                .bounds(leftPos + 41, topPos + 33, 48, 20)
                .tooltip(Tooltip.create(Component.translatable("gui.uncraftingtable.uncraft.tooltip")))
                .build());
        prevButton = addRenderableWidget(Button.builder(Component.literal("<"), button -> press(UncraftingMenu.BUTTON_PREV_RECIPE))
                .bounds(leftPos + 41, topPos + 56, 12, 12)
                .build());
        nextButton = addRenderableWidget(Button.builder(Component.literal(">"), button -> press(UncraftingMenu.BUTTON_NEXT_RECIPE))
                .bounds(leftPos + 77, topPos + 56, 12, 12)
                .build());
        updateButtons();
    }

    private void press(int id) {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
        }
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        updateButtons();
    }

    private void updateButtons() {
        List<CraftingRecipe> recipes = menu.getRecipes();
        CraftingRecipe recipe = recipes.isEmpty() ? null : recipes.get(Math.floorMod(menu.getRecipeIndex(), recipes.size()));
        uncraftButton.active = recipe != null && menu.getInput().getCount() >= menu.getRequiredCount(recipe);
        prevButton.visible = nextButton.visible = recipes.size() > 1;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderGhostGrid(graphics);
        renderTooltip(graphics, mouseX, mouseY);
    }

    /** Shows a faded preview of what the selected recipe will give back in any empty result slots. */
    private void renderGhostGrid(GuiGraphics graphics) {
        CraftingRecipe recipe = menu.getSelectedRecipe();
        if (recipe == null) {
            return;
        }
        ItemStack[] grid = UncraftingMenu.getGrid(recipe);
        for (int i = 0; i < 9; i++) {
            if (grid[i].isEmpty() || menu.getSlot(1 + i).hasItem()) {
                continue;
            }
            int x = leftPos + 94 + (i % 3) * 18;
            int y = topPos + 17 + (i / 3) * 18;
            graphics.renderFakeItem(grid[i], x, y);
            graphics.fill(RenderType.guiGhostRecipeOverlay(), x, y, x + 16, y + 16, 0x30FFFFFF);
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
        ItemStack input = menu.getInput();
        if (input.isEmpty()) {
            return;
        }
        List<CraftingRecipe> recipes = menu.getRecipes();
        float chance = UncraftingMenu.getReturnChance(input);
        if (!recipes.isEmpty() && chance < 1.0F) {
            Component chanceText = Component.translatable("gui.uncraftingtable.chance", Math.round(chance * 100));
            graphics.drawString(font, chanceText, 65 - font.width(chanceText) / 2, 22, 0x404040, false);
        }
        Component status;
        int color;
        if (recipes.isEmpty()) {
            status = Component.translatable("gui.uncraftingtable.no_recipe");
            color = 0xAA0000;
        } else {
            CraftingRecipe recipe = recipes.get(Math.floorMod(menu.getRecipeIndex(), recipes.size()));
            int required = menu.getRequiredCount(recipe);
            if (input.getCount() < required) {
                status = Component.translatable("gui.uncraftingtable.need", required);
                color = 0xAA0000;
            } else if (recipes.size() > 1) {
                status = Component.literal((Math.floorMod(menu.getRecipeIndex(), recipes.size()) + 1) + "/" + recipes.size());
                color = 0x404040;
            } else {
                return;
            }
        }
        graphics.drawString(font, status, 65 - font.width(status) / 2, 58, color, false);
    }
}
