package net.mcmetrics.fabric.mixin;

import net.mcmetrics.common.analytic.player.PlayerChatAnalytic;
import net.mcmetrics.fabric.MCMetrics;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin {

    @Shadow
    public ServerPlayer player;

    @Inject(method = "handleChat", at = @At("HEAD"))
    private void onChat(ServerboundChatPacket packet, CallbackInfo ci) {
        final MCMetrics mcMetrics = MCMetrics.getInstance();

        mcMetrics.getHoglin().track(new PlayerChatAnalytic(
                mcMetrics.getMcMetricsConfig().instance().id(),
                player.getUUID(),
                packet.message(),
                false
        ));
    }
}
