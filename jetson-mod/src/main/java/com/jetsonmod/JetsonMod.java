package com.jetsonmod;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(JetsonMod.MODID)
public class JetsonMod {
    public static final String MODID = "jetsonmod";

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, MODID);
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, MODID);
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, MODID);

    public static final DeferredHolder<SoundEvent, SoundEvent> ROTOR_SOUND = SOUNDS.register("rotor",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MODID, "rotor")));
    public static final DeferredHolder<SoundEvent, SoundEvent> FLUSH_SOUND = SOUNDS.register("flush",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MODID, "flush")));

    public static final DeferredHolder<EntityType<?>, EntityType<JetsonEntity>> JETSON_ENTITY = ENTITY_TYPES.register("jetson",
            () -> EntityType.Builder.<JetsonEntity>of(JetsonEntity::new, MobCategory.MISC)
                    .sized(1.2F, 1.6F)
                    .passengerAttachments(0.6F)
                    .clientTrackingRange(10)
                    .build("jetson"));

    public static final DeferredItem<JetsonItem> JETSON_ITEM = ITEMS.register("jetson",
            () -> new JetsonItem(new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<MenuType<?>, MenuType<JetsonMenu>> JETSON_MENU = MENUS.register("jetson",
            () -> new MenuType<>(JetsonMenu::new, FeatureFlags.DEFAULT_FLAGS));

    public JetsonMod(IEventBus modBus) {
        ITEMS.register(modBus);
        ENTITY_TYPES.register(modBus);
        MENUS.register(modBus);
        SOUNDS.register(modBus);
        modBus.addListener(this::addCreative);
        modBus.addListener(this::registerPayloads);
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(ExitPayload.TYPE, ExitPayload.STREAM_CODEC, ExitPayload::handle);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(JETSON_ITEM.get());
        }
    }
}
