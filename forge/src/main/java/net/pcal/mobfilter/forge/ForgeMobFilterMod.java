package net.pcal.mobfilter.forge;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.pcal.mobfilter.MobFilterService;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod("mobfilter")
public class ForgeMobFilterMod {

    private static final Logger LOGGER = LogManager.getLogger(ForgeMobFilterMod.class);

    static {
        LOGGER.info("\n\n\n\n\n\n[MobFilter] ForgeMobFilterMod class loaded\n\n\n\n\n\n\n");
    }

    public ForgeMobFilterMod(IEventBus modBus) {
        LOGGER.info("[MobFilter] Mod constructor called");
        modBus.addListener(ForgeMobFilterMod::onCommonSetup);
        NeoForge.EVENT_BUS.addListener(ForgeMobFilterMod::onServerStarting);
        throw new IllegalArgumentException("!!!");
    }

    private static void onCommonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("[MobFilter] FMLCommonSetupEvent fired - runs on both client and server");
        MobFilterService.get().ensureConfigFilesExist();        
    }

    private static void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("[MobFilter] ServerStartingEvent fired");
        MobFilterService.get().ensureConfigFilesExist();
        try {
            MobFilterService.get().loadConfig(ForgePlatform.get());
        } catch (Exception | NoClassDefFoundError e) {
            LOGGER.catching(Level.ERROR, e);
            LOGGER.error("[MobFilter] failed to initialize");
        }
    }
}

