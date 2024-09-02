package net.kubik.reputationmod.rep;

import net.kubik.reputationmod.ReputationMod;
import net.kubik.reputationmod.rep.event.entity.villager.ReputationTradeAdjuster;
import net.kubik.reputationmod.rep.network.ServerAndClientSync;
import net.minecraft.advancements.Advancement;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.event.entity.player.AdvancementEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.lang.reflect.Field;
import java.util.List;

@Mod.EventBusSubscriber(modid = ReputationMod.MOD_ID, value = Dist.CLIENT)
public class ReputationEventHandler {

    private static int lastAdjustedReputation = -1;
    private static final int ACHIEVEMENT_REPUTATION_INCREASE = 10;

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            ServerLevel level = (ServerLevel) serverPlayer.getCommandSenderWorld();
            int reputation = ReputationManager.getReputation(level);
            ReputationMod.LOGGER.info("Player {} joined. Current reputation: {}", serverPlayer.getName().getString(), reputation);
            ServerAndClientSync.sendToClient(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onEntityHurt(LivingHurtEvent event) {
        Entity entity = event.getEntity();
        Entity source = event.getSource().getEntity();
        if (entity.level() instanceof ServerLevel level && source instanceof Player) {
            if (entity.getType().is(ReputationMod.REPUTATION_AFFECTING_ENTITIES)) {
                ReputationManager.decreaseReputation(level, 10);
                ServerAndClientSync.sendToAllPlayers(level);
            }
        }
    }

    @SubscribeEvent
    public static void onEntityDeath(LivingDeathEvent event) {
        Entity entity = event.getEntity();
        Entity source = event.getSource().getEntity();
        if (entity.level() instanceof ServerLevel level && source instanceof ServerPlayer player) {
            if (entity.getType().is(ReputationMod.REPUTATION_AFFECTING_ENTITIES)) {
                ReputationManager.decreaseReputation(level, 50);
                ServerAndClientSync.sendToAllPlayers(level);
            } else if (entity instanceof EnderDragon || entity instanceof WitherBoss) {
                handleBossKill(level, player);
            }
        }
    }

    private static void handleBossKill(ServerLevel level, ServerPlayer player) {
        ReputationManager.setReputation(level, 100);
        ServerAndClientSync.sendToAllPlayers(level);
    }

    @SubscribeEvent
    public static void onAdvancementCompleted(AdvancementEvent.AdvancementEarnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            Advancement advancement = event.getAdvancement();
            if (!advancement.getId().getPath().startsWith("recipes/")) {
                ServerLevel level = player.serverLevel();
                int newReputation = Math.min(100, ReputationManager.getReputation(level) + ACHIEVEMENT_REPUTATION_INCREASE);
                ReputationManager.setReputation(level, newReputation);
            }
        }
    }

    @SubscribeEvent
    public static void onVillagerTrades(VillagerTradesEvent event) {
        for (int i = 1; i <= 5; i++) {
            List<VillagerTrades.ItemListing> trades = event.getTrades().get(i);
            if (trades != null) {
                for (int j = 0; j < trades.size(); j++) {
                    VillagerTrades.ItemListing originalTrade = trades.get(j);
                    trades.set(j, (trader, rand) -> {
                        MerchantOffer offer = originalTrade.getOffer(trader, rand);
                        if (trader.level() instanceof ServerLevel serverLevel) {
                            return ReputationTradeAdjuster.adjustTradeOffer(serverLevel, offer);
                        }
                        return offer;
                    });
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerOpenContainer(PlayerContainerEvent.Open event) {
        if (event.getContainer() instanceof MerchantMenu merchantMenu && event.getEntity() instanceof ServerPlayer serverPlayer) {
            Merchant merchant = getMerchantFromMenu(merchantMenu);
            if (merchant instanceof AbstractVillager villager && serverPlayer.level() instanceof ServerLevel serverLevel) {
                int currentReputation = ReputationManager.getReputation(serverLevel);
                if (currentReputation != lastAdjustedReputation) {
                    ReputationTradeAdjuster.adjustAllOffers(serverLevel, villager.getOffers());
                    lastAdjustedReputation = currentReputation;
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerInteractWithEntity(PlayerInteractEvent.EntityInteract event) {
        if (event.getTarget() instanceof IronGolem golem && event.getEntity() instanceof ServerPlayer player) {
            if (event.getItemStack().getItem() == Items.IRON_INGOT && golem.getHealth() < golem.getMaxHealth()) {
                ServerLevel level = (ServerLevel) player.level();
                int newReputation = Math.min(100, ReputationManager.getReputation(level) + 5);
                ReputationManager.updateReputation(level, newReputation);
            }
        }
    }

    @SubscribeEvent
    public static void onPotionAdded(MobEffectEvent.Added event) {
        if (event.getEntity() instanceof ServerPlayer player && event.getEffectInstance().getEffect() == MobEffects.HERO_OF_THE_VILLAGE) {
            ServerLevel level = (ServerLevel) player.level();
            int newReputation = Math.min(100, ReputationManager.getReputation(level) + 50);
            ReputationManager.updateReputation(level, newReputation);
        }
    }

    @SubscribeEvent
    public static void onPlayerOpenMerchantMenu(PlayerContainerEvent.Open event) {
        Player player = event.getEntity();
        if (player.containerMenu instanceof MerchantMenu merchantMenu) {
            merchantMenu.addSlotListener(new CustomMerchantListener(player, merchantMenu));
        }
    }

    private static class CustomMerchantListener implements ContainerListener {
        private final Player player;
        private final MerchantMenu merchantMenu;
        private ItemStack previousOutput = ItemStack.EMPTY;

        public CustomMerchantListener(Player player, MerchantMenu merchantMenu) {
            this.player = player;
            this.merchantMenu = merchantMenu;
        }

        @Override
        public void slotChanged(AbstractContainerMenu menu, int slotId, ItemStack stack) {
            if (menu == merchantMenu && slotId == 2) {
                Slot outputSlot = merchantMenu.getSlot(2);
                ItemStack currentOutput = outputSlot.getItem();
                if (!previousOutput.isEmpty() && currentOutput.isEmpty()) {
                    ServerLevel level = (ServerLevel) player.getCommandSenderWorld();
                    Object trader = getTrader(merchantMenu);
                    if (trader instanceof WanderingTrader || trader instanceof AbstractVillager) {
                        ReputationManager.increaseReputation(level, 5);
                        ServerAndClientSync.sendToAllPlayers(level);
                    }
                }
                previousOutput = currentOutput.copy();
            }
        }

        @Override
        public void dataChanged(AbstractContainerMenu menu, int propertyId, int value) {
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Player player = event.player;
            if (player.containerMenu instanceof MerchantMenu merchantMenu) {
                Slot outputSlot = merchantMenu.getSlot(2);
                ItemStack currentOutput = outputSlot.getItem();
            }
        }
    }

    private static Merchant getMerchantFromMenu(MerchantMenu menu) {
        try {
            Field traderField = MerchantMenu.class.getDeclaredField("trader");
            traderField.setAccessible(true);
            return (Merchant) traderField.get(menu);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return null;
        }
    }

    private static Object getTrader(MerchantMenu menu) {
        try {
            Field traderField = MerchantMenu.class.getDeclaredField("trader");
            traderField.setAccessible(true);
            return traderField.get(menu);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }
}