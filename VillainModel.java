package com.plusultra.client;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.world.entity.LivingEntity;

/** Stylized villain model with horns, reinforced shoulders and a raised collar. */
public class VillainModel<T extends LivingEntity> extends HumanoidModel<T> {
    public VillainModel(ModelPart root) { super(root); }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        PartDefinition root = mesh.getRoot();
        PartDefinition head = root.getChild("head");
        head.addOrReplaceChild("horn_left", CubeListBuilder.create().texOffs(0, 48)
                .addBox(-5.0F, -10.5F, -2.0F, 3.0F, 4.0F, 3.0F), PartPose.offset(0, 0, 0));
        head.addOrReplaceChild("horn_right", CubeListBuilder.create().texOffs(12, 48)
                .addBox(2.0F, -10.5F, -2.0F, 3.0F, 4.0F, 3.0F), PartPose.offset(0, 0, 0));
        PartDefinition body = root.getChild("body");
        body.addOrReplaceChild("collar", CubeListBuilder.create().texOffs(0, 40)
                .addBox(-5.0F, -1.0F, -3.5F, 10.0F, 4.0F, 2.0F), PartPose.offset(0, 0, 0));
        body.addOrReplaceChild("shoulder_left", CubeListBuilder.create().texOffs(24, 40)
                .addBox(4.0F, -1.0F, -3.0F, 3.0F, 5.0F, 6.0F), PartPose.offset(0, 0, 0));
        body.addOrReplaceChild("shoulder_right", CubeListBuilder.create().texOffs(42, 40)
                .addBox(-7.0F, -1.0F, -3.0F, 3.0F, 5.0F, 6.0F), PartPose.offset(0, 0, 0));
        return LayerDefinition.create(mesh, 64, 64);
    }
}
