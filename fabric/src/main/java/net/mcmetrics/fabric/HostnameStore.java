package net.mcmetrics.fabric;

import net.minecraft.network.Connection;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HostnameStore {

    private final Map<Connection, String> hosts = new ConcurrentHashMap<>();

    public void set(Connection connection, String hostname) {
        hosts.put(connection, hostname);
    }

    public String getAndRemove(Connection connection) {
        return hosts.remove(connection);
    }
}
