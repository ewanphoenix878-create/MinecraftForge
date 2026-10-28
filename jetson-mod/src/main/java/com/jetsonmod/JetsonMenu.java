package com.jetsonmod;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class JetsonMenu extends AbstractContainerMenu {
    public static final int FUEL_SLOT_X = 80;
    public static final int FUEL_SLOT_Y = 53;

    private final Container fuel;
    private final ContainerData data;
    @Nullable
    private final JetsonEntity jetson;

    // Client side
    public JetsonMenu(int id, Inventory playerInventory) {
        this(id, playerInventory, new SimpleContainer(1), new SimpleContainerData(2), null);
    }

    // Server side
    public JetsonMenu(int id, Inventory playerInventory, Container fuel, ContainerData data, @Nullable JetsonEntity jetson) {
        super(JetsonMod.JETSON_MENU.get(), id);
        checkContainerSize(fuel, 1);
        checkContainerDataCount(data, 2);
        this.fuel = fuel;
        this.data = data;
        this.jetson = jetson;

        addSlot(new Slot(fuel, 0, FUEL_SLOT_X, FUEL_SLOT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return JetsonEntity.isFuel(stack);
            }
        });

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }
        addDataSlots(data);
    }

    public int getBurnTime() {
        return data.get(0);
    }

    public int getMaxBurnTime() {
        return data.get(1);
    }

    /** 0..1 fill level of the fuel gauge. */
    public float getFuelProgress() {
        int max = getMaxBurnTime();
        return max <= 0 ? 0.0F : Math.min(1.0F, (float) getBurnTime() / max);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();
            if (index == 0) {
                if (!moveItemStackTo(stack, 1, slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (JetsonEntity.isFuel(stack)) {
                if (!moveItemStackTo(stack, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                return ItemStack.EMPTY;
            }
            if (stack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return result;
    }

    @Override
    public boolean stillValid(Player player) {
        return jetson == null || (jetson.isAlive() && player.distanceToSqr(jetson) < 64.0);
    }
}
