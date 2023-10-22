package net.pcal.mobfilter;


import com.google.common.collect.ImmutableList;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.biome.SpawnSettings;
import net.pcal.mobfilter.MFConfig.ConfigurationFile;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static net.pcal.mobfilter.MFRules.*;


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
    final Path configFilePath = Paths.get("config", "mobfilter.yaml");
    final File configFile = configFilePath.toFile();

    // ===================================================================================
    // Public methods

    /**
     * Called by the mixin to evaluate the rules to see if a mob spawn should be allowed.
     */
    public boolean isSpawnAllowed(ServerWorld sw,
                                  SpawnGroup sg,
                                  EntityType<?> et,
                                  BlockPos.Mutable pos) {
        if (this.ruleList == null) return true;
        final MFRules.SpawnRequest req = new SpawnRequest(sw, sg, et, pos, this.logger);
        final boolean allowSpawn = ruleList.isSpawnAllowed(req);
        if (allowSpawn) {
            logger.trace(() -> "[MobFilter] ALLOW " + req.getEntityId() + " at [" + req.blockPos().toShortString() + "]");
        } else {
            logger.debug(() -> "[MobFilter] DISALLOW " + req.getEntityId() + " at [" + req.blockPos().toShortString() + "]");
        }
        return allowSpawn;
    }
    public boolean isSpawnAllowed(ServerWorld sw,
                                  SpawnGroup sg,
                                  SpawnSettings.SpawnEntry se,
                                  BlockPos.Mutable pos) {
        return isSpawnAllowed(sw, sg, se.type, pos);
    }
    public boolean isSpawnAllowed(WorldView world, BlockPos pos, EntityType<?> entityType) {
        ServerWorld serverWorld;
        // I went through and looked at all the usages of the mixin that calls this and the methods that call this always use ServerWorldAccess or ServerWorld
        if (world instanceof ServerWorldAccess) {
            serverWorld = ((ServerWorldAccess) world).toServerWorld();
        } else if (world instanceof  ServerWorld) {
            serverWorld = ((ServerWorld) world);
        } else {
            // We shouldn't ever get here but I put it here just in case
            this.logger.warn("Failed to check the rules for an {} entity. Couldn't find a valid cast for {}", entityType.getName().getString(), world.getClass().getName());
            return true;
        }
        SpawnGroup spawnGroup = entityType.getSpawnGroup();

        return isSpawnAllowed(serverWorld, spawnGroup, entityType, pos.mutableCopy());
    }

    /**
     * Write a default configuration file if none exists.
     */
    public void ensureConfigExists() {
        if (!configFile.exists()) {
            try (InputStream in = this.getClass().getClassLoader().getResourceAsStream("default-mobfilter.yaml")) {
                if (in == null) {
                    throw new IllegalStateException("unable to load default-mobfilter.yaml");
                }
                configFilePath.getParent().toFile().mkdirs();
                java.nio.file.Files.copy(in, configFilePath);
                logger.info("[MobFilter] wrote default mobfilter.yaml");
            } catch (Exception e) {
                logger.catching(Level.ERROR, e);
                logger.error("[MobFilter] Failed to write default configuration file to " + configFile.getAbsolutePath());
            }
        }
    }

    /**
     * Re/loads mobfilter.yaml and initializes a new FiluterRuleList.
     */
    public void loadConfig() {
        this.ruleList = null;
        ensureConfigExists();
        try {
            setLogLevel(Level.INFO);
            //
            // load the config file and build the rules
            //
            final ConfigurationFile config;
            try (final InputStream in = new FileInputStream(configFile)) {
                config = MFConfig.load(in);
            }
            if (config == null) {
                this.logger.warn("[MobFilter] Empty configuration at " + configFile.getAbsolutePath());
                return;
            }
            this.ruleList = buildRules(config);
            if (this.ruleList == null) {
                this.logger.warn("[MobFilter] No rules configured in " + configFile.getAbsolutePath());
            }
            //
            // adjust logging to configured level
            //
            if (config.logLevel != null) {
                Level configuredLevel = Level.getLevel(config.logLevel);
                if (configuredLevel == null) {
                    logger.warn("[MobFilter] Invalid logLevel " + config.logLevel + " in mobfilter.yaml, using INFO");
                } else {
                    setLogLevel(configuredLevel);
                }
            }
            logger.info("[MobFilter] " + ruleList.getSize() + " rule(s) loaded.  Log level is " + logger.getLevel());
        } catch (Exception e) {
            logger.catching(Level.ERROR, e);
            logger.error("[MobFilter] Failed to load configuration from " + configFile.getAbsolutePath());
        }
    }

    // ===================================================================================
    // Private

    /**
     * Manually adjust our logger's level.  Because changing the log4j config is a PITA.
     */
    private void setLogLevel(Level logLevel) {
        Configurator.setLevel(MFService.class.getName(), logLevel);
    }

    /**
     * Build the runtime rule structures from the configuration.  Returns null if the configuration contains
     * no rules.
     */
    private static FilterRuleList buildRules(ConfigurationFile fromConfig) {
        requireNonNull(fromConfig);
        if (fromConfig.rules == null) return null;
        final ImmutableList.Builder<FilterRule> rulesBuilder = ImmutableList.builder();
        int i = 0;
        for (final MFConfig.Rule configRule : fromConfig.rules) {
            final ImmutableList.Builder<FilterCheck> checks = ImmutableList.builder();
            final String ruleName = configRule.name != null ? configRule.name : "rule" + i;
            if (configRule.what == null) {
                throw new IllegalArgumentException("'what' must be specified on " + ruleName);
            }
            final MFConfig.When when = configRule.when;
            if (when == null) {
                throw new IllegalArgumentException("'when' must be specified on " + ruleName);
            }
            if (when.spawnGroup != null && when.spawnGroup.length > 0) {
                final EnumSet<SpawnGroup> enumSet = EnumSet.copyOf(Arrays.asList(when.spawnGroup));
                checks.add(new SpawnGroupCheck(enumSet));
            }
            if (when.entityId != null) checks.add(new EntityIdCheck(StringSet.of(when.entityId)));
            if (when.worldName != null) checks.add(new WorldNameCheck(StringSet.of(when.worldName)));
            if (when.dimensionId != null) checks.add(new DimensionCheck(StringSet.of(when.dimensionId)));
            if (when.biomeId != null) checks.add(new BiomeCheck(StringSet.of(when.biomeId)));
            if (when.blockId != null) checks.add(new BlockIdCheck(StringSet.of(when.blockId)));
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