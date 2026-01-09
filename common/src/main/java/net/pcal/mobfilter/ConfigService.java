package net.pcal.mobfilter;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;


/**
 * Singleton service that manages the mod configuration, including loading the files
 * and answering whether mob spawns should be allowed.
 */
public final class ConfigService {

    // ===================================================================================
    // Singleton

    private static final class SingletonHolder {
        private static final ConfigService INSTANCE;

        static {
            INSTANCE = new ConfigService();
        }
    }

    public static ConfigService get() {
        return SingletonHolder.INSTANCE;
    }

    // ===================================================================================
    // Fields

    private static final String SIMPLE_FILENAME = "mobfilter.simple";
    private static final String JSON_FILENAME = "mobfilter.json5";
    private final Logger logger = LogManager.getLogger(ConfigService.class);
    private Config config;
    private Level logLevel = Level.INFO;
    private String configError = null;
    private boolean enabled = true;

    // ===================================================================================
    // Public methods

    /**
     * Called just as entities are being added to the world to determine whether they should
     * be allowed.
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isSpawnAllowed(final SpawnAttempt att) {
        // If mod is disabled, bypass all rules
        if (!this.enabled) return true;
        if (this.config == null) return true;
        final boolean allowSpawn = isSpawnAllowed(att, this.config.getRules());
        if (this.logLevel.isLessSpecificThan(Level.DEBUG)) { // redundant but this gets called a lot
            if (allowSpawn) {
                logger.debug(() -> "[MobFilter] ALLOW " + att.getSpawnReason() + " " + att.getEntityId() + " at [" + att.getBlockPos() + "]");
            } else {
                logger.debug(() -> "[MobFilter] DISALLOW " + att.getSpawnReason() + " " + att.getEntityId() + " at [" + att.getBlockPos() + "]");
            }
        }
        return allowSpawn;
    }

    /**
     * Write a default configuration file if none exists.
     */
    public void ensureConfigFilesExists(final Path configDirPath) {

        final File jsonConfigFile = configDirPath.resolve(JSON_FILENAME).toFile();
        final File simpleConfigFile = configDirPath.resolve(SIMPLE_FILENAME).toFile();

        if (!simpleConfigFile.exists()) {
            try (InputStream in = this.getClass().getClassLoader().getResourceAsStream("default-mobfilter.simple")) {
                if (in == null) {
                    throw new IllegalStateException("unable to load default-mobfilter.simple");
                }
                simpleConfigFile.getParentFile().mkdirs();
                java.nio.file.Files.copy(in, simpleConfigFile.toPath());
                logger.info(()->"[MobFilter] Wrote default config file to " + simpleConfigFile.getAbsolutePath());
            } catch (Exception e) {
                logger.catching(Level.ERROR, e);
                logger.error(()->"[MobFilter] Failed to write default configuration file to " + simpleConfigFile.getAbsolutePath());
            }
        }
        if (!jsonConfigFile.exists()) {
            try (InputStream in = this.getClass().getClassLoader().getResourceAsStream("default-mobfilter.json5")) {
                if (in == null) {
                    throw new IllegalStateException("unable to load default-mobfilter.json5");
                }
                jsonConfigFile.getParentFile().mkdirs();
                java.nio.file.Files.copy(in, jsonConfigFile.toPath());
                logger.info(()->"[MobFilter] Wrote default config file to " + jsonConfigFile.getAbsolutePath());
            } catch (Exception e) {
                logger.catching(Level.ERROR, e);
                logger.error(()->"[MobFilter] Failed to write default configuration file to " + jsonConfigFile.getAbsolutePath());
            }
        }
    }

    /**
     * @return the result to use for a RuleCheck that could not be evaluated (e.g., because
     * it's checking something that's inaccessible during worldgen).  Assuming true for now,
     * might want to make this configurable someday.
     */
    boolean getDefaultRuleCheckResult() {
        return true;
    }

    /**
     * @return the error that was encountered during configuration parsing, or null if none
     * was.  This is just so we can display it in the chat window for folks who don't know
     * how to find the logfile.
     */
    public String getConfigError() {
        return configError;
    }

    /**
     * Re/loads mobfilter.json5 and initializes a new FilterRuleList.
     */
    public void loadConfig(final Path configDirPath) {

        final File jsonConfigFile = configDirPath.resolve(JSON_FILENAME).toFile();
        final File simpleConfigFile = configDirPath.resolve(SIMPLE_FILENAME).toFile();
        //
        // Clean the slate
        //
        this.configError = null;
        this.config = null;
        setLogLevel(Level.INFO);
        ensureConfigFilesExists(configDirPath);
        final Config.Builder configBuilder = Config.builder();
        this.logger.info(()->"[MobFilter] Loading configuration");

        //
        // Load json config file
        //
        try {
            this.logger.debug(()->"[MobFilter] Loading config from " + jsonConfigFile.getAbsolutePath());
            try (final InputStream in = new FileInputStream(jsonConfigFile)) {
                JsonConfigLoader.loadRules(in, configBuilder);
            }
        } catch (Exception e) {
            this.configError = e.getMessage();
            logger.catching(Level.ERROR, e);
            logger.error(()->"[MobFilter] Failed to load " + jsonConfigFile.getAbsolutePath());
        }
        //
        // Load simple config file
        //
        try {
            this.logger.debug(()->"[MobFilter] Loading config from " + simpleConfigFile.getAbsolutePath());
            try (final InputStream in = new FileInputStream(simpleConfigFile)) {
                SimpleConfigLoader.loadRules(in, configBuilder);
            }
        } catch (Exception e) {
            this.configError = e.getMessage();
            logger.catching(Level.ERROR, e);
            logger.error(()->"[MobFilter] Failed to load config from " + simpleConfigFile.getAbsolutePath());
        }
        //
        // Assemble Config object
        //
        this.config = configBuilder.build();
        if (config.getLogLevel() != null) setLogLevel(config.getLogLevel());
        logger.info(()->"[MobFilter] Log level is " + logger.getLevel());
        if (this.config.getRules().isEmpty()) {
            this.logger.warn("[MobFilter] No rules configured");
        } else {
            this.logger.info(()->"[MobFilter] " + this.config.getRules().size() + " rule(s) loaded:");
            for (final Rule rule : this.config.getRules()) {
                this.logger.info(()->"[MobFilter] - " + rule.toString());
            }
        }
    }
    //
    // Initialise the isEnabled boolean for use in turning on and off the mod in-game
    //
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        logger.info("[MobFilter] Mod enabled status = {}", enabled);
    }

    // ===================================================================================
    // Private

    /**
     * @return whether the spawn attempt should be allowed according the rules in the given list.
     */
    private static boolean isSpawnAllowed(final SpawnAttempt att, final List<Rule> rules) {
        att.getLogger().trace(() -> "[MobFilter] IS_SPAWN_ALLOWED " + att);
        for (final Rule rule : rules) {
            att.getLogger().trace(() -> "[MobFilter]   RULE '" + rule.getName() + "'");
            Boolean isSpawnAllowed = rule.isSpawnAllowed(att);
            if (isSpawnAllowed != null) {
                att.getLogger().trace(() -> "[MobFilter]   SpawnAllowed: " + isSpawnAllowed);
                return isSpawnAllowed;
            }
        }
        att.getLogger().trace(() -> "[MobFilter]   RETURN true (no rules matched)");
        return true;
    }

    /**
     * Manually adjust our logger's level.  Because changing the log4j config is a PITA.
     */
    private void setLogLevel(Level logLevel) {
        Configurator.setLevel(ConfigService.class.getName(), logLevel);
        this.logLevel = logLevel;
    }
}