package net.mcmetrics.fabric.listener;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.mcmetrics.common.analytic.player.PlayerQuitAnalytic;
import net.mcmetrics.common.player.TrackedPlayer;
import net.mcmetrics.fabric.Listener;
import net.mcmetrics.fabric.MCMetrics;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.apache.logging.log4j.Level;

import java.util.UUID;

public class PlayerQuitListener implements Listener {

    private final MCMetrics mcMetrics;

    public PlayerQuitListener(final MCMetrics mcMetrics) {
        this.mcMetrics = mcMetrics;
    }

    @Override
    public void register() {
        ServerPlayConnectionEvents.DISCONNECT.register(this::onQuit);
    }

    public void onQuit(ServerGamePacketListenerImpl handler, MinecraftServer server) {
        final UUID uuid = handler.getPlayer().getUUID();
        final TrackedPlayer trackedPlayer = this.mcMetrics.getSessionManager().getPlayer(uuid);
        if (trackedPlayer == null) {
            mcMetrics.getLogger().log(Level.FATAL, "TrackedPlayer not found for UUID: " + uuid);
            return;
        }

        long sessionTime = System.currentTimeMillis() - trackedPlayer.getSessionStart();

        mcMetrics.getHoglin().track(new PlayerQuitAnalytic(
            mcMetrics.getMcMetricsConfig().instance().id(),
            trackedPlayer.getSessionId(),
            uuid,
            trackedPlayer.getHostName(),
            trackedPlayer.getIp(),
            trackedPlayer.getClientPlatform(),
            sessionTime
        ));

        mcMetrics.getSessionManager().removePlayer(uuid);
        mcMetrics.getConnectionManager().pushPlayerCountUpdate();
    }

}
