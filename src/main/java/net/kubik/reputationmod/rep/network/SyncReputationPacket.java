package net.kubik.reputationmod.rep.network;

import net.kubik.reputationmod.ReputationMod;
import net.kubik.reputationmod.rep.ReputationManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import java.util.function.Supplier;

public class SyncReputationPacket {
    private final int reputation;

    public SyncReputationPacket(int reputation) {
        this.reputation = reputation;
    }

    public SyncReputationPacket(FriendlyByteBuf buf) {
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

    public static void sendToClient(ServerPlayer player, int reputation) {
        ReputationMod.NETWORK.send(PacketDistributor.PLAYER.with(
                () -> player), new SyncReputationPacket(reputation));
    }
}