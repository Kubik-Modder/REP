package net.kubik.reputationmod.rep.event;

import net.kubik.reputationmod.ReputationMod;
import net.kubik.reputationmod.rep.ReputationManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.monster.Monster;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Random;

@Mod.EventBusSubscriber(modid = ReputationMod.MOD_ID)
public class LowReputationMobSpawnHandler {

    private static final int LOW_REPUTATION_THRESHOLD = 30;
    private static final float ADDITIONAL_SPAWN_CHANCE = 0.2f;
    private static final Random RANDOM = new Random();

    @SubscribeEvent
    public static void onCheckSpawn(MobSpawnEvent.FinalizeSpawn event) {
        if (event.getLevel() instanceof ServerLevel serverLevel && event.getEntity() instanceof Monster) {
            int reputation = ReputationManager.getReputation(serverLevel);

            if (reputation <= LOW_REPUTATION_THRESHOLD) {
                if (RANDOM.nextFloat() < ADDITIONAL_SPAWN_CHANCE) {
                    event.setResult(Event.Result.ALLOW);
                }

                if (RANDOM.nextFloat() < ADDITIONAL_SPAWN_CHANCE) {
                    Monster additionalMob = (Monster) event.getEntity().getType().create(serverLevel);
                    if (additionalMob != null) {
                        additionalMob.moveTo(event.getX(), event.getY(), event.getZ());
                        serverLevel.addFreshEntity(additionalMob);
                    }
                }
            }
        }
    }
}