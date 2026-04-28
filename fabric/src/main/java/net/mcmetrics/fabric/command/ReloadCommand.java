package net.mcmetrics.fabric.command;

import net.mcmetrics.fabric.MCMetrics;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.CommandDescription;
import org.incendo.cloud.annotations.Permission;

/**
 * Command to reload the Hoglin configuration.
 */
public class ReloadCommand {

    @Command("mcmetrics reload")
    @CommandDescription("Reloads the MCMetrics configuration.")
    @Permission("mcmetrics.reload")
    public void reload(final CommandSourceStack sender) {
        final long now = System.currentTimeMillis();
        final boolean success = MCMetrics.getInstance().attemptReload();

        if (success) {
            sender.sendSuccess(() -> Component.literal("MCMetrics configuration reloaded successfully in " + (System.currentTimeMillis() - now) + "ms.").withColor(0x55ff55), false);
            return;
        }

        sender.sendFailure(Component.literal("Failed to reload MCMetrics configuration. Please check server console for details.").withColor(0xaa0000));
    }

}
