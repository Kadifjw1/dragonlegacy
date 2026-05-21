package com.frametrip.dragonlegacyquesttoast.client.renderer;

import com.frametrip.dragonlegacyquesttoast.entity.NpcEntity;
import com.frametrip.dragonlegacyquesttoast.entity.NpcEntityData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

import java.util.Map;

// [APP-3]: Renders accessory items attached to NPC bone positions.
public class NpcAccessoryLayer extends GeoRenderLayer<NpcEntity> {

    public NpcAccessoryLayer(GeoEntityRenderer<NpcEntity> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, NpcEntity entity, BakedGeoModel bakedModel,
                       net.minecraft.client.renderer.RenderType renderType,
                       MultiBufferSource bufferSource, com.mojang.blaze3d.vertex.VertexConsumer buffer,
                       float partialTick, int packedLight, int packedOverlay) {
        NpcEntityData data = entity.getNpcData();
        if (data.accessories == null || data.accessories.isEmpty()) return;

        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        float scale = (data.modelConfig != null) ? data.modelConfig.scale : 1.0f;

        for (Map.Entry<String, String> entry : data.accessories.entrySet()) {
            String slot   = entry.getKey();
            String itemId = entry.getValue();
            if (itemId == null || itemId.isEmpty()) continue;

            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemId));
            if (item == null) continue;
            ItemStack stack = new ItemStack(item, 1);

            poseStack.pushPose();
            applySlotTransform(poseStack, slot, scale);
            itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED,
                    packedLight, packedOverlay, poseStack, bufferSource, entity.level(), 0);
            poseStack.popPose();
        }
    }

    /** Applies a pose-stack transform for each named accessory slot. */
    private static void applySlotTransform(PoseStack ps, String slot, float scale) {
        float s = 0.4f / scale;
        switch (slot) {
            case "HEAD" -> {
                ps.translate(0, 1.6 * scale, 0);
                ps.scale(s, s, s);
            }
            case "BACK" -> {
                ps.translate(0, 1.2 * scale, -0.3 * scale);
                ps.mulPose(Axis.XP.rotationDegrees(-20f));
                ps.scale(s, s, s);
            }
            case "BELT" -> {
                ps.translate(0, 0.8 * scale, 0.2 * scale);
                ps.scale(s * 0.8f, s * 0.8f, s * 0.8f);
            }
            case "LEFT_HAND" -> {
                ps.translate(-0.35 * scale, 0.9 * scale, 0);
                ps.mulPose(Axis.ZP.rotationDegrees(90f));
                ps.scale(s, s, s);
            }
            case "RIGHT_HAND" -> {
                ps.translate(0.35 * scale, 0.9 * scale, 0);
                ps.mulPose(Axis.ZP.rotationDegrees(-90f));
                ps.scale(s, s, s);
            }
        }
    }
}
