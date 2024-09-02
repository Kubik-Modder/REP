package net.kubik.reputationmod.rep;

import net.kubik.reputationmod.ReputationMod;
import net.kubik.reputationmod.rep.event.entity.villager.ReputationTradeAdjuster;
import net.kubik.reputationmod.rep.network.ServerAndClientSync;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.trading.Merchant;

import java.lang.reflect.Field;

public class ReputationManager {
    private static int clientReputation = 100;
    private static final int LOW_REPUTATION_THRESHOLD = 20;

    public static int getReputation(ServerLevel level) {
        return ReputationWorldData.getOrCreate(level).getReputation();
    }

    public static void setReputation(ServerLevel level, int value) {
        ReputationWorldData worldData = ReputationWorldData.getOrCreate(level);
        worldData.setReputation(value);
        ServerAndClientSync.sendToAllPlayers(level);
        updateVillagerTrades(level);
    }

    public static void increaseReputation(ServerLevel level, int amount) {
        ReputationWorldData worldData = ReputationWorldData.getOrCreate(level);
        worldData.increaseReputation(amount);
        ServerAndClientSync.sendToAllPlayers(level);
        updateVillagerTrades(level);
    }

    public static void decreaseReputation(ServerLevel level, int amount) {
        ReputationWorldData worldData = ReputationWorldData.getOrCreate(level);
        worldData.decreaseReputation(amount);
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
        return getReputation(level) <= LOW_REPUTATION_THRESHOLD;
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
        return (100 - getReputation(level)) / 100.0f;
    }

    public static void increaseReputationCapped(ServerLevel level, int amount) {
        setReputation(level, Math.min(100, getReputation(level) + amount));
    }
}