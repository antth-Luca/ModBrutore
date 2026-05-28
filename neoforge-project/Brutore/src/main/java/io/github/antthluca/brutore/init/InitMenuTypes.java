package io.github.antthluca.brutore.init;

import io.github.antthluca.brutore.Brutore;
import io.github.antthluca.brutore.screens.menu.RawInatorMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class InitMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES     = DeferredRegister.create(
            Registries.MENU, Brutore.MODID);

    // Menu Types
    public static final DeferredHolder<MenuType<?>, MenuType<RawInatorMenu>> RAW_INATOR_MENU = MENU_TYPES.register(
            "raw_inator_menu", () -> IMenuTypeExtension.create(RawInatorMenu::new));
}
