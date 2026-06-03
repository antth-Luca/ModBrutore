package io.github.antthluca.brutore.screens.custom;

import io.github.antthluca.brutore.Brutore;
import io.github.antthluca.brutore.screens.menu.RawInatorMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class RawInatorScreen extends AbstractContainerScreen<RawInatorMenu> {
    private static final ResourceLocation GUI_TEXTURE = ResourceLocation.fromNamespaceAndPath(Brutore.MODID,
            "textures/gui/raw_inator/raw_inator_gui.png");
    private static final ResourceLocation ARROW_TEXTURE = ResourceLocation.fromNamespaceAndPath("minecraft",
            "textures/gui/sprites/container/furnace/burn_progress.png");

    // SUPER
    @Override
    protected void renderBg(GuiGraphics guiGraphics, float v, int i, int i1) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, GUI_TEXTURE, x, y, 0, 0, imageWidth, imageHeight, 256, 256);

        renderProgressArrow(guiGraphics, x, y);
        renderLavaLevel(guiGraphics, x, y);
        renderLavaText(guiGraphics, x, y);
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        this.renderTooltip(pGuiGraphics, pMouseX, pMouseY);
    }

    // MAIN
    public RawInatorScreen(RawInatorMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    private void renderProgressArrow(GuiGraphics guiGraphics, int x, int y) {
        if (menu.isCrafting()) {
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, ARROW_TEXTURE, x + 73, y + 35, 0, 0,
                    menu.getScaledArrowProgress(), 16, 24, 16);
        }
    }

    private void renderLavaLevel(GuiGraphics guiGraphics, int x, int y) {
        int scaled = menu.getScaledLavaLevel();
        if (scaled <= 0) {
            return;
        }

        int top = y + 16 + (54 - scaled);
        guiGraphics.fill(x + 8, top, x + 22, y + 70, 0xFFFF6A00);
        guiGraphics.fill(x + 9, top + 1, x + 21, y + 69, 0xFFFFB347);
    }

    private void renderLavaText(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.drawString(this.font, "Lava: " + menu.getLavaAmount() + "/" + menu.getLavaCapacity() + " mB",
                x + 30, y + 14, 0xFFFFFFFF, false);
    }
}
