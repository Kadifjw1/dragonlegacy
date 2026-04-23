package com.frametrip.dragonlegacyquesttoast.registry;
 
import com.frametrip.dragonlegacyquesttoast.DragonLegacyQuestToastMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
 
public class ModCreativeTabs {
 
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, DragonLegacyQuestToastMod.MODID);
 
    public static final RegistryObject<CreativeModeTab> MOD_TAB =
        CREATIVE_TABS.register("main", () ->
            CreativeModeTab.builder()
                .title(Component.literal("Dragon Legacy"))
                .icon(() -> ModItems.NPC_SPAWNER.get().getDefaultInstance())
                .displayItems((params, output) -> {
                    output.accept(ModItems.NPC_SPAWNER.get());
                })
                .build()
        );
}
