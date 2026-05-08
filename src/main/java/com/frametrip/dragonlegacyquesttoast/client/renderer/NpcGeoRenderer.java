package com.frametrip.dragonlegacyquesttoast.client.renderer;

import com.frametrip.dragonlegacyquesttoast.client.model.NpcGeoModel;
import com.frametrip.dragonlegacyquesttoast.entity.NpcEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class NpcGeoRenderer extends GeoEntityRenderer<NpcEntity> {

    public NpcGeoRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new NpcGeoModel());
    }
}
