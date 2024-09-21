package net.kubik.reputationmod.gui.button;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;

public class ReputationImageButton extends Button {
    private final ResourceLocation image;
    private final int imageWidth;
    private final int imageHeight;

    public ReputationImageButton(int x, int y, int width, int height, ResourceLocation image, int imageWidth, int imageHeight, OnPress onPress) {
        super(x, y, width, height, Component.empty(), onPress, Button.DEFAULT_NARRATION);
        this.image = image;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);

        int x = this.getX() + (this.width - this.imageWidth) / 2;
        int y = this.getY() + (this.height - this.imageHeight) / 2;

        guiGraphics.blit(this.image, x, y, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);
    }
}