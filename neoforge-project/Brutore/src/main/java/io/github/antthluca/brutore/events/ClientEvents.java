package io.github.antthluca.brutore.events;

import io.github.antthluca.brutore.Brutore;
import io.github.antthluca.brutore.init.InitMenuTypes;
import io.github.antthluca.brutore.screens.custom.RawInatorScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = Brutore.MODID, value = Dist.CLIENT)
public class ClientEvents {
    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(InitMenuTypes.RAW_INATOR_MENU.get(), RawInatorScreen::new);
    }
}
