package com.jetsonmod;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.entity.EntityMountEvent;

@Mod.EventBusSubscriber(modid = JetsonMod.MODID)
public class CommonEvents {
    /**
     * Shift is the "descend" key, so it never lets the pilot out; the exit key (R) does that
     * through {@link JetsonEntity#handleExitRequest}.
     * <p>
     * Server only: clients just apply the server's passenger updates. Blocking one on a client
     * (where other players' sneak state is all it can see) left them stuck "riding" there after
     * they had already got out on the server.
     */
    @SubscribeEvent
    public static void onDismount(EntityMountEvent event) {
        if (event.isDismounting()
                && event.getEntityBeingMounted() instanceof JetsonEntity jetson
                && !jetson.level().isClientSide()
                && jetson.isAlive()
                && event.getEntityMounting() instanceof Player player
                && player.isAlive()
                && player.isShiftKeyDown()
                && !jetson.isExitRequested()) {
            event.setCanceled(true);
        }
    }
}
