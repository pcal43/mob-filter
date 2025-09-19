package net.pcal.mobfilter;

import com.google.common.collect.ImmutableList;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.pcal.mobfilter.MFConfig.Configuration;
import net.pcal.mobfilter.RuleCheck.BiomeCheck;
import net.pcal.mobfilter.RuleCheck.BlockIdCheck;
import net.pcal.mobfilter.RuleCheck.BlockPosCheck;
import net.pcal.mobfilter.RuleCheck.CategoryCheck;
import net.pcal.mobfilter.RuleCheck.DimensionCheck;
import net.pcal.mobfilter.RuleCheck.EntityIdCheck;
import net.pcal.mobfilter.RuleCheck.LightLevelCheck;
import net.pcal.mobfilter.RuleCheck.MoonPhaseCheck;
import net.pcal.mobfilter.RuleCheck.RandomCheck;
import net.pcal.mobfilter.RuleCheck.SkylightLevelCheck;
import net.pcal.mobfilter.RuleCheck.SpawnReasonCheck;
import net.pcal.mobfilter.RuleCheck.TimeOfDayCheck;
import net.pcal.mobfilter.RuleCheck.WeatherCheck;
import net.pcal.mobfilter.RuleCheck.WorldNameCheck;
import net.pcal.mobfilter.SpawnAttempt.MainThreadSpawnAttempt;
import net.pcal.mobfilter.SpawnAttempt.WorldgenThreadSpawnAttempt;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static net.pcal.mobfilter.MFService.MinecraftThreadType.SERVER;
import static net.pcal.mobfilter.MFService.MinecraftThreadType.WORLDGEN;


/**
 * Singleton service that orchestrates the filtering logic.
 */
public final class MFService {

    // ===================================================================================
    // Singleton

    private static final class SingletonHolder {
        private static final MFService INSTANCE;

        static {
            INSTANCE = new MFService();
        }
    }

    public static MFService get() {
        return SingletonHolder.INSTANCE;
    }

    // ===================================================================================
    // Fields

    private final Logger logger = LogManager.getLogger(MFService.class);
    private RuleList ruleList;
    private Level logLevel = Level.INFO;
    private String configError = null;
    private final File jsonConfigFile = Paths.get("config", "mobfilter.json5").toFile();
    private final ThreadLocal<EntitySpawnReason> spawnReason = new ThreadLocal<>();

    // ===================================================================================
    // Public methods

    /**
     * Called during entity creation so that we can remember the spawnReason for future use.
     */
    public void notifyEntityCreate(net.minecraft.world.level.Level level, final EntitySpawnReason reason, final Entity entity) {
        if (level.isClientSide()) return;
        if (!(entity instanceof Mob)) return;
        if (reason == null) {
            this.logger.debug(() -> "[MobFilter] Ignoring attempt to set null spawnReason for " + entity);
            return;
        } else if (this.spawnReason.get() != null && this.spawnReason.get() != reason) {
            this.logger.trace(() -> "[MobFilter] Unexpectedly changing existing spawnReason for " +
                    entity + " from " + this.spawnReason.get() + " to " + reason);
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
    public boolean isSpawnAllowed(final ServerLevel serverLevel,
                                  final Entity entity,
                                  final MinecraftThreadType threadTypeGuess) {
        if (this.ruleList == null) return true;
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
        final boolean allowSpawn = ruleList.isSpawnAllowed(att);
        if (this.logLevel.isLessSpecificThan(Level.DEBUG)) { // redundant but this gets called a lot
            if (allowSpawn) {
                logger.debug(() -> "[MobFilter] ALLOW " + att.getSpawnReason() + " " + att.getEntityId() + " at [" + att.getBlockPos().toShortString() + "]");
            } else {
                logger.debug(() -> "[MobFilter] DISALLOW " + att.getSpawnReason() + " " + att.getEntityId() + " at [" + att.getBlockPos().toShortString() + "]");
            }
        }
        return allowSpawn;
    }

    /**
     * Write a default configuration file if none exists.
     */
    public void ensureConfigExists() {
        if (!jsonConfigFile.exists()) {
            try (InputStream in = this.getClass().getClassLoader().getResourceAsStream("default-mobfilter.json5")) {
                if (in == null) {
                    throw new IllegalStateException("unable to load default-mobfilter.json5");
                }
                jsonConfigFile.getParentFile().mkdirs();
                java.nio.file.Files.copy(in, jsonConfigFile.toPath());
                logger.info("[MobFilter] Wrote default config file to " + jsonConfigFile.getAbsolutePath());
            } catch (Exception e) {
                logger.catching(Level.ERROR, e);
                logger.error("[MobFilter] Failed to write default configuration file to " + jsonConfigFile.getAbsolutePath());
            }
        }
    }

    /**
     * Re/loads mobfilter.json5 and initializes a new FilterRuleList.
     */
    public void loadConfig() {
        this.configError = null;
        this.ruleList = null;
        ensureConfigExists();
        try {
            setLogLevel(Level.INFO);
            //
            // load the config file and build the rules
            //
            final Configuration config;
            this.logger.info("[MobFilter] Loading config from " + jsonConfigFile.getAbsolutePath());
            try (final InputStream in = new FileInputStream(jsonConfigFile)) {
                config = MFConfig.loadFromJson(in);
            }
            if (config == null) {
                this.logger.warn("[MobFilter] Empty configuration");
                return;
            }
            this.ruleList = buildRules(config);
            if (this.ruleList == null) {
                this.logger.warn("[MobFilter] No rules configured in ");
            } else {
                this.logger.info("[MobFilter] " + ruleList.getSize() + " rule(s) loaded:");
                for (Rule rule : this.ruleList.getRules()) {
                    this.logger.info("- " + rule.toString());
                }
            }
            //
            // adjust logging to configured level
            //
            if (config.logLevel != null) {
                Level configuredLevel = Level.getLevel(config.logLevel);
                if (configuredLevel == null) {
                    logger.warn("[MobFilter] Invalid configured logLevel " + config.logLevel + ", using INFO");
                } else {
                    setLogLevel(configuredLevel);
                }
            }
            logger.info("[MobFilter] Log level is " + logger.getLevel());
        } catch (Exception e) {
            this.configError = e.getMessage();
            logger.catching(Level.ERROR, e);
            logger.error("[MobFilter] Failed to load configuration");
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

    // ===================================================================================
    // Private

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
        Configurator.setLevel(MFService.class.getName(), logLevel);
        this.logLevel = logLevel;
    }

    /**
     * Build the runtime rule structures from the configuration.  Returns null if the configuration contains
     * no rules.
     */
    static RuleList buildRules(Configuration fromConfig) {
        requireNonNull(fromConfig);
        if (fromConfig.rules == null) return null;
        final ImmutableList.Builder<Rule> rulesBuilder = ImmutableList.builder();
        int i = 0;
        for (final MFConfig.Rule configRule : fromConfig.rules) {
            if (configRule == null) continue; // common with json trailing comma in list
            final ImmutableList.Builder<RuleCheck> checks = ImmutableList.builder();
            final String ruleName = configRule.name != null ? configRule.name : "rule" + i;
            if (configRule.what == null) {
                throw new IllegalArgumentException("'what' must be specified on " + ruleName);
            }
            final MFConfig.When when = configRule.when;
            if (when == null) {
                throw new IllegalArgumentException("'when' must be specified on " + ruleName);
            }
            if (when.spawnReason != null && when.spawnReason.length > 0) {
                final EnumSet<EntitySpawnReason> enumSet = EnumSet.copyOf(Arrays.asList(when.spawnReason));
                checks.add(new SpawnReasonCheck(enumSet));
            } else if (when.spawnType != null && when.spawnType.length > 0) {
                // legacy support for old name 'spawnType'
                final EnumSet<EntitySpawnReason> enumSet = EnumSet.copyOf(Arrays.asList(when.spawnType));
                checks.add(new SpawnReasonCheck(enumSet));
            }
            if (when.category != null && when.category.length > 0) {
                final EnumSet<MobCategory> enumSet = EnumSet.copyOf(Arrays.asList(when.category));
                checks.add(new CategoryCheck(enumSet));
            } else if (when.spawnGroup != null && when.spawnGroup.length > 0) {
                // legacy support for old name 'spawnGroup'
                final EnumSet<MobCategory> enumSet = EnumSet.copyOf(Arrays.asList(when.spawnGroup));
                checks.add(new CategoryCheck(enumSet));
            }
            if (when.entityId != null) {
                checks.add(new EntityIdCheck(IdMatcher.of(when.entityId)));
            }
            if (when.worldName != null) {
                checks.add(new WorldNameCheck(Matcher.of(when.worldName)));
            }
            if (when.dimensionId != null) {
                checks.add(new DimensionCheck(IdMatcher.of(when.dimensionId)));
            }
            if (when.biomeId != null) {
                checks.add(new BiomeCheck(IdMatcher.of(when.biomeId)));
            }
            if (when.blockId != null) {
                checks.add(new BlockIdCheck(IdMatcher.of(when.blockId)));
            }
            if (when.blockX != null) {
                int[] range = parseRange(when.blockX);
                checks.add(new BlockPosCheck(Direction.Axis.X, range[0], range[1]));
            }
            if (when.blockY != null) {
                int[] range = parseRange(when.blockY);
                checks.add(new BlockPosCheck(Direction.Axis.Y, range[0], range[1]));
            }
            if (when.blockZ != null) {
                int[] range = parseRange(when.blockZ);
                checks.add(new BlockPosCheck(Direction.Axis.Z, range[0], range[1]));
            }
            if (when.timeOfDay != null) {
                int[] range = parseRange(when.timeOfDay);
                checks.add(new TimeOfDayCheck(range[0], range[1]));
            }
            if (when.lightLevel != null) {
                int[] range = parseRange(when.lightLevel);
                checks.add(new LightLevelCheck(range[0], range[1]));
            }
            if (when.skylightLevel != null) {
                int[] range = parseRange(when.skylightLevel);
                checks.add(new SkylightLevelCheck(range[0], range[1]));
            }
            if (when.moonPhase != null) {
                checks.add(new MoonPhaseCheck(Matcher.of(when.moonPhase)));
            }
            if (when.weather != null) {
                checks.add(new WeatherCheck(Matcher.of(when.weather)));
            }
            if (when.random != null) {
                checks.add(new RandomCheck(when.random));
            }
            rulesBuilder.add(new Rule(ruleName, checks.build(), configRule.what));
            i++;
        }
        final List<Rule> rules = rulesBuilder.build();
        return rules.isEmpty() ? null : new RuleList(rulesBuilder.build());
    }

    /**
     * Parse a two-value list into an integer range.
     */
    private static int[] parseRange(String[] configValues) {
        if (configValues.length != 2) {
            throw new IllegalArgumentException("Invalid number of values in int range: " + Arrays.toString(configValues));
        }
        int[] out = new int[2];
        out[0] = "MIN".equals(configValues[0]) ? Integer.MIN_VALUE : Integer.parseInt(configValues[0]);
        out[1] = "MAX".equals(configValues[1]) ? Integer.MAX_VALUE : Integer.parseInt(configValues[1]);
        if (out[0] > out[1]) {
            throw new IllegalArgumentException("Invalid min/max range: " + Arrays.toString(configValues));
        }
        return out;
    }
}