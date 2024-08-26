package net.kubik.reputationmod.rep.event;

import net.kubik.reputationmod.ReputationMod;
import net.kubik.reputationmod.rep.ReputationManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.LevelData;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Random;

@Mod.EventBusSubscriber(modid = ReputationMod.MOD_ID)
public class WeatherHandler {

    private static final Random RANDOM = new Random();

    @SubscribeEvent
    public static void onWorldTick(TickEvent.LevelTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.level instanceof ServerLevel serverLevel) {
            LevelData levelData = serverLevel.getLevelData();

            if (!levelData.isRaining() && !levelData.isThundering()) {
                int reputation = ReputationManager.getReputation(serverLevel);
                float rainChance = calculateRainChance(reputation);
                float thunderChance = calculateThunderChance(reputation);

                if (RANDOM.nextFloat() < thunderChance) {
                    int duration = calculateWeatherDuration(reputation);
                    serverLevel.setWeatherParameters(0, duration, true, true);
                } else if (RANDOM.nextFloat() < rainChance) {
                    int duration = calculateWeatherDuration(reputation);
                    serverLevel.setWeatherParameters(0, duration, true, false);
                }
            }
        }
    }

    private static float calculateRainChance(int reputation) {
        float baseChance = 1.0f / 100000;

        float multiplier = (100 - reputation) / 100.0f;
        return baseChance * (1 + multiplier);
    }

    private static float calculateThunderChance(int reputation) {
        float baseChance = 1.0f / 200000;

        float multiplier = (float) Math.pow((100 - reputation) / 100.0f, 1.5);
        return baseChance * (1 + multiplier);
    }

    private static int calculateWeatherDuration(int reputation) {
        int baseDuration = 12000 + RANDOM.nextInt(12001);

        float multiplier = (100 - reputation) / 100.0f;
        return (int) (baseDuration * (1 + multiplier));
    }
}