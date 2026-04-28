package net.mcmetrics.fabric.listener;

import gg.hoglin.sdk.models.experiment.ExperimentData;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.mcmetrics.common.analytic.player.PlayerJoinAnalytic;
import net.mcmetrics.common.player.TrackedPlayer;
import net.mcmetrics.fabric.Listener;
import net.mcmetrics.fabric.MCMetrics;
import net.mcmetrics.fabric.experiment.ExperimentUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.stats.Stats;
import org.apache.logging.log4j.Level;

public class PlayerJoinListener implements Listener {

    private final MCMetrics mcMetrics;

    public PlayerJoinListener(final MCMetrics mcMetrics) {
        this.mcMetrics = mcMetrics;
    }

    @Override
    public void register() {
        ServerPlayConnectionEvents.JOIN.register(this::onJoin);
    }

    public void onJoin(ServerGamePacketListenerImpl packet, PacketSender sender, MinecraftServer server) {
        ServerPlayer player = packet.getPlayer();
        int playTime = player.getStats().getValue(Stats.CUSTOM.get(Stats.PLAY_TIME));
        boolean isNewPlayer = playTime == 0;

        final TrackedPlayer trackedPlayer = mcMetrics.getSessionManager().getPlayer(player.getUUID());
        if (trackedPlayer == null) {
            mcMetrics.getLogger().log(Level.FATAL, "TrackedPlayer not found for UUID: " + player.getUUID());
            return;
        }

        mcMetrics.getHoglin().track(new PlayerJoinAnalytic(
            mcMetrics.getMcMetricsConfig().instance().id(),
            trackedPlayer.getSessionId(),
            player.getUUID(),
            trackedPlayer,
            isNewPlayer
        ));

        mcMetrics.getConnectionManager().pushPlayerCountUpdate();

        // Fire experiments
        if (isNewPlayer) {
            this.mcMetrics.getHoglin().getExperiments().values().stream()
                    .filter(data -> data.getEnabled() &&
                            data.getTrigger() == ExperimentData.Trigger.FIRST_JOIN)
                    .forEach(data -> {
                        ExperimentUtil.triggerExperiment(this.mcMetrics.getHoglin(), data, player);
                    });
        }
        this.mcMetrics.getHoglin().getExperiments().values().stream()
                .filter(data -> data.getEnabled() &&
                        data.getTrigger() == ExperimentData.Trigger.JOIN)
                .forEach(data -> {
                    ExperimentUtil.triggerExperiment(this.mcMetrics.getHoglin(), data, player);
                });
    }

}
