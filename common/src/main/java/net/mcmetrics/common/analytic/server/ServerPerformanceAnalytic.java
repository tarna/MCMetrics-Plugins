package net.mcmetrics.common.analytic.server;

import gg.hoglin.sdk.models.analytic.NamedAnalytic;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

/**
 * Represents all performance data available to us, including cpu, memory, storage, tps, etc
 */
@Data
public class ServerPerformanceAnalytic implements NamedAnalytic {

    private final @NotNull long cpuTime;
    private final @NotNull long memoryUsage;
    private final @NotNull long allocatedMemory;
    private final @NotNull long diskUsage;
    private final @NotNull long diskSpace;
    private final @NotNull double ticksPerSecond;
    private final @NotNull double millisecondsPerTick;

    @Override
    public @NotNull String getEventType() {
        return "server_performance";
    }
}
