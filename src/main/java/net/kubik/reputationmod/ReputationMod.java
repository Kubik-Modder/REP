package net.kubik.reputationmod;

import com.mojang.logging.LogUtils;
import net.kubik.reputationmod.gui.config.AdjustGuiPositionScreen;
import net.kubik.reputationmod.gui.button.ReputationImageButton;
import net.kubik.reputationmod.gui.ReputationHudOverlay;
import net.kubik.reputationmod.rep.ReputationEventHandler;
import net.kubik.reputationmod.rep.event.block.LowReputationOreExplosionHandler;
import net.kubik.reputationmod.rep.event.crop.LowReputationCropFailureHandler;
import net.kubik.reputationmod.rep.event.entity.LowReputationMobSpawnHandler;
import net.kubik.reputationmod.rep.event.entity.villager.TradeRejectionHandler;
import net.kubik.reputationmod.rep.network.ServerAndClientSync;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import org.slf4j.Logger;

@Mod(ReputationMod.MOD_ID)
public class ReputationMod {
    public static final String MOD_ID = "reputationmod";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final SimpleChannel NETWORK;

    public static final TagKey<EntityType<?>> REPUTATION_AFFECTING_ENTITIES = TagKey.create(Registries.ENTITY_TYPE,
            new ResourceLocation(MOD_ID, "reputation_affecting_entities"));

    private static final String PROTOCOL_VERSION = "1";

    static {
        NETWORK = NetworkRegistry.newSimpleChannel(
                new ResourceLocation(MOD_ID, "main"),
                () -> PROTOCOL_VERSION,
                PROTOCOL_VERSION::equals,
                PROTOCOL_VERSION::equals
        );
    }

    public ReputationMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new ReputationEventHandler());
        MinecraftForge.EVENT_BUS.register(LowReputationMobSpawnHandler.class);
        MinecraftForge.EVENT_BUS.register(TradeRejectionHandler.class);
        MinecraftForge.EVENT_BUS.register(LowReputationCropFailureHandler.class);
        MinecraftForge.EVENT_BUS.register(LowReputationOreExplosionHandler.class);
    }

    @SubscribeEvent
    public void onScreenInit(ScreenEvent.Init.Post event) {
        if (event.getScreen() instanceof PauseScreen) {
            int x = (event.getScreen().width / 2) - 125;
            int y = (event.getScreen().height / 4) + 98;
            int width = 20;
            int height = 20;

            ResourceLocation buttonImage = new ResourceLocation("reputationmod", "textures/gui/rep_gui_pause_screen.png");
            int imageWidth = 16;
            int imageHeight = 16;

            ReputationImageButton imageButton = new ReputationImageButton(x, y, width, height, buttonImage, imageWidth, imageHeight, button -> {
                Minecraft.getInstance().setScreen(new AdjustGuiPositionScreen());
            });

            event.addListener(imageButton);
        }
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        NETWORK.registerMessage(0, ServerAndClientSync.class, ServerAndClientSync::toBytes,
                ServerAndClientSync::new, ServerAndClientSync::handle);
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(new ReputationHudOverlay());
    }
}