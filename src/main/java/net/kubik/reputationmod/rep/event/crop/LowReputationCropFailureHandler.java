package net.kubik.reputationmod.rep.event.crop;

import net.kubik.reputationmod.ReputationMod;
import net.kubik.reputationmod.rep.ReputationManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.Random;

@Mod.EventBusSubscriber(modid = ReputationMod.MOD_ID)
public class LowReputationCropFailureHandler {

    private static final int LOW_REPUTATION_THRESHOLD = 50;
    private static final float MAX_FAILURE_CHANCE = 0.5f;
    private static final float MAX_YIELD_REDUCTION = 0.75f;
    private static final Random RANDOM = new Random();

    @SubscribeEvent
    public static void onCropHarvest(BlockEvent.BreakEvent event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;

        Block block = event.getState().getBlock();
        if (!(block instanceof CropBlock)) return;

        int reputation = ReputationManager.getReputation(serverLevel);
        if (reputation >= LOW_REPUTATION_THRESHOLD) return;

        float reputationFactor = (LOW_REPUTATION_THRESHOLD - reputation) / (float) LOW_REPUTATION_THRESHOLD;
        float failureChance = reputationFactor * MAX_FAILURE_CHANCE;
        float yieldReductionFactor = 1 - (reputationFactor * MAX_YIELD_REDUCTION);

        if (RANDOM.nextFloat() < failureChance) {
            event.setCanceled(true);
            serverLevel.destroyBlock(event.getPos(), false);
        } else {
            List<ItemStack> drops = Block.getDrops(event.getState(), serverLevel, event.getPos(), null);
            event.setCanceled(true);
            serverLevel.destroyBlock(event.getPos(), false);

            for (ItemStack drop : drops) {
                drop.setCount(Math.max(1, (int) (drop.getCount() * yieldReductionFactor)));
                Block.popResource(serverLevel, event.getPos(), drop);
            }
        }
    }
}