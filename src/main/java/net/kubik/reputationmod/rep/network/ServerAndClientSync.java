package net.kubik.reputationmod.rep.network;

import net.kubik.reputationmod.rep.ReputationManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import net.kubik.reputationmod.ReputationMod;

import java.util.function.Supplier;

public class ServerAndClientSync {
    private final int reputation;

    public ServerAndClientSync(int reputation) {
        this.reputation = reputation;
    }

    public ServerAndClientSync(FriendlyByteBuf buf) {
        this.reputation = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(reputation);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ReputationManager.setClientReputation(reputation);
        });
        ctx.get().setPacketHandled(true);
    }

    public static void sendToClient(ServerPlayer player) {
        ServerLevel level = (ServerLevel) player.getCommandSenderWorld();
        int reputation = ReputationManager.getReputation(level);
        ReputationMod.NETWORK.send(PacketDistributor.PLAYER.with(
                () -> player), new ServerAndClientSync(reputation));
    }

    public static void sendToAllPlayers(ServerLevel level) {
        int reputation = ReputationManager.getReputation(level);
        ReputationMod.NETWORK.send(PacketDistributor.ALL.noArg(),
                new ServerAndClientSync(reputation));
    }
}