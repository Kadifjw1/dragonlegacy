package com.frametrip.dragonlegacyquesttoast.client.renderer;

import com.frametrip.dragonlegacyquesttoast.client.model.NpcGeoModel;
import com.frametrip.dragonlegacyquesttoast.entity.NpcEntity;
import com.frametrip.dragonlegacyquesttoast.entity.NpcEntityData;
import com.frametrip.dragonlegacyquesttoast.server.model.NpcModelProfile;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.registries.ForgeRegistries;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class NpcGeoRenderer extends GeoEntityRenderer<NpcEntity> {

    public NpcGeoRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new NpcGeoModel());
    }

    @Override
    public void render(NpcEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        NpcEntityData data = entity.getNpcData();
        NpcModelProfile profile = (data.modelConfig != null && data.modelConfig.profile != null)
                ? data.modelConfig.profile : NpcModelProfile.PLAYER;

        if (profile != NpcModelProfile.PLAYER
                && (data.geckoModel == null || data.geckoModel.isEmpty())) {
            renderVanilla(entity, profile, entityYaw, partialTick, poseStack, bufferSource, packedLight);
            return;
        }
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    private static void renderVanilla(NpcEntity npc, NpcModelProfile profile,
            float entityYaw, float partialTick,
            PoseStack ps, MultiBufferSource buf, int light) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        EntityType<?> type = ForgeRegistries.ENTITY_TYPES
                .getValue(new ResourceLocation("minecraft", profile.id));
        if (type == null) return;

        Entity tmp = type.create(mc.level);
        if (!(tmp instanceof LivingEntity living)) return;

        living.walkAnimation.update(npc.walkAnimation.speed(partialTick), 1.0f);

        float scale = (npc.getNpcData().modelConfig != null)
                ? npc.getNpcData().modelConfig.scale : 1.0f;

        ps.pushPose();
        if (scale != 1.0f) ps.scale(scale, scale, scale);
        mc.getEntityRenderDispatcher().render(
                living, 0.0, 0.0, 0.0, entityYaw, partialTick, ps, buf, light);
        ps.popPose();
    }
}
