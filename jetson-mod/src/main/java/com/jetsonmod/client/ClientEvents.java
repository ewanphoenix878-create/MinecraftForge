package com.jetsonmod.client;

import com.jetsonmod.ExitPayload;
import com.jetsonmod.JetsonEntity;
import com.jetsonmod.JetsonMod;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.network.PacketDistributor;

public class ClientEvents {
    public static final KeyMapping EXIT_KEY =
            new KeyMapping("key.jetsonmod.exit", InputConstants.KEY_R, "key.categories.jetsonmod");

    @EventBusSubscriber(modid = JetsonMod.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ModBus {
        @SubscribeEvent
        public static void registerKeys(RegisterKeyMappingsEvent event) {
            event.register(EXIT_KEY);
        }

        @SubscribeEvent
        public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(JetsonMod.JETSON_ENTITY.get(), JetsonRenderer::new);
        }

        @SubscribeEvent
        public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
            event.registerLayerDefinition(JetsonModel.LAYER, JetsonModel::createBodyLayer);
        }

        @SubscribeEvent
        public static void registerScreens(RegisterMenuScreensEvent event) {
            event.register(JetsonMod.JETSON_MENU.get(), JetsonScreen::new);
        }

        @SubscribeEvent
        public static void registerHud(RegisterGuiLayersEvent event) {
            event.registerAbove(VanillaGuiLayers.HOTBAR,
                    ResourceLocation.fromNamespaceAndPath(JetsonMod.MODID, "fuel_hud"), JetsonHud::render);
        }
    }

    @EventBusSubscriber(modid = JetsonMod.MODID, value = Dist.CLIENT)
    public static class GameBus {
        /** Feed the pilot's keys to the aircraft before it ticks. */
        @SubscribeEvent
        public static void onClientTick(ClientTickEvent.Pre event) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null && mc.player.getVehicle() instanceof JetsonEntity jetson) {
                Options o = mc.options;
                boolean inGui = mc.screen != null;
                jetson.setInput(
                        !inGui && o.keyUp.isDown(),
                        !inGui && o.keyDown.isDown(),
                        !inGui && o.keyLeft.isDown(),
                        !inGui && o.keyRight.isDown(),
                        !inGui && o.keyJump.isDown(),
                        !inGui && o.keyShift.isDown());
            }
            while (EXIT_KEY.consumeClick()) {
                if (mc.player != null && mc.player.getVehicle() instanceof JetsonEntity jetson) {
                    if (jetson.isLanded()) {
                        PacketDistributor.sendToServer(ExitPayload.INSTANCE);
                    } else {
                        mc.player.displayClientMessage(Component.translatable("hud.jetsonmod.land_first"), true);
                    }
                }
            }
        }
    }
}
