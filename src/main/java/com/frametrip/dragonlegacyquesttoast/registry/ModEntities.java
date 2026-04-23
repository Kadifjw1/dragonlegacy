package com.frametrip.dragonlegacyquesttoast.registry;
 
import com.frametrip.dragonlegacyquesttoast.DragonLegacyQuestToastMod;
import com.frametrip.dragonlegacyquesttoast.entity.NpcEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
 
@Mod.EventBusSubscriber(modid = DragonLegacyQuestToastMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEntities {
 
    public static final DeferredRegister<EntityType<?>> ENTITIES =
        DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, DragonLegacyQuestToastMod.MODID);
 
    public static final RegistryObject<EntityType<NpcEntity>> NPC =
        ENTITIES.register("npc", () ->
            EntityType.Builder.<NpcEntity>of(NpcEntity::new, MobCategory.MISC)
                .sized(0.6f, 1.8f)
                .clientTrackingRange(10)
                .updateInterval(3)
                .build("npc")
        );
 
    @SubscribeEvent
    public static void onAttributeCreate(EntityAttributeCreationEvent event) {
        event.put(NPC.get(), NpcEntity.createAttributes().build());
    }
}
