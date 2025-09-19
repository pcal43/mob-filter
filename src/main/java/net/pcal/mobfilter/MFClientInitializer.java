package net.pcal.mobfilter;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.network.chat.Component;

import static net.minecraft.ChatFormatting.RED;

public class MFClientInitializer implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            if (client.player != null) {
                String configError = MFService.get().getConfigError();
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