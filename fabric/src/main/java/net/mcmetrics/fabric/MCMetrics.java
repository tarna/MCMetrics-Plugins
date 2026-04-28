package net.mcmetrics.fabric;

import gg.hoglin.sdk.Hoglin;
import lombok.AccessLevel;
import lombok.Getter;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.mcmetrics.common.HoglinLoader;
import net.mcmetrics.common.config.TomlConfigLoader;
import net.mcmetrics.common.player.SessionManager;
import net.mcmetrics.fabric.command.ReloadCommand;
import net.mcmetrics.fabric.command.TrackPurchaseCommand;
import net.mcmetrics.fabric.config.MCMetricsFabricConfig;
import net.mcmetrics.fabric.connection.ConnectionManager;
import net.mcmetrics.fabric.listener.PlayerChatListener;
import net.mcmetrics.fabric.listener.PlayerJoinListener;
import net.mcmetrics.fabric.listener.PlayerQuitListener;
import net.mcmetrics.fabric.task.ServerHeartbeatTask;
import net.minecraft.commands.CommandSourceStack;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.incendo.cloud.SenderMapper;
import org.incendo.cloud.annotations.AnnotationParser;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.fabric.FabricServerCommandManager;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Getter
public class MCMetrics implements DedicatedServerModInitializer {

    public static final String MOD_ID = "mcmetrics";

    public final Logger logger = LogManager.getLogger(MOD_ID);

    @Getter(AccessLevel.NONE)
    private final TomlConfigLoader<MCMetricsFabricConfig> configLoader = new TomlConfigLoader<>(FabricLoader.getInstance().getConfigDir().toFile(), "mcmetrics-config.toml", "default-config.toml", MCMetricsFabricConfig.class);

    private final HoglinLoader hoglinLoader = new HoglinLoader();
    private final SessionManager sessionManager = new SessionManager();
    private final ConnectionManager connectionManager = new ConnectionManager(this);
    private final HostnameStore hostnameStore = new HostnameStore();

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    private MCMetricsFabricConfig mcMetricsConfig;

    @Getter
    private static MCMetrics instance;

    @Override
    public void onInitializeServer() {
        instance = this;

        setupCommands();
        attemptReload();

        // Probably a terrible way to register events
        List<Listener> listeners = List.of(
                new PlayerChatListener(this),
                new PlayerJoinListener(this),
                new PlayerQuitListener(this)
        );
        listeners.forEach(Listener::register);

        executor.scheduleAtFixedRate(new ServerHeartbeatTask(), 0, 1, TimeUnit.MINUTES);

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            executor.shutdown();

            new ServerHeartbeatTask().run();

            if (hoglinLoader.isLoaded()) {
                hoglinLoader.getHoglin().close();
            }
        });
    }

    public boolean attemptReload() {
        if (hoglinLoader.isLoaded()) {
            hoglinLoader.getHoglin().close();
        }

        mcMetricsConfig = configLoader.loadConfig();
        if (mcMetricsConfig == null || mcMetricsConfig.hoglin() == null || mcMetricsConfig.instance() == null) {
            getLogger().log(Level.FATAL, "Failed to reload MCMetrics config! Ensure your config.toml configuration is correct then execute /mcmetrics reload.");
            return false;
        }

        final boolean success = hoglinLoader.load(mcMetricsConfig.hoglin());
        if (!success) {
            getLogger().log(Level.FATAL, "Failed to load Hoglin! Ensure the Hoglin details in config.toml are correct then execute /mcmetrics reload.");
            return false;
        }

        return true;
    }

    private void setupCommands() {
        final FabricServerCommandManager<CommandSourceStack> manager = new FabricServerCommandManager<>(
                ExecutionCoordinator.simpleCoordinator(),
                SenderMapper.identity()
        );

        final AnnotationParser<CommandSourceStack> annotationParser = new AnnotationParser<>(manager, CommandSourceStack.class);
        annotationParser.parse(new TrackPurchaseCommand());
        annotationParser.parse(new ReloadCommand());
    }

    public Hoglin getHoglin() {
        return hoglinLoader.getHoglin();
    }
}
