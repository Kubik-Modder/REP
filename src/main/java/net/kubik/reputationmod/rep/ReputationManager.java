package net.kubik.reputationmod.rep;

import net.kubik.reputationmod.ReputationMod;
import net.kubik.reputationmod.rep.network.ServerAndClientSync;
import net.minecraft.server.level.ServerLevel;

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
    }

    public static void increaseReputation(ServerLevel level, int amount) {
        ReputationWorldData worldData = ReputationWorldData.getOrCreate(level);
        int newReputation = Math.min(100, worldData.getReputation() + amount);
        worldData.setReputation(newReputation);
        worldData.setDirty();
        ServerAndClientSync.sendToAllPlayers(level);
    }

    public static void decreaseReputation(ServerLevel level, int amount) {
        ReputationWorldData worldData = ReputationWorldData.getOrCreate(level);
        int newReputation = Math.max(0, worldData.getReputation() - amount);
        worldData.setReputation(newReputation);
        worldData.setDirty();
        ServerAndClientSync.sendToAllPlayers(level);
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