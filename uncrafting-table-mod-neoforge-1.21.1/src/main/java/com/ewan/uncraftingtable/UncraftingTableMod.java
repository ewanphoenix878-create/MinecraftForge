package com.ewan.uncraftingtable;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(UncraftingTableMod.MOD_ID)
public class UncraftingTableMod {
    public static final String MOD_ID = "uncraftingtable";

    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MOD_ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, MOD_ID);

    public static final DeferredBlock<UncraftingTableBlock> UNCRAFTING_TABLE = BLOCKS.register("uncrafting_table",
            () -> new UncraftingTableBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(3.5F)
                    .sound(SoundType.METAL)));

    public static final DeferredItem<BlockItem> UNCRAFTING_TABLE_ITEM = ITEMS.registerSimpleBlockItem("uncrafting_table", UNCRAFTING_TABLE);

    public static final DeferredHolder<MenuType<?>, MenuType<UncraftingMenu>> UNCRAFTING_MENU = MENUS.register("uncrafting_table",
            () -> IMenuTypeExtension.create((id, inventory, buf) -> new UncraftingMenu(id, inventory)));

    public UncraftingTableMod(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        MENUS.register(modBus);
        modBus.addListener(this::addCreative);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(UNCRAFTING_TABLE_ITEM);
        }
    }
}
