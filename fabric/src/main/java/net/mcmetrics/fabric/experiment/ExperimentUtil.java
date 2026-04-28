package net.mcmetrics.fabric.experiment;

import gg.hoglin.sdk.Hoglin;
import gg.hoglin.sdk.models.experiment.ExperimentData;
import gg.hoglin.sdk.models.experiment.ExperimentVariant;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class ExperimentUtil {

    /**
     * Triggers the experiment to run for a player
     *
     * @param hoglin Hoglin instance
     * @param data cached experiment data object
     * @param player the player to run the experiment on
     */
    public static void triggerExperiment(Hoglin hoglin, ExperimentData data, ServerPlayer player) {
        boolean exposed = hoglin.evaluateExperiment(data.getExperimentId(), player.getUUID());
        ExperimentVariant variant;
        String payload;
        if (exposed) {
            variant = data.getVariants().get(ExperimentVariant.Variant.EXPOSED);
            payload = variant.formatPayload(player.getName().getString(), player.getUUID(), ExperimentVariant.Variant.EXPOSED);
        } else {
            variant = data.getVariants().get(ExperimentVariant.Variant.CONTROL);
            payload = variant.formatPayload(player.getName().getString(), player.getUUID(), ExperimentVariant.Variant.CONTROL);
        }
        triggerAction(variant.getAction(), player, payload);
    }

    /**
     * Triggers the specific action associated with an experiment variant
     *
     * @param action the specific action
     * @param player the player that the action is being performed on
     * @param payload the payload used by the action
     */
    public static void triggerAction(ExperimentVariant.Action action, ServerPlayer player, String payload) {

        switch (action) {
            case RUN_CONSOLE_COMMAND -> player.getServer().getCommands().performPrefixedCommand(
                    player.getServer().createCommandSourceStack(),
                    payload
            );
            case RUN_COMMAND_AS_PLAYER -> player.getServer().getCommands().performPrefixedCommand(
                    player.createCommandSourceStack(),
                    payload
            );
            case SEND_MESSAGE -> player.sendSystemMessage(Component.literal(payload));
        }
    }
}
