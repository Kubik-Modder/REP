package net.kubik.reputationmod.gui.config;

import net.kubik.reputationmod.gui.ReputationHudOverlay;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class AdjustGuiPositionScreen extends Screen {

    private EditBox xPositionField;
    private EditBox yPositionField;
    private Button doneButton;
    private Button resetXButton;
    private Button resetYButton;

    private String errorMessage = "";

    public AdjustGuiPositionScreen() {
        super(Component.translatable("Adjust GUI Position"));
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        int startY = centerY - 60;
        int incrementY = 50;

        this.xPositionField = new EditBox(this.font, centerX - 100, startY, 150, 20, Component.literal("X Position"));
        this.xPositionField.setValue(String.valueOf(ReputationHudOverlay.guiX));

        this.yPositionField = new EditBox(this.font, centerX - 100, startY + incrementY, 150, 20, Component.literal("Y Position"));
        this.yPositionField.setValue(String.valueOf(ReputationHudOverlay.guiY));

        this.addRenderableWidget(xPositionField);
        this.addRenderableWidget(yPositionField);

        this.resetXButton = Button.builder(Component.literal("Reset"), button -> {
            int defaultX = ReputationHudOverlay.DEFAULT_GUI_X;
            ReputationHudOverlay.guiX = defaultX;
            xPositionField.setValue(String.valueOf(defaultX));
        }).bounds(centerX + 60, startY, 40, 20).build();

        this.resetYButton = Button.builder(Component.literal("Reset"), button -> {
            int defaultY = ReputationHudOverlay.DEFAULT_GUI_Y;
            ReputationHudOverlay.guiY = defaultY;
            yPositionField.setValue(String.valueOf(defaultY));
        }).bounds(centerX + 60, startY + incrementY, 40, 20).build();

        this.addRenderableWidget(resetXButton);
        this.addRenderableWidget(resetYButton);

        this.doneButton = Button.builder(CommonComponents.GUI_DONE, button -> {
            try {
                int newX = Integer.parseInt(xPositionField.getValue());
                int newY = Integer.parseInt(yPositionField.getValue());

                ReputationHudOverlay.guiX = newX;
                ReputationHudOverlay.guiY = newY;

                this.minecraft.setScreen(null);
            } catch (NumberFormatException e) {
                errorMessage = "Please enter valid numbers for X and Y positions.";
            }
        }).bounds(centerX - 100, startY + 2 * incrementY + 20, 200, 20).build();

        this.addRenderableWidget(doneButton);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);

        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int startY = centerY - 60;
        int incrementY = 50;

        guiGraphics.drawString(this.font, "X Position:", centerX - 100, startY - 12, 0xFFFFFF);
        guiGraphics.drawString(this.font, "Y Position:", centerX - 100, startY + incrementY - 12, 0xFFFFFF);

        if (!errorMessage.isEmpty()) {
            guiGraphics.drawCenteredString(this.font, Component.literal(errorMessage), centerX, startY + 2 * incrementY + 50, 0xFF5555);
        }
    }
}