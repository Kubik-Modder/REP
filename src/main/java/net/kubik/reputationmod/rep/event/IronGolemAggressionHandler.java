package net.kubik.reputationmod.rep.event;

import net.kubik.reputationmod.rep.ReputationManager;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.kubik.reputationmod.ReputationMod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = ReputationMod.MOD_ID)
public class IronGolemAggressionHandler {

    private static final int UPDATE_FREQUENCY = 20 * 10;
    private static int tickCounter = 0;
    private static final Map<UUID, NearestAttackableTargetGoal<Player>> golemTargetingGoals = new HashMap<>();

    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof IronGolem golem && event.getLevel() instanceof ServerLevel serverLevel) {
            addTargetingGoal(golem, serverLevel);
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            tickCounter++;
            if (tickCounter >= UPDATE_FREQUENCY) {
                tickCounter = 0;
                updateGolemTargeting(event.getServer().getAllLevels());
            }
        }
    }

    private static void updateGolemTargeting(Iterable<ServerLevel> levels) {
        for (ServerLevel level : levels) {
            int reputation = ReputationManager.getReputation(level);
            boolean shouldTarget = ReputationManager.isLowReputation(level);

            AABB worldBounds = new AABB(level.getWorldBorder().getMinX(), level.getMinBuildHeight(), level.getWorldBorder().getMinZ(),
                    level.getWorldBorder().getMaxX(), level.getMaxBuildHeight(), level.getWorldBorder().getMaxZ());

            for (IronGolem golem : level.getEntitiesOfClass(IronGolem.class, worldBounds)) {
                UUID golemId = golem.getUUID();
                if (shouldTarget) {
                    if (!golemTargetingGoals.containsKey(golemId)) {
                        addTargetingGoal(golem, level);
                    }
                } else {
                    removeTargetingGoal(golem);
                }
            }
        }
    }

    private static void addTargetingGoal(IronGolem golem, ServerLevel level) {
        NearestAttackableTargetGoal<Player> targetGoal = new NearestAttackableTargetGoal<>(golem, Player.class, 10, true, false,
                player -> ReputationManager.isLowReputation(level));
        golem.targetSelector.addGoal(3, targetGoal);
        golemTargetingGoals.put(golem.getUUID(), targetGoal);
    }

    private static void removeTargetingGoal(IronGolem golem) {
        UUID golemId = golem.getUUID();
        NearestAttackableTargetGoal<Player> targetGoal = golemTargetingGoals.remove(golemId);
        if (targetGoal != null) {
            golem.targetSelector.removeGoal(targetGoal);
        }
    }
}