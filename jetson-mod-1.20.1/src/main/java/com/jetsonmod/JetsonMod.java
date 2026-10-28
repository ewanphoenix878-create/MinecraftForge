package com.jetsonmod;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod(JetsonMod.MODID)
public class JetsonMod {
    public static final String MODID = "jetsonmod";

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MODID);
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, MODID);

    public static final RegistryObject<EntityType<JetsonEntity>> JETSON_ENTITY = ENTITY_TYPES.register("jetson",
            () -> EntityType.Builder.<JetsonEntity>of(JetsonEntity::new, MobCategory.MISC)
                    .sized(2.2F, 1.3F)
                    .clientTrackingRange(10)
                    .build("jetson"));

    public static final RegistryObject<JetsonItem> JETSON_ITEM = ITEMS.register("jetson",
            () -> new JetsonItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<MenuType<JetsonMenu>> JETSON_MENU = MENUS.register("jetson",
            () -> new MenuType<>(JetsonMenu::new, FeatureFlags.DEFAULT_FLAGS));

    public JetsonMod() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ITEMS.register(modBus);
        ENTITY_TYPES.register(modBus);
        MENUS.register(modBus);
        modBus.addListener(this::addCreative);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(JETSON_ITEM.get());
        }
    }
}
