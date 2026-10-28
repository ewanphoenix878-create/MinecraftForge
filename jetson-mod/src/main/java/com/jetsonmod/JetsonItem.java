package com.jetsonmod;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

public class JetsonItem extends Item {
    public JetsonItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos().relative(context.getClickedFace());
        JetsonEntity jetson = new JetsonEntity(JetsonMod.JETSON_ENTITY.get(), level);
        jetson.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0.0F, 0.0F);
        Player player = context.getPlayer();
        if (player != null) {
            jetson.setYRot(player.getYRot());
        }
        if (!level.noCollision(jetson, jetson.getBoundingBox())) {
            return InteractionResult.FAIL;
        }
        if (level instanceof ServerLevel) {
            level.addFreshEntity(jetson);
            level.gameEvent(player, GameEvent.ENTITY_PLACE, pos);
            if (player == null || !player.getAbilities().instabuild) {
                context.getItemInHand().shrink(1);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
