package net.kubik.reputationmod.rep;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;

public class WorldSavedData extends SavedData {
    private static final String DATA_NAME = "reputation_data";
    private int reputation = 100;

    public static WorldSavedData load(CompoundTag tag) {
        WorldSavedData data = new WorldSavedData();
        data.reputation = tag.getInt("reputation");
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putInt("reputation", reputation);
        return tag;
    }

    public int getReputation() {
        return reputation;
    }

    public void setReputation(int value) {
        reputation = Math.max(0, Math.min(100, value));
        setDirty();
    }

    public void increaseReputation(int amount) {
        setReputation(reputation + amount);
    }

    public void decreaseReputation(int amount) {
        setReputation(reputation - amount);
    }

    public static WorldSavedData getOrCreate(net.minecraft.server.level.ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(WorldSavedData::load, WorldSavedData::new, DATA_NAME);
    }
}