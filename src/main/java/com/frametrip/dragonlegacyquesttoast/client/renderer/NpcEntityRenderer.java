package com.frametrip.dragonlegacyquesttoast.client.renderer;
 
import com.frametrip.dragonlegacyquesttoast.client.NpcSkinManager;
import com.frametrip.dragonlegacyquesttoast.entity.NpcEntity;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;
 
public class NpcEntityRenderer extends LivingEntityRenderer<NpcEntity, NpcEntityModel> {
 
    public NpcEntityRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new NpcEntityModel(ctx.bakeLayer(ModelLayers.PLAYER)), 0.5f);
    }
 
    @Override
    public ResourceLocation getTextureLocation(NpcEntity entity) {
        return NpcSkinManager.getTexture(entity.getNpcData().skinId);
    }
}
