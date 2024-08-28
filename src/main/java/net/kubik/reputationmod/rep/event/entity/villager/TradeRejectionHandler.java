package net.kubik.reputationmod.rep.event.entity.villager;

import net.kubik.reputationmod.ReputationMod;
import net.kubik.reputationmod.rep.ReputationManager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ReputationMod.MOD_ID)
public class TradeRejectionHandler {

    private static final int LOW_REPUTATION_THRESHOLD = 20;

    @SubscribeEvent
    public static void onPlayerInteractWithEntity(PlayerInteractEvent.EntityInteract event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ServerLevel level = (ServerLevel) player.level();
            int reputation = ReputationManager.getReputation(level);

            if (reputation < LOW_REPUTATION_THRESHOLD) {
                if (event.getTarget() instanceof Villager villager) {
                    handleVillagerRejection(event, villager);
                } else if (event.getTarget() instanceof WanderingTrader wanderingTrader) {
                    handleWanderingTraderRejection(event, wanderingTrader);
                }
            }
        }
    }

    private static void handleVillagerRejection(PlayerInteractEvent.EntityInteract event, Villager villager) {
        event.setCanceled(true);
        villager.setUnhappyCounter(40);
        villager.playSound(SoundEvents.VILLAGER_NO, 1.0F, villager.getVoicePitch());
    }

    private static void handleWanderingTraderRejection(PlayerInteractEvent.EntityInteract event, WanderingTrader wanderingTrader) {
        event.setCanceled(true);
        wanderingTrader.playSound(SoundEvents.WANDERING_TRADER_NO, 1.0F, wanderingTrader.getVoicePitch());
    }
}