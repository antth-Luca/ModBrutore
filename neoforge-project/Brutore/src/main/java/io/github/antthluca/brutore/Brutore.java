package io.github.antthluca.brutore;

import io.github.antthluca.brutore.init.*;

import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

@Mod(Brutore.MODID)
public class Brutore {
    public static final String MODID = "brutore";

    public Brutore(IEventBus bus, ModContainer container) {
        // Init
        InitBlocks.BLOCKS.register(bus);
        InitBlockEntities.BLOCK_ENTITIES.register(bus);
        InitBlockItems.BLOCK_ITEMS.register(bus);
        InitRecipes.SERIALIZERS.register(bus);
        InitRecipes.TYPES.register(bus);
        InitMenuTypes.MENU_TYPES.register(bus);
        // Register the item to a Vanilla Creative Tab
        bus.addListener(this::addCreative);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(InitBlockItems.RAW_INATOR);
        }
    }
}
