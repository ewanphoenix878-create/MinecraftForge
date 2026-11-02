package com.jetsonmod.client;

import com.jetsonmod.JetsonEntity;
import com.jetsonmod.JetsonMod;
import com.jetsonmod.JetsonNetwork;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ClientEvents {
    public static final KeyMapping EXIT_KEY =
            new KeyMapping("key.jetsonmod.exit", InputConstants.KEY_R, "key.categories.jetsonmod");

    @Mod.EventBusSubscriber(modid = JetsonMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ModBus {
        @SubscribeEvent
        public static void clientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> MenuScreens.register(JetsonMod.JETSON_MENU.get(), JetsonScreen::new));
        }

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
        public static void registerHud(RegisterGuiOverlaysEvent event) {
            event.registerAbove(VanillaGuiOverlay.HOTBAR.id(), "fuel_hud",
                    (gui, graphics, partialTick, width, height) -> JetsonHud.render(graphics));
        }
    }

    @Mod.EventBusSubscriber(modid = JetsonMod.MODID, value = Dist.CLIENT)
    public static class GameBus {
        /** Feed the pilot's keys to the aircraft before it ticks. */
        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.START) {
                return;
            }
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
                        JetsonNetwork.CHANNEL.sendToServer(new JetsonNetwork.ExitMessage());
                    } else {
                        mc.player.displayClientMessage(Component.translatable("hud.jetsonmod.land_first"), true);
                    }
                }
            }
        }
    }
}
