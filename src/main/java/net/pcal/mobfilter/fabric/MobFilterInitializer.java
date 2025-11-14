package net.pcal.mobfilter.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.pcal.mobfilter.MobFilterService;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * THe main mod initializer.
 */
@SuppressWarnings("unused")
public class MobFilterInitializer implements ModInitializer {

    private static final Logger LOGGER = LogManager.getLogger(MobFilterInitializer.class);

    @Override
    public void onInitialize() {
        MobFilterService.get().ensureConfigFilesExist();
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            try {
                MobFilterService.get().loadConfig();
            } catch (Exception | NoClassDefFoundError e) {
                LOGGER.catching(Level.ERROR, e);
                LOGGER.error("[MobFilter] failed to initialize");
            }
        });
    }
}
