package net.kubik.reputationmod.rep;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public class ReputationWorldData extends SavedData {
    private static final String DATA_NAME = "reputation_data";
    private int reputation = 100;

    public static ReputationWorldData load(CompoundTag tag) {
        ReputationWorldData data = new ReputationWorldData();
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
        this.reputation = Math.max(0, Math.min(100, value)); // Clamp between 0 and 100
        setDirty();
    }

    public void increaseReputation(int amount) {
        setReputation(reputation + amount);
    }

    public void decreaseReputation(int amount) {
        setReputation(reputation - amount);
    }

    public static ReputationWorldData getOrCreate(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(ReputationWorldData::load,
                ReputationWorldData::new, DATA_NAME);
    }
}