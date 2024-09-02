package net.kubik.reputationmod.rep.event.entity.villager;

import net.kubik.reputationmod.rep.ReputationManager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.server.level.ServerLevel;

import java.util.HashMap;
import java.util.Map;

public class ReputationTradeAdjuster {

    private static final int MAX_PRICE = 64;
    private static final Map<MerchantOffer, ItemStack[]> originalPrices = new HashMap<>();

    public static void adjustAllOffers(ServerLevel level, MerchantOffers offers) {
        float priceMultiplier = calculatePriceMultiplier(ReputationManager.getReputation(level));
        for (MerchantOffer offer : offers) {
            adjustOffer(offer, priceMultiplier);
        }
    }

    public static void adjustOffer(MerchantOffer offer, float priceMultiplier) {
        originalPrices.putIfAbsent(offer, new ItemStack[]{offer.getBaseCostA().copy(), offer.getCostB().copy()});
        ItemStack[] original = originalPrices.get(offer);
        int newCostA = calculateNewCost(original[0].getCount(), priceMultiplier);
        int newCostB = original[1].isEmpty() ? 0 : calculateNewCost(original[1].getCount(), priceMultiplier);
        offer.getBaseCostA().setCount(newCostA);
        if (!original[1].isEmpty()) {
            offer.getCostB().setCount(newCostB);
        }
    }

    public static float calculatePriceMultiplier(int reputation) {
        return 2.0f - (reputation / 100.0f);
    }

    public static MerchantOffer adjustTradeOffer(ServerLevel level, MerchantOffer offer) {
        if (offer == null) return null;
        float priceMultiplier = calculatePriceMultiplier(ReputationManager.getReputation(level));
        int newCostA = calculateNewCost(offer.getBaseCostA().getCount(), priceMultiplier);
        int newCostB = offer.getCostB().isEmpty() ? 0 : calculateNewCost(offer.getCostB().getCount(), priceMultiplier);
        return new MerchantOffer(
                new ItemStack(offer.getBaseCostA().getItem(), newCostA),
                offer.getCostB().isEmpty() ? ItemStack.EMPTY : new ItemStack(offer.getCostB().getItem(), newCostB),
                offer.getResult().copy(),
                offer.getUses(),
                offer.getMaxUses(),
                offer.getXp(),
                offer.getPriceMultiplier(),
                offer.getDemand()
        );
    }

    private static int calculateNewCost(int originalCost, float priceMultiplier) {
        return Math.min(MAX_PRICE, Math.max(1, Math.round(originalCost * priceMultiplier)));
    }
}