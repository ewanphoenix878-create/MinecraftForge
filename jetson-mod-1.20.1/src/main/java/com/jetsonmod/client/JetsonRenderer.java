package com.jetsonmod.client;

import com.jetsonmod.JetsonEntity;
import com.jetsonmod.JetsonMod;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class JetsonRenderer extends EntityRenderer<JetsonEntity> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(JetsonMod.MODID, "textures/entity/jetson.png");

    private final JetsonModel model;

    public JetsonRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 1.1F;
        this.model = new JetsonModel(context.bakeLayer(JetsonModel.LAYER));
    }

    @Override
    public void render(JetsonEntity entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - entityYaw));

        // Lean into the direction of travel, like a real multirotor.
        double dx = entity.getX() - entity.xo;
        double dz = entity.getZ() - entity.zo;
        float yaw = entity.getYRot() * Mth.DEG_TO_RAD;
        double forward = -dx * Mth.sin(yaw) + dz * Mth.cos(yaw);
        double left = dx * Mth.cos(yaw) + dz * Mth.sin(yaw);
        float pitch = Mth.clamp((float) (-forward * 25.0), -14.0F, 14.0F);
        float roll = Mth.clamp((float) (left * 25.0), -14.0F, 14.0F);
        poseStack.mulPose(Axis.XP.rotationDegrees(pitch));
        poseStack.mulPose(Axis.ZP.rotationDegrees(roll));

        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.translate(0.0F, -1.501F, 0.0F);

        model.prepare(entity, partialTick);
        VertexConsumer consumer = buffer.getBuffer(model.renderType(TEXTURE));
        model.renderToBuffer(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        poseStack.popPose();

        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(JetsonEntity entity) {
        return TEXTURE;
    }
}
