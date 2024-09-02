package net.kubik.reputationmod.rep.event.entity.iron_golem;

import net.kubik.reputationmod.ReputationMod;
import net.kubik.reputationmod.rep.ReputationManager;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ReputationMod.MOD_ID)
public class IronGolemAggressionHandler {
    private static final int REPUTATION_THRESHOLD = 20;

    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof IronGolem golem && event.getLevel() instanceof ServerLevel serverLevel) {
            golem.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(golem, Player.class, 10, true, false,
                    player -> ReputationManager.getReputation(serverLevel) <= REPUTATION_THRESHOLD));
        }
    }
}