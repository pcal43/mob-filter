package net.pcal.mobfilter.forge;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.pcal.mobfilter.MobFilterService;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The main mod initializer.
 */
@EventBusSubscriber(modid = "mobfilter")
public class ForgeModInitializer {

    private static final Logger LOGGER = LogManager.getLogger(ForgeModInitializer.class);

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        MobFilterService.get().ensureConfigFilesExist();
        try {
            MobFilterService.get().loadConfig(ForgePlatform.get());
        } catch (Exception | NoClassDefFoundError e) {
            LOGGER.catching(Level.ERROR, e);
            LOGGER.error("[MobFilter] failed to initialize");
        }
    }
}

