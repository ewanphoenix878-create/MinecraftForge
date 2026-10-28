package com.jetsonmod;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.entity.EntityMountEvent;

@Mod.EventBusSubscriber(modid = JetsonMod.MODID)
public class CommonEvents {
    /**
     * Shift is the "descend" key while flying, so only let the pilot climb out with Shift
     * once the aircraft has landed.
     */
    @SubscribeEvent
    public static void onDismount(EntityMountEvent event) {
        if (event.isDismounting()
                && event.getEntityBeingMounted() instanceof JetsonEntity jetson
                && jetson.isAlive()
                && event.getEntityMounting() instanceof Player player
                && player.isAlive()
                && player.isShiftKeyDown()
                && !jetson.isLanded()) {
            event.setCanceled(true);
        }
    }
}
