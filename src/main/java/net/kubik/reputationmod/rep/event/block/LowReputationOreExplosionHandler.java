package net.kubik.reputationmod.rep.event.block;

import net.kubik.reputationmod.ReputationMod;
import net.kubik.reputationmod.rep.ReputationManager;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Random;

@Mod.EventBusSubscriber(modid = ReputationMod.MOD_ID)
public class LowReputationOreExplosionHandler {

    private static final int LOW_REPUTATION_THRESHOLD = 20;
    private static final float BASE_EXPLOSION_CHANCE = 0.05f;
    private static final float MAX_EXPLOSION_CHANCE = 0.25f;
    private static final float EXPLOSION_POWER = 5.0f;
    private static final Random RANDOM = new Random();

    private static final TagKey<Block> CUSTOM_ORES_TAG = TagKey.create(Registries.BLOCK, new ResourceLocation(ReputationMod.MOD_ID, "explosive_ores"));

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;

        Player player = event.getPlayer();
        BlockState state = event.getState();
        BlockPos pos = event.getPos();

        if (isOre(state)) {
            int reputation = ReputationManager.getReputation(serverLevel);
            if (reputation <= LOW_REPUTATION_THRESHOLD) {
                float explosionChance = calculateExplosionChance(reputation);
                if (RANDOM.nextFloat() < explosionChance) {
                    event.setCanceled(true);
                    triggerExplosion(serverLevel, pos);
                }
            }
        }
    }

    private static boolean isOre(BlockState state) {
        return state.is(CUSTOM_ORES_TAG);
    }

    private static float calculateExplosionChance(int reputation) {
        float reputationFactor = (float)(LOW_REPUTATION_THRESHOLD - reputation) / LOW_REPUTATION_THRESHOLD;
        return BASE_EXPLOSION_CHANCE + (reputationFactor * (MAX_EXPLOSION_CHANCE - BASE_EXPLOSION_CHANCE));
    }

    private static void triggerExplosion(ServerLevel level, BlockPos pos) {
        level.explode(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, EXPLOSION_POWER, Level.ExplosionInteraction.BLOCK);
    }
}