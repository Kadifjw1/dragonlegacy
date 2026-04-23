package com.frametrip.dragonlegacyquesttoast.registry;
 
import com.frametrip.dragonlegacyquesttoast.DragonLegacyQuestToastMod;
import com.frametrip.dragonlegacyquesttoast.item.NpcSpawnerItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
 
public class ModItems {
 
    public static final DeferredRegister<Item> ITEMS =
        DeferredRegister.create(ForgeRegistries.ITEMS, DragonLegacyQuestToastMod.MODID);
 
    public static final RegistryObject<Item> NPC_SPAWNER =
        ITEMS.register("npc_spawner", () ->
            new NpcSpawnerItem(new Item.Properties().stacksTo(1))
        );
}
