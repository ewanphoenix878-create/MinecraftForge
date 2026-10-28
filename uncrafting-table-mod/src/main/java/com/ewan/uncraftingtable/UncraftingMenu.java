package com.ewan.uncraftingtable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.crafting.IShapedRecipe;

public class UncraftingMenu extends AbstractContainerMenu {
    public static final int BUTTON_UNCRAFT = 0;
    public static final int BUTTON_UNCRAFT_ALL = 1;
    public static final int BUTTON_PREV_RECIPE = 2;
    public static final int BUTTON_NEXT_RECIPE = 3;

    private static final int INPUT_SLOT = 0;
    private static final int RESULT_START = 1;
    private static final int INV_START = 10;
    private static final int INV_END = 46;

    // Tag keys that don't affect what an item was crafted from, so they're allowed on items being uncrafted.
    private static final Set<String> IGNORED_TAGS = Set.of("Damage", "Enchantments", "RepairCost", "display");

    private final ContainerLevelAccess access;
    private final Player player;
    private final SimpleContainer input = new SimpleContainer(1);
    private final SimpleContainer results = new SimpleContainer(9);
    private final DataSlot recipeIndex = DataSlot.standalone();
    private ItemStack lastInput = ItemStack.EMPTY;

    public UncraftingMenu(int id, Inventory inventory) {
        this(id, inventory, ContainerLevelAccess.NULL);
    }

    public UncraftingMenu(int id, Inventory inventory, ContainerLevelAccess access) {
        super(UncraftingTableMod.UNCRAFTING_MENU.get(), id);
        this.access = access;
        this.player = inventory.player;
        this.input.addListener(this::slotsChanged);

        addSlot(new Slot(input, 0, 20, 35));
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                addSlot(new Slot(results, col + row * 3, 94 + col * 18, 17 + row * 18) {
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return false;
                    }
                });
            }
        }
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inventory, col, 8 + col * 18, 142));
        }
        addDataSlot(recipeIndex);
    }

    @Override
    public void slotsChanged(Container container) {
        ItemStack stack = input.getItem(0);
        // The selected recipe is server-owned and synced to the client through the data slot.
        if (!player.level().isClientSide && !ItemStack.isSameItem(stack, lastInput)) {
            recipeIndex.set(0);
        }
        lastInput = stack.copy();
        super.slotsChanged(container);
    }

    public ItemStack getInput() {
        return input.getItem(0);
    }

    public int getRecipeIndex() {
        return recipeIndex.get();
    }

    /** All crafting recipes that could be reversed for the current input, in a stable order shared by client and server. */
    public List<CraftingRecipe> getRecipes() {
        return findRecipes(player.level(), getInput());
    }

    /** The recipe currently selected for the input, or null if there is none. */
    public CraftingRecipe getSelectedRecipe() {
        List<CraftingRecipe> recipes = getRecipes();
        if (recipes.isEmpty()) {
            return null;
        }
        return recipes.get(Math.floorMod(recipeIndex.get(), recipes.size()));
    }

    public static List<CraftingRecipe> findRecipes(Level level, ItemStack stack) {
        List<CraftingRecipe> found = new ArrayList<>();
        if (stack.isEmpty()) {
            return found;
        }
        for (CraftingRecipe recipe : level.getRecipeManager().getAllRecipesFor(RecipeType.CRAFTING)) {
            if (recipe.isSpecial()) {
                continue;
            }
            ItemStack result = recipe.getResultItem(level.registryAccess());
            if (result.isEmpty() || !ItemStack.isSameItem(result, stack) || !tagsMatch(stack, result)) {
                continue;
            }
            if (hasUsableIngredients(recipe.getIngredients())) {
                found.add(recipe);
            }
        }
        found.sort(Comparator.comparing(recipe -> recipe.getId().toString()));
        return found;
    }

    private static boolean tagsMatch(ItemStack stack, ItemStack result) {
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return !result.hasTag();
        }
        CompoundTag stripped = tag.copy();
        IGNORED_TAGS.forEach(stripped::remove);
        CompoundTag resultTag = result.getTag();
        return stripped.isEmpty() ? resultTag == null || resultTag.isEmpty() : stripped.equals(resultTag);
    }

    private static boolean hasUsableIngredients(NonNullList<Ingredient> ingredients) {
        boolean any = false;
        for (Ingredient ingredient : ingredients) {
            if (ingredient.isEmpty()) {
                continue;
            }
            if (ingredient.getItems().length == 0) {
                return false;
            }
            any = true;
        }
        return any;
    }

    /** The 3x3 grid of items one uncraft of the given recipe produces. */
    public static ItemStack[] getGrid(CraftingRecipe recipe) {
        ItemStack[] grid = new ItemStack[9];
        Arrays.fill(grid, ItemStack.EMPTY);
        NonNullList<Ingredient> ingredients = recipe.getIngredients();
        int width = recipe instanceof IShapedRecipe<?> shaped ? shaped.getRecipeWidth() : 3;
        for (int i = 0; i < ingredients.size() && i < 9; i++) {
            Ingredient ingredient = ingredients.get(i);
            if (ingredient.isEmpty()) {
                continue;
            }
            int slot = (i / width) * 3 + i % width;
            if (slot < 9) {
                grid[slot] = ingredient.getItems()[0].copyWithCount(1);
            }
        }
        return grid;
    }

    /** The chance (0 to 1) of getting each ingredient back: the fraction of durability the item has left. */
    public static float getReturnChance(ItemStack stack) {
        if (!stack.isDamageableItem() || stack.getMaxDamage() <= 0) {
            return 1.0F;
        }
        return (float) (stack.getMaxDamage() - stack.getDamageValue()) / stack.getMaxDamage();
    }

    /** How many of the input item one uncraft of the recipe consumes. */
    public int getRequiredCount(CraftingRecipe recipe) {
        return recipe.getResultItem(player.level().registryAccess()).getCount();
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        switch (id) {
            case BUTTON_UNCRAFT -> uncraftOnce();
            case BUTTON_UNCRAFT_ALL -> {
                while (uncraftOnce()) {
                    // keep going until the input runs out or the grid is full
                }
            }
            case BUTTON_PREV_RECIPE -> recipeIndex.set(recipeIndex.get() - 1);
            case BUTTON_NEXT_RECIPE -> recipeIndex.set(recipeIndex.get() + 1);
            default -> {
                return false;
            }
        }
        broadcastChanges();
        return true;
    }

    private boolean uncraftOnce() {
        CraftingRecipe recipe = getSelectedRecipe();
        if (recipe == null) {
            return false;
        }
        int required = getRequiredCount(recipe);
        ItemStack stack = getInput();
        if (stack.getCount() < required) {
            return false;
        }
        ItemStack[] grid = getGrid(recipe);
        for (int i = 0; i < 9; i++) {
            ItemStack existing = results.getItem(i);
            if (grid[i].isEmpty() || existing.isEmpty()) {
                continue;
            }
            if (!ItemStack.isSameItemSameTags(existing, grid[i]) || existing.getCount() + grid[i].getCount() > existing.getMaxStackSize()) {
                return false;
            }
        }
        // Each ingredient is rolled separately, so a half-worn pickaxe might give back the diamond but only one stick.
        float chance = getReturnChance(stack);
        for (int i = 0; i < 9; i++) {
            if (grid[i].isEmpty() || (chance < 1.0F && player.getRandom().nextFloat() >= chance)) {
                continue;
            }
            ItemStack existing = results.getItem(i);
            if (existing.isEmpty()) {
                results.setItem(i, grid[i]);
            } else {
                existing.grow(grid[i].getCount());
            }
        }
        stack.shrink(required);
        input.setChanged();
        results.setChanged();
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (index < INV_START) {
            if (!moveItemStackTo(stack, INV_START, INV_END, true)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, INPUT_SLOT, RESULT_START, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        if (stack.getCount() == original.getCount()) {
            return ItemStack.EMPTY;
        }
        slot.onTake(player, stack);
        return original;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        access.execute((level, pos) -> {
            clearContainer(player, input);
            clearContainer(player, results);
        });
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, UncraftingTableMod.UNCRAFTING_TABLE.get());
    }
}
