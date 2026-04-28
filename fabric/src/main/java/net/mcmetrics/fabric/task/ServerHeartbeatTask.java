package net.mcmetrics.fabric.task;

import gg.hoglin.sdk.Hoglin;
import net.mcmetrics.fabric.MCMetrics;

public class ServerHeartbeatTask implements Runnable {

    @Override
    public void run() {
        final Hoglin hoglin = MCMetrics.getInstance().getHoglinLoader().getHoglin();
        if (hoglin == null) {
            return;
        }

        MCMetrics.getInstance().getConnectionManager().pushPlayerCountUpdate();
        MCMetrics.getInstance().getConnectionManager().pushPerformanceUpdate();
    }

}
