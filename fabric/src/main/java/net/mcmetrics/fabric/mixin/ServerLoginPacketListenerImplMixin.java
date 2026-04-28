package net.mcmetrics.fabric.mixin;

import com.fasterxml.uuid.Generators;
import com.mojang.authlib.GameProfile;
import net.mcmetrics.common.platform.PlatformUtil;
import net.mcmetrics.common.player.TrackedPlayer;
import net.mcmetrics.fabric.MCMetrics;
import net.minecraft.network.Connection;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(ServerLoginPacketListenerImpl.class)
public class ServerLoginPacketListenerImplMixin {

    @Shadow
    @Final
    Connection connection;

    @Inject(method = "finishLoginAndWaitForClient", at = @At("HEAD"))
    private void interceptGameProfile(GameProfile gameProfile, CallbackInfo ci) {
        // Do the login event here :)
        final UUID uuid = gameProfile.getId();
        final TrackedPlayer player = MCMetrics.getInstance().getSessionManager().addPlayer(uuid);
        final UUID sessionId = Generators.timeBasedGenerator().generate();

        player.setSessionId(sessionId.toString());
        player.setIp(this.connection.getRemoteAddress().toString());
        player.setHostName(MCMetrics.getInstance().getHostnameStore().getAndRemove(this.connection));
        player.setClientPlatform(PlatformUtil.getPlatform(uuid));
        player.setSessionStart(System.currentTimeMillis());
    }
}
