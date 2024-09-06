package net.pcal.mobfilter;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

@SuppressWarnings("unused")
public class MFInitializer implements ModInitializer {

    private static final Logger LOGGER = LogManager.getLogger(MFInitializer.class);

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            try {
                MFService.getInstance().ensureConfigExists();
                MFService.getInstance().loadConfig();
            } catch (IOException ioe) {
                throw new RuntimeException("Failed to load configuration.  See log for details.", ioe);
            }
        });
    }
}
