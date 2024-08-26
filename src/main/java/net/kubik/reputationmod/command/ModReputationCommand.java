package net.kubik.reputationmod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.kubik.reputationmod.rep.ReputationManager;
import net.kubik.reputationmod.ReputationMod;

@Mod.EventBusSubscriber(modid = ReputationMod.MOD_ID)
public class ModReputationCommand {

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(Commands.literal("setreputation")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("value", IntegerArgumentType.integer(0, 100))
                                .executes(ModReputationCommand::execute))));
    }

    private static int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(context, "player");
        int reputationValue = IntegerArgumentType.getInteger(context, "value");

        ServerLevel level = (ServerLevel) player.getCommandSenderWorld();

        ReputationManager.setReputation(level, reputationValue);

        context.getSource().sendSuccess(() -> Component.literal("Set reputation for " + player.getName().getString() + " to " + reputationValue), true);
        return 1;
    }
}