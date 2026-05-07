package com.frametrip.dragonlegacyquesttoast.registry;
 
import com.frametrip.dragonlegacyquesttoast.DragonLegacyQuestToastMod;
import com.frametrip.dragonlegacyquesttoast.item.EventBookItem;
import com.frametrip.dragonlegacyquesttoast.item.GuiConfiguratorItem;
import com.frametrip.dragonlegacyquesttoast.item.LegacyCoinItem;
import com.frametrip.dragonlegacyquesttoast.item.NpcSpawnerItem;
import com.frametrip.dragonlegacyquesttoast.item.TravelerJournalItem;
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
 
    public static final RegistryObject<Item> LEGACY_COIN =
        ITEMS.register("legacy_coin", () ->
            new LegacyCoinItem(new Item.Properties().stacksTo(64))
        );

    public static final RegistryObject<Item> TRAVELER_JOURNAL =
        ITEMS.register("traveler_journal", () ->
            new TravelerJournalItem(new Item.Properties().stacksTo(1))
        );

 public static final RegistryObject<Item> EVENT_BOOK =
        ITEMS.register("event_book", () ->
            new EventBookItem(new Item.Properties().stacksTo(1))
        );

    public static final RegistryObject<Item> GUI_CONFIGURATOR =
        ITEMS.register("gui_configurator", () ->
            new GuiConfiguratorItem(new Item.Properties().stacksTo(1))
        );
}
