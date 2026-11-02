package com.jetsonmod.client;

import com.jetsonmod.JetsonEntity;
import com.jetsonmod.JetsonMod;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/** Flying toilet: a gadget-style telescoping mast rises out of the cistern with a rotor on top. */
public class JetsonModel extends EntityModel<JetsonEntity> {
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(new ResourceLocation(JetsonMod.MODID, "jetson"), "main");

    private final ModelPart root;
    private final ModelPart rotor;

    public JetsonModel(ModelPart root) {
        this.root = root;
        this.rotor = root.getChild("rotor");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("toilet", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4F, 16F, -7F, 8F, 8F, 16F)
                .texOffs(48, 0).addBox(-6F, 13F, -11F, 12F, 3F, 15F)
                .texOffs(0, 24).addBox(-6F, 12F, -11F, 12F, 1F, 2F)
                .texOffs(28, 24).addBox(-6F, 12F, -9F, 2F, 1F, 13F)
                .texOffs(28, 24).addBox(4F, 12F, -9F, 2F, 1F, 13F)
                .texOffs(58, 24).addBox(-6F, 1F, 4F, 12F, 11F, 1F)
                .texOffs(84, 24).addBox(-4F, 13F, 5F, 8F, 3F, 4F)
                .texOffs(0, 38).addBox(-7F, 0F, 5F, 14F, 13F, 6F)
                .texOffs(40, 38).addBox(-8F, -1F, 4F, 16F, 1F, 8F)
                .texOffs(88, 38).addBox(-8F, 2F, 6F, 1F, 1F, 3F),
                PartPose.offset(0F, 0F, 0F));
        root.addOrReplaceChild("mast", CubeListBuilder.create()
                .texOffs(96, 38).addBox(-2F, -8F, 6F, 4F, 7F, 4F)
                .texOffs(112, 38).addBox(-1F, -18F, 7F, 2F, 10F, 2F)
                .texOffs(0, 57).addBox(-2F, -9F, 6F, 4F, 1F, 4F),
                PartPose.offset(0F, 0F, 0F));
        root.addOrReplaceChild("rotor", CubeListBuilder.create()
                .texOffs(16, 57).addBox(-2F, -2F, -2F, 4F, 2F, 4F)
                .texOffs(32, 57).addBox(-22F, -2F, -1F, 44F, 1F, 2F)
                .texOffs(0, 63).addBox(-1F, -3F, -1F, 2F, 1F, 2F),
                PartPose.offset(0F, -18F, 8F));

        return LayerDefinition.create(mesh, 128, 128);
    }

    public void prepare(JetsonEntity entity, float partialTick) {
        rotor.yRot = entity.getPropAngle(partialTick) * Mth.DEG_TO_RAD;
    }

    @Override
    public void setupAnim(JetsonEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        root.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
