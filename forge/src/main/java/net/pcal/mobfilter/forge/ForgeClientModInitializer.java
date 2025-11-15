package net.pcal.mobfilter.forge;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.pcal.mobfilter.MobFilterService;

import static net.minecraft.ChatFormatting.RED;

/**
 * Mob Filter is a serverside mod.  However, if we're on the client, it's
 * nice to output an error in the chat window when their config doesn't
 * load.  Otherwise, they won't have any indication that anything's wrong
 * without checking the logs, and a lot of people don't think to do that.
 */
@EventBusSubscriber(modid = "mobfilter", value = Dist.CLIENT)
public class ForgeClientModInitializer {

    @SubscribeEvent
    public static void onPlayerLoggedIn(ClientPlayerNetworkEvent.LoggingIn event) {
        if (Minecraft.getInstance().player != null) {
            String configError = MobFilterService.get().getConfigError();
            if (configError != null) {
                Minecraft.getInstance().player.displayClientMessage(Component.literal(
                        "Mob Filter has been disabled due to an error in config/mobfilter.json5:\n" +
                                configError + "\n" +
                                "See log for details.").withStyle(RED), false);
            }
        }
    }
}

