package net.pcal.mobfilter;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static net.pcal.mobfilter.MobFilterService.MinecraftThreadType.SERVER;
import static net.pcal.mobfilter.MobFilterService.MinecraftThreadType.WORLDGEN;


/**
 * Singleton service that orchestrates the filtering logic.
 */
public final class MobFilterService {

    // ===================================================================================
    // Singleton

    private static final class SingletonHolder {
        private static final MobFilterService INSTANCE;

        static {
            INSTANCE = new MobFilterService();
        }
    }

    public static MobFilterService get() {
        return SingletonHolder.INSTANCE;
    }

    // ===================================================================================
    // Fields

    private final Logger logger = LogManager.getLogger(MobFilterService.class);
    private Config config;
    private Level logLevel = Level.INFO;
    private String configError = null;
    private final File jsonConfigFile = Paths.get("config", "mobfilter.json5").toFile();
    private final File simpleConfigFile = Paths.get("config", "mobfilter.simple").toFile();
    private final ThreadLocal<Enum<?>> spawnReason = new ThreadLocal<>();

    // ===================================================================================
    // Public methods

    /**
     * Called during entity creation so that we can remember the spawnReason for future use.
     */
    public void notifyServersideMobCreate(final Enum<?> reason) {
        //if (level.isClientSide()) return;
        //if (!(entity instanceof Mob)) return;
        if (reason == null) {
            this.logger.debug(() -> "[MobFilter] Ignoring attempt to set null spawnReason");
            return;
        } else if (this.spawnReason.get() != null && this.spawnReason.get() != reason) {
            this.logger.trace(() -> "[MobFilter] Unexpectedly changing existing spawnReason  " +
                    " from " + this.spawnReason.get() + " to " + reason);
        }
        this.spawnReason.set(reason);
    }

    /**
     * Broad categories of vanilla minecraft thready types.  We care because some kinds of filtering
     * can't be done in the worldgen thread.
     */
    public enum MinecraftThreadType {
        SERVER,
        WORLDGEN
    }

    /**
     * Called just as entities are being added to the world to determine whether they should
     * be allowed.
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isSpawnAllowed(final SpawnAttempt att) {
        if (this.config == null) return true;
        final boolean allowSpawn = isSpawnAllowed(att, this.config.getRules());
        if (this.logLevel.isLessSpecificThan(Level.DEBUG)) { // redundant but this gets called a lot
            if (allowSpawn) {
                logger.debug(() -> "[MobFilter] ALLOW " + att.getSpawnReason() + " " + att.getEntityId() + " at [" + att.getBlockPosition() + "]");
            } else {
                logger.debug(() -> "[MobFilter] DISALLOW " + att.getSpawnReason() + " " + att.getEntityId() + " at [" + att.getBlockPosition() + "]");
            }
        }
        return allowSpawn;
    }

    /**
    public boolean isSpawnAllowed(final Spawn serverLevel,
                                  final Entity entity,
                                  final MinecraftThreadType threadTypeGuess) {
        if (this.config == null) return true;
        if (serverLevel.isClientSide()) return true;
        if (!(entity instanceof Mob)) return true;
        final EntitySpawnReason reason = this.spawnReason.get();
        if (reason == null) {
            this.logger.debug(() -> "[MobFilter] No spawnReason was set for " + entity.getType());
        } else {
            this.spawnReason.remove();
        }
        final EntityType<?> entityType = entity.getType();
        final SpawnAttempt att;
        if (determineThreadType(threadTypeGuess) == SERVER) {
            att = new MainThreadSpawnAttempt(serverLevel, reason, entityType.getCategory(), entityType, entity.blockPosition(), this.logger);
        } else {
            att = new WorldgenThreadSpawnAttempt(reason, entityType.getCategory(), entityType, entity.blockPosition(), this.logger);
        }
        final boolean allowSpawn = isSpawnAllowed(att, this.config.getRules());
        if (this.logLevel.isLessSpecificThan(Level.DEBUG)) { // redundant but this gets called a lot
            if (allowSpawn) {
                logger.debug(() -> "[MobFilter] ALLOW " + att.getSpawnReason() + " " + att.getEntityId() + " at [" + att.getBlockPosition() + "]");
            } else {
                logger.debug(() -> "[MobFilter] DISALLOW " + att.getSpawnReason() + " " + att.getEntityId() + " at [" + att.getBlockPosition() + "]");
            }
        }
        return allowSpawn;
    }**/

    /**
     * Write a default configuration file if none exists.
     */
    public void ensureConfigFilesExist() {
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
    String getConfigError() {
        return configError;
    }

    /**
     * Re/loads mobfilter.json5 and initializes a new FilterRuleList.
     */
    void loadConfig(final Platform platform) {
        requireNonNull(platform);
        //
        // Clean the slate
        //
        this.configError = null;
        this.config = null;
        setLogLevel(Level.INFO);
        ensureConfigFilesExist();
        final Config.Builder configBuilder = Config.builder();
        this.logger.info(()->"[MobFilter] Loading configuration");

        //
        // Load json config file
        //
        try {
            this.logger.debug(()->"[MobFilter] Loading config from " + jsonConfigFile.getAbsolutePath());
            try (final InputStream in = new FileInputStream(jsonConfigFile)) {
                JsonConfigLoader.loadRules(in, configBuilder, platform);
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
                SimpleConfigLoader.loadRules(in, configBuilder, platform);
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
     * Determine which type of thread we're running in.  The 'guess' is based on where in the minecraft code the
     * mixin executed, and it's probably right.  But because the consequence of guessing wrong can cause the entire
     * game to deadlock, we need to err on the side of caution.
     */
    private MinecraftThreadType determineThreadType(final MinecraftThreadType threadTypeGuess) {
        final String threadName = Thread.currentThread().getName();
        final boolean threadNameLooksLikeWorldgen = threadName.contains("Worker"); // I guess?
        if (threadTypeGuess == WORLDGEN) {
            if (!threadNameLooksLikeWorldgen) {
                this.logger.debug(() -> "[MobFilter] Thread guess is " + WORLDGEN + " but the name is " + threadName);
            }
            // WORLDGEN is the least-risky case, so let's just go with the guess in any case
            return WORLDGEN;
        } else {
            // However, if we think we're in the MAIN thread, let's double check the name of the current thread.
            // It's difficult to be certain whether any of the mixin code is guaranteed to only run in the MAIN thread,
            // so let's err on the side of caution and double-check the thrad name.  This is not very robust
            // but AFAICT the vanilla code gives us no better way to check.
            if (threadNameLooksLikeWorldgen) {
                this.logger.debug(() -> "[MobFilter] Overriding guessed MAIN thread to WORLDGEN because current thread name is " + threadName);
                return WORLDGEN;
            } else {
                return SERVER;
            }
        }
    }

    /**
     * Manually adjust our logger's level.  Because changing the log4j config is a PITA.
     */
    private void setLogLevel(Level logLevel) {
        Configurator.setLevel(MobFilterService.class.getName(), logLevel);
        this.logLevel = logLevel;
    }
}