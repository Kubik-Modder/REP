package net.kubik.reputationmod.gui.config;

import net.kubik.reputationmod.gui.ReputationHudOverlay;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class AdjustGuiPositionScreen extends Screen {

    private EditBox xPositionField;
    private EditBox yPositionField;
    private Button doneButton;

    public AdjustGuiPositionScreen() {
        super(Component.translatable("Adjust GUI Position"));
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        this.xPositionField = new EditBox(this.font, centerX - 100, centerY - 30, 200, 20, Component.literal("X Position"));
        this.xPositionField.setValue(String.valueOf(ReputationHudOverlay.guiX));

        this.yPositionField = new EditBox(this.font, centerX - 100, centerY, 200, 20, Component.literal("Y Position"));
        this.yPositionField.setValue(String.valueOf(ReputationHudOverlay.guiY));

        this.addRenderableWidget(xPositionField);
        this.addRenderableWidget(yPositionField);

        this.doneButton = Button.builder(Component.literal("Done"), button -> {
            try {
                int newX = Integer.parseInt(xPositionField.getValue());
                int newY = Integer.parseInt(yPositionField.getValue());

                ReputationHudOverlay.guiX = newX;
                ReputationHudOverlay.guiY = newY;

                this.minecraft.setScreen(null);
            } catch (NumberFormatException e) {
                this.minecraft.player.sendSystemMessage(Component.literal("Please enter valid numbers for X and Y positions."));
            }
        }).bounds(centerX - 100, centerY + 40, 200, 20).build();

        this.addRenderableWidget(doneButton);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);

    }
}