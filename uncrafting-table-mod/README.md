# Uncrafting Table (Forge 1.20.1)

Adds an **Uncrafting Table** that turns crafted items back into their ingredients.

## Recipe

A Crafting Table surrounded by 8 Iron Ingots:

```
I I I
I C I
I I I
```

(`I` = Iron Ingot, `C` = Crafting Table). It's also in the Functional Blocks creative tab.

## How to use

1. Put an item in the slot on the left. A faded preview of what you'll get back shows in the 3x3 grid.
2. Click **Uncraft** (Shift-click to uncraft the whole stack).
3. If the item has more than one recipe, use `<` / `>` to pick which one to reverse.
4. Take the ingredients out of the grid. Anything left in the table is given back when you close it.

Notes:
- Items that craft in batches need the full batch (e.g. 4 sticks to get back 2 planks).
- Damaged tools/armour can be uncrafted, but each ingredient only has a chance to come back equal to the
  durability left. A diamond pickaxe at 50% durability gives a 50% chance for the diamond, and a separate
  50% chance for each stick, so you might get the diamond and one stick, both sticks, everything or nothing.
  The table shows the chance (e.g. "50% each") when a damaged item is in the slot.
- Enchantments are lost.
- Ingredients that accept several items (like "any planks") come back as the first option (e.g. oak planks).

## Installing

Download `uncraftingtable-1.0.0.jar` (from the GitHub Actions build artifact, or build it yourself)
and drop it into your `.minecraft/mods` folder with Forge 1.20.1 installed.

## Building

Requires Java 17:

```
./gradlew build
```

The jar ends up in `build/libs/`.
