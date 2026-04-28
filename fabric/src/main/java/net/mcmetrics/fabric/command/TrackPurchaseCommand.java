package net.mcmetrics.fabric.command;

import net.mcmetrics.common.analytic.player.PlayerPurchaseAnalytic;
import net.mcmetrics.fabric.MCMetrics;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import org.incendo.cloud.annotation.specifier.Greedy;
import org.incendo.cloud.annotation.specifier.Range;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

import java.util.UUID;

/**
 * Command to track player purchases on the server. This command should be set up to be executed by the webstore
 * platform when a player makes a purchase.
 */
public class TrackPurchaseCommand {

    @Command("playertracker purchase <player_uuid> <purchase_value> <currency> <product_name>")
    @Permission("*")
    public void trackPurchase(
        final CommandSourceStack sender,
        @Argument("player_uuid") final UUID playerUuid,
        @Argument("purchase_value") @Range(min = "0") final double purchaseValue,
        @Argument("currency") final String currency,
        @Argument("product_name") @Greedy final String productName
    ) {

        if (!MCMetrics.getInstance().getHoglinLoader().isLoaded()) {
            sender.sendFailure(Component.literal("Attempted to track a purchase whilst MCMetrics is correctly configured, this analytic will be lost. Please ensure the plugin is configured correctly.").withColor(0xff5555));
            return;
        }

        MCMetrics.getInstance().getHoglin().track(new PlayerPurchaseAnalytic(
            MCMetrics.getInstance().getMcMetricsConfig().instance().id(),
            playerUuid,
            productName,
            currency,
            purchaseValue
        ));
    }

}
