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
    private static final ResourceLocation ARROW_TEXTURE = ResourceLocation.fromNamespaceAndPath(Brutore.MODID,
            "textures/gui/raw_progress.png");
    private static final ResourceLocation LAVA_TEXTURE = ResourceLocation.fromNamespaceAndPath("minecraft",
            "textures/block/lava_still.png");

    protected int imageHeight = 174;

    // SUPER
    @Override
    protected void renderBg(GuiGraphics guiGraphics, float v, int i, int i1) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        renderLavaLevel(guiGraphics, x, y);

        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, GUI_TEXTURE, x, y, 0, 0, imageWidth, imageHeight, 256, 256);

        renderProgressArrow(guiGraphics, x, y);
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

        this.titleLabelY = 2;
        this.inventoryLabelY = this.imageHeight - 98;
    }

    private void renderProgressArrow(GuiGraphics guiGraphics, int x, int y) {
        if (menu.isCrafting()) {
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, ARROW_TEXTURE, x + 76, y + 43, 0, 0,
                    menu.getScaledArrowProgress(), 16, 24, 16);
        }
    }

    private void renderLavaLevel(GuiGraphics guiGraphics, int x, int y) {
        int scaled = menu.getScaledLavaLevel();
        if (scaled == 0) return;

        int top = y + 2 + (35 - scaled);

        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, LAVA_TEXTURE, x + 81, top, 0, 35 - scaled,
                15, scaled, 16, 320);
    }

    private void renderLavaText(GuiGraphics guiGraphics, int x, int y) {
        int lavaAmount = menu.getLavaAmount();
        if (lavaAmount != 0) return;

        guiGraphics.drawString(this.font, Component.translatable("block.brutore.raw_inator.empty_lava"),
                x + 12, y + 18, -12566464, false);
    }
}
