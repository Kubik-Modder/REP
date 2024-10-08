package net.kubik.reputationmod.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.kubik.reputationmod.ReputationMod;
import net.kubik.reputationmod.rep.ReputationManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.GameType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ReputationMod.MOD_ID, value = Dist.CLIENT)
public class ReputationHudOverlay {
    private static final Minecraft minecraft = Minecraft.getInstance();

    public static final int DEFAULT_GUI_X;
    public static final int DEFAULT_GUI_Y;

    public static int guiX;
    public static int guiY;

    static {
        // POSITION OF THE GUI
        DEFAULT_GUI_X = minecraft.getWindow().getGuiScaledWidth() / 2 + 230;
        DEFAULT_GUI_Y = minecraft.getWindow().getGuiScaledHeight() + 197;

        guiX = DEFAULT_GUI_X;
        guiY = DEFAULT_GUI_Y;
    }

    private static final ResourceLocation GUI_TEXTURE_VERY_BAD = new ResourceLocation("reputationmod", "textures/gui/rep_gui_bad.png");
    private static final ResourceLocation GUI_TEXTURE_BAD = new ResourceLocation("reputationmod", "textures/gui/rep_gui_below_neutral.png");
    private static final ResourceLocation GUI_TEXTURE_NEUTRAL = new ResourceLocation("reputationmod", "textures/gui/rep_gui_neutral.png");
    private static final ResourceLocation GUI_TEXTURE_GOOD = new ResourceLocation("reputationmod", "textures/gui/rep_gui_above_neutral.png");
    private static final ResourceLocation GUI_TEXTURE_VERY_GOOD = new ResourceLocation("reputationmod", "textures/gui/rep_gui_good.png");

    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiOverlayEvent.Pre event) {
        if (minecraft.player != null && minecraft.gameMode.getPlayerMode() != GameType.SPECTATOR
                && minecraft.gameMode.getPlayerMode() != GameType.CREATIVE) {
            renderCustomGui(event.getGuiGraphics());
        }
    }

    private static void renderCustomGui(GuiGraphics guiGraphics) {
        ResourceLocation selectedTexture;
        int reputation = ReputationManager.getClientReputation();

        if (reputation <= 20) {
            selectedTexture = GUI_TEXTURE_VERY_BAD;
        } else if (reputation <= 40) {
            selectedTexture = GUI_TEXTURE_BAD;
        } else if (reputation <= 60) {
            selectedTexture = GUI_TEXTURE_NEUTRAL;
        } else if (reputation <= 80) {
            selectedTexture = GUI_TEXTURE_GOOD;
        } else {
            selectedTexture = GUI_TEXTURE_VERY_GOOD;
        }

        RenderSystem.setShaderTexture(0, selectedTexture);

        // WIDTH AND HEIGHT (SIZE) OF THE GUI
        int textureWidth = 20;
        int textureHeight = 20;

        guiGraphics.blit(selectedTexture, guiX, guiY, 0, 0, textureWidth, textureHeight, textureWidth, textureHeight);
    }
}