package net.kubik.reputationmod.rep.event.entity;

import net.kubik.reputationmod.ReputationMod;
import net.kubik.reputationmod.rep.ReputationManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Random;

@Mod.EventBusSubscriber(modid = ReputationMod.MOD_ID)
public class LowReputationMobSpawnHandler {

    private static final int LOW_REPUTATION_THRESHOLD = 20;
    private static final int SPAWN_ATTEMPT_INTERVAL = 200;
    private static final int SPAWN_RANGE = 32;
    private static final Random RANDOM = new Random();

    private static int tickCounter = 0;

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            tickCounter++;
            if (tickCounter >= SPAWN_ATTEMPT_INTERVAL) {
                tickCounter = 0;
                for (ServerLevel level : event.getServer().getAllLevels()) {
                    if (!level.isDay()) {
                        for (ServerPlayer player : level.players()) {
                            int reputation = ReputationManager.getReputation(level);
                            if (reputation <= LOW_REPUTATION_THRESHOLD) {
                                attemptMobSpawn(level, player);
                            }
                        }
                    }
                }
            }
        }
    }

    private static void attemptMobSpawn(ServerLevel level, ServerPlayer player) {
        Vec3 spawnPos = getRandomSpawnPosition(player);
        EntityType<?> mobType = getRandomMobType();

        if (mobType != null) {
            Monster monster = (Monster) mobType.create(level);
            if (monster != null) {
                monster.moveTo(spawnPos.x, spawnPos.y, spawnPos.z);
                if (level.noCollision(monster) && level.isUnobstructed(monster)) {
                    monster.finalizeSpawn(level, level.getCurrentDifficultyAt(monster.blockPosition()), MobSpawnType.NATURAL, null, null);
                    level.addFreshEntity(monster);
                }
            }
        }
    }

    private static Vec3 getRandomSpawnPosition(ServerPlayer player) {
        double angle = RANDOM.nextDouble() * 2 * Math.PI;
        double distance = RANDOM.nextDouble() * SPAWN_RANGE;
        double x = player.getX() + Math.cos(angle) * distance;
        double z = player.getZ() + Math.sin(angle) * distance;
        double y = player.level().getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE, (int)x, (int)z);
        return new Vec3(x, y, z);
    }

    private static EntityType<?> getRandomMobType() {
        EntityType<?>[] mobTypes = {
                EntityType.ZOMBIE,
                EntityType.SKELETON,
                EntityType.SPIDER,
                EntityType.CREEPER
        };
        return mobTypes[RANDOM.nextInt(mobTypes.length)];
    }
}