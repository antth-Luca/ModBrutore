package io.github.antthluca.brutore.events;

import io.github.antthluca.brutore.Brutore;
import io.github.antthluca.brutore.datagen.BrutoreBlockTagsProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = Brutore.MODID, bus = EventBusSubscriber.Bus.MOD)
public class BrutoreDataGeneration {
    @SubscribeEvent
    public static void gatherClientData(GatherDataEvent event) {
        DataGenerator gen = event.getGenerator();
        PackOutput out = gen.getPackOutput();
        var lookup = event.getLookupProvider();

        gen.addProvider(true, new BrutoreBlockTagsProvider(
                out,
                lookup,
                Brutore.MODID,
                event.getExistingFileHelper()
        ));
    }
}
