package net.kubik.reputationmod.rep;

import net.kubik.reputationmod.ReputationMod;
import net.kubik.reputationmod.rep.event.ReputationTradeAdjuster;
import net.kubik.reputationmod.rep.network.ServerAndClientSync;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.trading.Merchant;

import java.lang.reflect.Field;

import static net.kubik.reputationmod.rep.ReputationEventHandler.getMerchantFromMenu;

public class ReputationManager {
    private static int clientReputation = 100;
    private static final int LOW_REPUTATION_THRESHOLD = 30;

    public static int getReputation(ServerLevel level) {
        return ReputationWorldData.getOrCreate(level).getReputation();
    }

    public static void setReputation(ServerLevel level, int value) {
        ReputationWorldData worldData = ReputationWorldData.getOrCreate(level);
        worldData.setReputation(Math.max(0, Math.min(100, value)));
        worldData.setDirty();
        ServerAndClientSync.sendToAllPlayers(level);
        updateVillagerTrades(level);
    }


    public static void increaseReputation(ServerLevel level, int amount) {
        ReputationWorldData worldData = ReputationWorldData.getOrCreate(level);
        int newReputation = Math.min(100, worldData.getReputation() + amount);
        worldData.setReputation(newReputation);
        worldData.setDirty();
        ServerAndClientSync.sendToAllPlayers(level);
        updateVillagerTrades(level);
    }

    public static void decreaseReputation(ServerLevel level, int amount) {
        ReputationWorldData worldData = ReputationWorldData.getOrCreate(level);
        int newReputation = Math.max(0, worldData.getReputation() - amount);
        worldData.setReputation(newReputation);
        worldData.setDirty();
        ServerAndClientSync.sendToAllPlayers(level);
        updateVillagerTrades(level);
    }

    private static void updateVillagerTrades(ServerLevel level) {
        for (ServerPlayer serverPlayer : level.players()) {
            if (serverPlayer.containerMenu instanceof MerchantMenu merchantMenu) {
                Merchant merchant = getMerchantFromMenu(merchantMenu);
                if (merchant instanceof AbstractVillager villager) {
                    ReputationTradeAdjuster.adjustAllOffers(level, villager.getOffers());
                }
            }
        }
    }

    public static Merchant getMerchantFromMenu(MerchantMenu menu) {
        try {
            Field traderField = MerchantMenu.class.getDeclaredField("trader");
            traderField.setAccessible(true);
            return (Merchant) traderField.get(menu);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            ReputationMod.LOGGER.error("Error accessing merchant field", e);
            return null;
        }
    }

    public static boolean isLowReputation(ServerLevel level) {
        int reputation = getReputation(level);
        return reputation <= LOW_REPUTATION_THRESHOLD;
    }

    public static void updateReputation(ServerLevel level, int newReputation) {
        setReputation(level, newReputation);
        ReputationMod.LOGGER.info("Reputation updated to: " + newReputation);
    }


    public static int getClientReputation() {
        return clientReputation;
    }

    public static void setClientReputation(int reputation) {
        clientReputation = reputation;
    }

    public static float getWeatherModifier(ServerLevel level) {
        int reputation = getReputation(level);
        return (100 - reputation) / 100.0f;
    }

    public static void increaseReputationCapped(ServerLevel level, int amount) {
        int currentReputation = getReputation(level);
        int newReputation = Math.min(100, currentReputation + amount);
        setReputation(level, newReputation);
    }
}