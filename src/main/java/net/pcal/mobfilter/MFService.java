package net.pcal.mobfilter;

import com.google.common.collect.ImmutableList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.pcal.mobfilter.MFConfig.Configuration;
import net.pcal.mobfilter.MFRules.BiomeCheck;
import net.pcal.mobfilter.MFRules.BlockIdCheck;
import net.pcal.mobfilter.MFRules.BlockPosCheck;
import net.pcal.mobfilter.MFRules.CategoryCheck;
import net.pcal.mobfilter.MFRules.DimensionCheck;
import net.pcal.mobfilter.MFRules.EntityIdCheck;
import net.pcal.mobfilter.MFRules.FilterCheck;
import net.pcal.mobfilter.MFRules.FilterRule;
import net.pcal.mobfilter.MFRules.FilterRuleList;
import net.pcal.mobfilter.MFRules.LightLevelCheck;
import net.pcal.mobfilter.MFRules.MoonPhaseCheck;
import net.pcal.mobfilter.MFRules.SpawnReasonCheck;
import net.pcal.mobfilter.MFRules.SpawnRequest;
import net.pcal.mobfilter.MFRules.TimeOfDayCheck;
import net.pcal.mobfilter.MFRules.WorldNameCheck;
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


/**
 * Singleton service that orchestrates the filtering logic.
 */
public class MFService {

    // ===================================================================================
    // Singleton

    private static final class SingletonHolder {
        private static final MFService INSTANCE;

        static {
            INSTANCE = new MFService();
        }
    }

    public static MFService getInstance() {
        return SingletonHolder.INSTANCE;
    }

    // ===================================================================================
    // Fields

    private final Logger logger = LogManager.getLogger(MFService.class);
    private FilterRuleList ruleList;
    private Level logLevel = Level.INFO;
    private String configError = null;
    final File jsonConfigFile = Paths.get("config", "mobfilter.json5").toFile();
    final File yamlConfigFile = Paths.get("config", "mobfilter.yaml").toFile();

    // ===================================================================================
    // Public methods

    /**
     * Called by the mixins to evaluate the rules to see if a random mob spawn should be allowed.
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isSpawnAllowed(ServerLevel serverLevel,
                                  EntitySpawnReason spawnReason, EntityType<? extends Mob> entityType,
                                  BlockPos pos) {
        if (this.ruleList == null) return true;
        final SpawnRequest req = new SpawnRequest(serverLevel, spawnReason, entityType.getCategory(), entityType, pos, this.logger);
        final boolean allowSpawn = ruleList.isSpawnAllowed(req);
        if (this.logLevel.isLessSpecificThan(Level.DEBUG)) { // redundant but this gets called a lot
            if (allowSpawn) {
                logger.debug(() -> "[MobFilter] ALLOW " + req.spawnReason() + " " + req.getEntityId() + " at [" + req.blockPos().toShortString() + "]");
            } else {
                logger.debug(() -> "[MobFilter] DISALLOW " + req.spawnReason() + " " + req.getEntityId() + " at [" + req.blockPos().toShortString() + "]");
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
     * Re/loads mobfilter.yaml and initializes a new FiluterRuleList.
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
                this.logger.info("[MobFilter] "+ruleList.getSize()+" rule(s) loaded:");
                 for (FilterRule rule : this.ruleList.getRules()) {
                     this.logger.info("- "+rule.toString());
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

    String getConfigError() {
        return configError;
    }

    // ===================================================================================
    // Private

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
    static FilterRuleList buildRules(Configuration fromConfig) {
        requireNonNull(fromConfig);
        if (fromConfig.rules == null) return null;
        final ImmutableList.Builder<FilterRule> rulesBuilder = ImmutableList.builder();
        int i = 0;
        for (final MFConfig.Rule configRule : fromConfig.rules) {
            if (configRule == null) continue; // common with json trailing comma in list
            final ImmutableList.Builder<FilterCheck> checks = ImmutableList.builder();
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
            if (when.entityId != null) checks.add(new EntityIdCheck(IdMatcher.of(when.entityId)));
            if (when.worldName != null) checks.add(new WorldNameCheck(Matcher.of(when.worldName)));
            if (when.dimensionId != null) checks.add(new DimensionCheck(IdMatcher.of(when.dimensionId)));
            if (when.biomeId != null) checks.add(new BiomeCheck(IdMatcher.of(when.biomeId)));
            if (when.blockId != null) checks.add(new BlockIdCheck(IdMatcher.of(when.blockId)));
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
            if (when.moonPhase != null) {
                checks.add(new MoonPhaseCheck(Matcher.of(when.moonPhase)));
            }
            rulesBuilder.add(new FilterRule(ruleName, checks.build(), configRule.what));
            i++;
        }
        final List<FilterRule> rules = rulesBuilder.build();
        return rules.isEmpty() ? null : new FilterRuleList(rulesBuilder.build());
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