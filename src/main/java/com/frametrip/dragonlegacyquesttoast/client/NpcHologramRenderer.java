package com.frametrip.dragonlegacyquesttoast.client;

import com.frametrip.dragonlegacyquesttoast.entity.NpcEntity;
import com.frametrip.dragonlegacyquesttoast.entity.NpcEntityData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;

// [VFX-2]: Billboard hologram text above NPCs.
@OnlyIn(Dist.CLIENT)
public class NpcHologramRenderer {

    @SubscribeEvent
    public void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        Camera camera = event.getCamera();
        PoseStack ps  = event.getPoseStack();
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();

        List<NpcEntity> npcs = mc.level.getEntitiesOfClass(
                NpcEntity.class,
                mc.player.getBoundingBox().inflate(48));

        for (NpcEntity npc : npcs) {
            NpcEntityData data = npc.getNpcData();
            if (data == null || !data.hologramEnabled || data.hologramText.isBlank()) continue;

            String text = resolvePlaceholders(data, npc);
            float  sc   = data.hologramScale * 0.025f;

            double dx = npc.getX() - camera.getPosition().x;
            double dy = npc.getY() + data.hologramHeight - camera.getPosition().y;
            double dz = npc.getZ() - camera.getPosition().z;

            ps.pushPose();
            ps.translate(dx, dy, dz);
            // Billboard: face the camera
            ps.mulPose(Axis.YP.rotationDegrees(-camera.getYRot()));
            ps.mulPose(Axis.XP.rotationDegrees(camera.getXRot()));
            ps.scale(-sc, -sc, sc);

            int halfW = mc.font.width(text) / 2;
            mc.font.drawInBatch(text, -halfW, 0f, 0xFFFFFFFF,
                    false, ps.last().pose(), buffers,
                    Font.DisplayMode.NORMAL, 0x55000000, 0xF000F0);

            ps.popPose();
        }

        buffers.endBatch();
    }

    private static String resolvePlaceholders(NpcEntityData data, NpcEntity npc) {
        return data.hologramText
                .replace("{name}", data.displayName)
                .replace("{hp}",   String.format("%.0f", npc.getHealth()))
                .replace("{mood}", data.immersionData != null ? String.valueOf(data.immersionData.mood) : "0")
                .replace("{level}", "1");
    }
}
