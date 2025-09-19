package net.pcal.mobfilter;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.network.chat.Component;

import static net.minecraft.ChatFormatting.RED;

/**
 * Mob Filter is a serverside mod.  However, if we're on the client, it's
 * nice to output an error in the chat window when their config doesn't
 * load.  Otherwise, they won't have any indication that anything's wrong
 * without checking the logs, and a lot of people don't think to do that.
 */
@SuppressWarnings("unused")
public class MobFilterClientInitializer implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            if (client.player != null) {
                String configError = MobFilterService.get().getConfigError();
                if (configError != null) {
                    client.player.displayClientMessage(Component.literal(
                            "Mob Filter has been disabled due to an error in config/mobfilter.json5:\n" +
                                    configError + "\n" +
                                    "See log for details.").withStyle(RED), false);
                }
            }
        });
    }
}