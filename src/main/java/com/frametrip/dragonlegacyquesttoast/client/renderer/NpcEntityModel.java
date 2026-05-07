package com.frametrip.dragonlegacyquesttoast.client.renderer;
 
import com.frametrip.dragonlegacyquesttoast.entity.NpcEntity;
import com.frametrip.dragonlegacyquesttoast.entity.NpcEntityData;
import com.frametrip.dragonlegacyquesttoast.server.animation.AnimationState;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
 
public class NpcEntityModel extends HumanoidModel<NpcEntity> {
 
    public NpcEntityModel(ModelPart root) {
        super(root);
    }
 
    @Override
    public void setupAnim(NpcEntity entity, float limbSwing, float limbSwingAmount,
                           float ageInTicks, float netHeadYaw, float headPitch) {
        NpcEntityData data = entity.getNpcData();

     // Arm poses must be set BEFORE super so HumanoidModel reads them correctly.
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

     super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
     
        if (data.lockBodyRotation) {
            this.body.yRot = (float) Math.toRadians(data.bodyYaw);
        }

     // Determine current animation state from movement.
        AnimationState state;
        if (limbSwingAmount > 0.05f) {
            state = limbSwingAmount > 0.7f ? AnimationState.RUN : AnimationState.WALK;
        } else {
            state = AnimationState.IDLE;
        }

        // Apply custom keyframe animation on top of base pose (only for bones with keyframes).
        NpcAnimationPlayer.applyAnimation(entity.getUUID(), state, ageInTicks, data, this);
    }
}
