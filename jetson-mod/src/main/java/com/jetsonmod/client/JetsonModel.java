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

/** Single-seat eVTOL: tub hull, roll cage, skids and four rotors on outrigger arms. */
public class JetsonModel extends EntityModel<JetsonEntity> {
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(JetsonMod.MODID, "jetson"), "main");

    private final ModelPart root;
    private final ModelPart propFL, propFR, propRL, propRR;

    public JetsonModel(ModelPart root) {
        this.root = root;
        this.propFL = root.getChild("prop_fl");
        this.propFR = root.getChild("prop_fr");
        this.propRL = root.getChild("prop_rl");
        this.propRR = root.getChild("prop_rr");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("hull", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-6F, 14F, -10F, 12F, 6F, 20F)
                .texOffs(64, 0).addBox(-5F, 15F, -14F, 10F, 4F, 4F)
                .texOffs(0, 26).addBox(-5F, 20F, -9F, 10F, 1F, 18F)
                .texOffs(56, 26).addBox(-4F, 13F, -3F, 8F, 1F, 9F)
                .texOffs(90, 26).addBox(-5F, 4F, 6F, 10F, 10F, 2F)
                .texOffs(0, 45).addBox(-6F, 13F, -10F, 12F, 1F, 3F),
                PartPose.offset(0F, 0F, 0F));
        root.addOrReplaceChild("cage", CubeListBuilder.create()
                .texOffs(30, 45).addBox(-7F, 4F, -8F, 1F, 1F, 16F)
                .texOffs(30, 45).addBox(6F, 4F, -8F, 1F, 1F, 16F)
                .texOffs(64, 45).addBox(-7F, 5F, -8F, 1F, 10F, 1F)
                .texOffs(64, 45).addBox(6F, 5F, -8F, 1F, 10F, 1F)
                .texOffs(68, 45).addBox(-7F, 3F, -8F, 14F, 1F, 1F)
                .texOffs(68, 45).addBox(-7F, 3F, 7F, 14F, 1F, 1F),
                PartPose.offset(0F, 0F, 0F));
        root.addOrReplaceChild("skids", CubeListBuilder.create()
                .texOffs(0, 62).addBox(-7F, 22F, -10F, 1F, 2F, 20F)
                .texOffs(0, 62).addBox(6F, 22F, -10F, 1F, 2F, 20F)
                .texOffs(42, 62).addBox(-7F, 20F, -6F, 1F, 2F, 1F)
                .texOffs(42, 62).addBox(6F, 20F, -6F, 1F, 2F, 1F)
                .texOffs(42, 62).addBox(-7F, 20F, 5F, 1F, 2F, 1F)
                .texOffs(42, 62).addBox(6F, 20F, 5F, 1F, 2F, 1F),
                PartPose.offset(0F, 0F, 0F));
        root.addOrReplaceChild("arms", CubeListBuilder.create()
                .texOffs(46, 62).addBox(-17F, 15F, -9F, 34F, 1F, 2F)
                .texOffs(46, 62).addBox(-17F, 15F, 7F, 34F, 1F, 2F),
                PartPose.offset(0F, 0F, 0F));
        root.addOrReplaceChild("motor_fl", CubeListBuilder.create()
                .texOffs(0, 84).addBox(-2F, -3F, -2F, 4F, 4F, 4F),
                PartPose.offset(16F, 15F, -8F));
        root.addOrReplaceChild("prop_fl", CubeListBuilder.create()
                .texOffs(16, 84).addBox(-7F, 0F, -1F, 14F, 1F, 2F)
                .texOffs(48, 84).addBox(-1F, 0F, -7F, 2F, 1F, 14F)
                .texOffs(80, 84).addBox(-1F, -1F, -1F, 2F, 1F, 2F),
                PartPose.offset(16F, 11.5F, -8F));
        root.addOrReplaceChild("motor_fr", CubeListBuilder.create()
                .texOffs(0, 84).addBox(-2F, -3F, -2F, 4F, 4F, 4F),
                PartPose.offset(-16F, 15F, -8F));
        root.addOrReplaceChild("prop_fr", CubeListBuilder.create()
                .texOffs(16, 84).addBox(-7F, 0F, -1F, 14F, 1F, 2F)
                .texOffs(48, 84).addBox(-1F, 0F, -7F, 2F, 1F, 14F)
                .texOffs(80, 84).addBox(-1F, -1F, -1F, 2F, 1F, 2F),
                PartPose.offset(-16F, 11.5F, -8F));
        root.addOrReplaceChild("motor_rl", CubeListBuilder.create()
                .texOffs(0, 84).addBox(-2F, -3F, -2F, 4F, 4F, 4F),
                PartPose.offset(16F, 15F, 8F));
        root.addOrReplaceChild("prop_rl", CubeListBuilder.create()
                .texOffs(16, 84).addBox(-7F, 0F, -1F, 14F, 1F, 2F)
                .texOffs(48, 84).addBox(-1F, 0F, -7F, 2F, 1F, 14F)
                .texOffs(80, 84).addBox(-1F, -1F, -1F, 2F, 1F, 2F),
                PartPose.offset(16F, 11.5F, 8F));
        root.addOrReplaceChild("motor_rr", CubeListBuilder.create()
                .texOffs(0, 84).addBox(-2F, -3F, -2F, 4F, 4F, 4F),
                PartPose.offset(-16F, 15F, 8F));
        root.addOrReplaceChild("prop_rr", CubeListBuilder.create()
                .texOffs(16, 84).addBox(-7F, 0F, -1F, 14F, 1F, 2F)
                .texOffs(48, 84).addBox(-1F, 0F, -7F, 2F, 1F, 14F)
                .texOffs(80, 84).addBox(-1F, -1F, -1F, 2F, 1F, 2F),
                PartPose.offset(-16F, 11.5F, 8F));
        return LayerDefinition.create(mesh, 128, 128);
    }

    /** Spin the rotors; diagonal pairs turn in opposite directions like a real quad. */
    public void prepare(JetsonEntity entity, float partialTick) {
        float angle = entity.getPropAngle(partialTick) * Mth.DEG_TO_RAD;
        propFL.yRot = angle;
        propRR.yRot = angle;
        propFR.yRot = -angle;
        propRL.yRot = -angle;
    }

    @Override
    public void setupAnim(JetsonEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        root.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
