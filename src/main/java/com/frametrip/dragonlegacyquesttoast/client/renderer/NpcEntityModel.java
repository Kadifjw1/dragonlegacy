package com.frametrip.dragonlegacyquesttoast.client.renderer;
 
import com.frametrip.dragonlegacyquesttoast.entity.NpcEntity;
import com.frametrip.dragonlegacyquesttoast.entity.NpcEntityData;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
 
public class NpcEntityModel extends HumanoidModel<NpcEntity> {
 
    public NpcEntityModel(ModelPart root) {
        super(root);
    }
 
    @Override
    public void setupAnim(NpcEntity entity, float limbSwing, float limbSwingAmount,
                           float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
 
        NpcEntityData data = entity.getNpcData();
 
        try {
            this.rightArmPose = ArmPose.valueOf(data.rightArmPose);
        } catch (IllegalArgumentException e) {
            this.rightArmPose = ArmPose.EMPTY;
        }
        try {
            this.leftArmPose = ArmPose.valueOf(data.leftArmPose);
        } catch (IllegalArgumentException e) {
            this.leftArmPose = ArmPose.EMPTY;
        }
 
        if (data.lockBodyRotation) {
            this.body.yRot = (float) Math.toRadians(data.bodyYaw);
        }
    }
}
