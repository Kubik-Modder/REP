package net.kubik.reputationmod.rep.event;

import net.kubik.reputationmod.rep.ReputationManager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.server.level.ServerLevel;
import net.kubik.reputationmod.ReputationMod;

import java.util.HashMap;
import java.util.Map;

public class ReputationTradeAdjuster {

    private static final int MAX_PRICE = 64;
    private static final Map<MerchantOffer, ItemStack[]> originalPrices = new HashMap<>();

    public static void adjustAllOffers(ServerLevel level, MerchantOffers offers) {
        int reputation = ReputationManager.getReputation(level);
        float priceMultiplier = calculatePriceMultiplier(reputation);
        ReputationMod.LOGGER.info("Adjusting all offers. Reputation: " + reputation + ", Price multiplier: " + priceMultiplier);

        for (MerchantOffer offer : offers) {
            adjustOffer(offer, priceMultiplier);
        }
    }

    public static void adjustOffer(MerchantOffer offer, float priceMultiplier) {
        if (!originalPrices.containsKey(offer)) {
            originalPrices.put(offer, new ItemStack[]{offer.getBaseCostA().copy(), offer.getCostB().copy()});
        }

        ItemStack[] original = originalPrices.get(offer);
        ItemStack baseCostA = original[0].copy();
        ItemStack costB = original[1].isEmpty() ? ItemStack.EMPTY : original[1].copy();

        int newCostA = Math.min(MAX_PRICE, Math.max(1, Math.round(baseCostA.getCount() * priceMultiplier)));
        int newCostB = costB.isEmpty() ? 0 : Math.min(MAX_PRICE, Math.max(1, Math.round(costB.getCount() * priceMultiplier)));

        offer.getBaseCostA().setCount(newCostA);
        if (!costB.isEmpty()) {
            offer.getCostB().setCount(newCostB);
        }

        ReputationMod.LOGGER.info("Adjusting offer. Original costs: " + baseCostA.getCount() + ", " + (costB.isEmpty() ? 0 : costB.getCount()) +
                ". New costs: " + newCostA + ", " + newCostB);
    }

    public static float calculatePriceMultiplier(int reputation) {
        return 2.0f - (reputation / 100.0f);
    }

    public static MerchantOffer adjustTradeOffer(ServerLevel level, MerchantOffer offer) {
        if (offer == null) return null;

        int reputation = ReputationManager.getReputation(level);
        float priceMultiplier = calculatePriceMultiplier(reputation);

        ItemStack baseCostA = offer.getBaseCostA().copy();
        ItemStack costB = offer.getCostB().isEmpty() ? ItemStack.EMPTY : offer.getCostB().copy();
        ItemStack result = offer.getResult().copy();

        int newCostA = Math.min(MAX_PRICE, Math.max(1, Math.round(baseCostA.getCount() * priceMultiplier)));
        int newCostB = costB.isEmpty() ? 0 : Math.min(MAX_PRICE, Math.max(1, Math.round(costB.getCount() * priceMultiplier)));

        baseCostA.setCount(newCostA);
        if (!costB.isEmpty()) {
            costB.setCount(newCostB);
        }

        return new MerchantOffer(
                baseCostA,
                costB,
                result,
                offer.getUses(),
                offer.getMaxUses(),
                offer.getXp(),
                offer.getPriceMultiplier(),
                offer.getDemand()
        );
    }
}