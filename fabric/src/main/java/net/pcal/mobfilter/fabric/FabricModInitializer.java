package net.pcal.mobfilter.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.pcal.mobfilter.ConfigService;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;

/**
 * THe main mod initializer.
 */
@SuppressWarnings("unused")
public class FabricModInitializer implements ModInitializer {

    private static final Logger LOGGER = LogManager.getLogger(FabricModInitializer.class);
    private static final Path CONFIG_DIR_PATH = Path.of("config");

    @Override
    public void onInitialize() {
        ConfigService.get().ensureConfigFilesExists(CONFIG_DIR_PATH);
        MobFilterCommands.register();
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            try {
                ConfigService.get().loadConfig(CONFIG_DIR_PATH);
            } catch (Exception | NoClassDefFoundError e) {
                LOGGER.catching(Level.ERROR, e);
                LOGGER.error("[MobFilter] failed to initialize");
            }
        });
    }
}
