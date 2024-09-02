package net.kubik.reputationmod;

import com.mojang.logging.LogUtils;
import net.kubik.reputationmod.gui.ReputationHudOverlay;
import net.kubik.reputationmod.rep.ReputationEventHandler;
import net.kubik.reputationmod.rep.event.block.LowReputationOreExplosionHandler;
import net.kubik.reputationmod.rep.event.crop.LowReputationCropFailureHandler;
import net.kubik.reputationmod.rep.event.entity.LowReputationMobSpawnHandler;
import net.kubik.reputationmod.rep.event.entity.villager.TradeRejectionHandler;
import net.kubik.reputationmod.rep.network.ServerAndClientSync;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
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

    private void commonSetup(final FMLCommonSetupEvent event) {
        NETWORK.registerMessage(0, ServerAndClientSync.class, ServerAndClientSync::toBytes,
                ServerAndClientSync::new, ServerAndClientSync::handle);
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(new ReputationHudOverlay());
    }
}