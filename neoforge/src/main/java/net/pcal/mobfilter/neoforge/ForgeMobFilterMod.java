package net.pcal.mobfilter.neoforge;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.pcal.mobfilter.ConfigService;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;

@Mod("mobfilter")
public class ForgeMobFilterMod {

    private static final Logger LOGGER = LogManager.getLogger(ForgeMobFilterMod.class);
    private static final Path CONFIG_DIR_PATH = Path.of("config");

    public ForgeMobFilterMod(IEventBus modBus) {
        LOGGER.info("[MobFilter] Mod constructor called");
        modBus.addListener(ForgeMobFilterMod::onCommonSetup);
        NeoForge.EVENT_BUS.addListener(ForgeMobFilterMod::onServerStarting);
    }

    private static void onCommonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("[MobFilter] FMLCommonSetupEvent fired - runs on both client and server");
        ConfigService.get().ensureConfigFilesExists(CONFIG_DIR_PATH);
    }

    private static void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("[MobFilter] ServerStartingEvent fired");
        ConfigService.get().ensureConfigFilesExists(CONFIG_DIR_PATH);
        try {
            ConfigService.get().loadConfig(CONFIG_DIR_PATH);
        } catch (Exception | NoClassDefFoundError e) {
            LOGGER.catching(Level.ERROR, e);
            LOGGER.error("[MobFilter] failed to initialize");
        }
    }
}

