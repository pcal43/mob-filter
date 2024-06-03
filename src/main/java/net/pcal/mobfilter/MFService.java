package net.pcal.mobfilter;


import com.google.common.collect.ImmutableList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.Vec3;
import net.pcal.mobfilter.MFConfig.ConfigurationFile;
import net.pcal.mobfilter.MFRules.BiomeCheck;
import net.pcal.mobfilter.MFRules.BlockIdCheck;
import net.pcal.mobfilter.MFRules.BlockPosCheck;
import net.pcal.mobfilter.MFRules.DimensionCheck;
import net.pcal.mobfilter.MFRules.EntityIdCheck;
import net.pcal.mobfilter.MFRules.FilterCheck;
import net.pcal.mobfilter.MFRules.FilterRule;
import net.pcal.mobfilter.MFRules.FilterRuleList;
import net.pcal.mobfilter.MFRules.LightLevelCheck;
import net.pcal.mobfilter.MFRules.SpawnGroupCheck;
import net.pcal.mobfilter.MFRules.SpawnRequest;
import net.pcal.mobfilter.MFRules.SpawnTypeCheck;
import net.pcal.mobfilter.MFRules.StringSet;
import net.pcal.mobfilter.MFRules.TimeOfDayCheck;
import net.pcal.mobfilter.MFRules.WorldNameCheck;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Consumer;

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
    final Path configFilePath = Paths.get("config", "mobfilter.yaml");
    final File configFile = configFilePath.toFile();

    // ===================================================================================
    // Public methods

    /**
     * Called by the mixins to evaluate the rules to see if a random mob spawn should be allowed.
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isSpawnAllowed(ServerLevel serverLevel,
                                  MobSpawnType spawnType, EntityType<? extends Mob> entityType,
                                  BlockPos pos) {
        if (this.ruleList == null) return true;
        return isSpawnAllowed(new SpawnRequest(serverLevel, spawnType, entityType.getCategory(), entityType, pos, this.logger));
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
                    logger.warn("[MobFilter] Invalid logLevel " + config.logLevel + " in mobfilter.yaml, using INFO");
                } else {
                    setLogLevel(configuredLevel);
                }
            }
            logger.info("[MobFilter] Log level is " + logger.getLevel());
        } catch (Exception e) {
            logger.catching(Level.ERROR, e);
            logger.error("[MobFilter] Failed to load configuration from " + configFile.getAbsolutePath());
        }
    }

    // ===================================================================================
    // Private

    /**
     * Check our rules to see if the given SpawnRequest is allowed to happen.
     */
    private boolean isSpawnAllowed(final SpawnRequest req) {
        final boolean allowSpawn = (ruleList == null || ruleList.isSpawnAllowed(req));
        if (allowSpawn) {
            logger.debug(() -> "[MobFilter] ALLOW " + req.spawnType() + " " + req.getEntityId() + " at [" + req.blockPos().toShortString() + "]");
        } else {
            logger.debug(() -> "[MobFilter] DISALLOW " + req.spawnType() + " " + req.getEntityId() + " at [" + req.blockPos().toShortString() + "]");
        }
        return allowSpawn;
    }

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
            if (when.spawnType != null && when.spawnType.length > 0) {
                final EnumSet<MobSpawnType> enumSet = EnumSet.copyOf(Arrays.asList(when.spawnType));
                checks.add(new SpawnTypeCheck(enumSet));
            }
            if (when.spawnGroup != null && when.spawnGroup.length > 0) {
                final EnumSet<MobCategory> enumSet = EnumSet.copyOf(Arrays.asList(when.spawnGroup));
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

    /**
     * Raw implementations of our mixin methods.  Putting them here just makes them more debuggable/reloadable.
     */
    @SuppressWarnings("unchecked")
    public static class MixinBodies {

        public static void EntityTypeMixin_spawn(EntityType<? extends Mob> self,
                                                 ServerLevel serverLevel,
                                                 BlockPos blockPos,
                                                 MobSpawnType mobSpawnType,
                                                 CallbackInfoReturnable<Entity> cir) {
            if (!MFService.getInstance().isSpawnAllowed(serverLevel, mobSpawnType, self, blockPos)) {
                cir.setReturnValue(null);
                cir.cancel();
            }
        }

        public static void EntityTypeMixin_spawn(EntityType<? extends Mob> self, ServerLevel serverLevel,
                                                  Consumer<?> ignored0,
                                                  BlockPos blockPos,
                                                  MobSpawnType mobSpawnType,
                                                  boolean ignored1,
                                                  boolean ignored2,
                                                  CallbackInfoReturnable<Entity> cir) {
            if (!MFService.getInstance().isSpawnAllowed(serverLevel, mobSpawnType, self, blockPos)) {
                cir.setReturnValue(null);
                cir.cancel();
            }
        }

        public static void SpawnPlacementsMixin_checkSpawnRules(EntityType<?> entityType,
                                                                ServerLevelAccessor sla,
                                                                MobSpawnType mobSpawnType,
                                                                BlockPos blockPos,
                                                                RandomSource ignored,
                                                                CallbackInfoReturnable<Boolean> cir) {
            if (!MFService.getInstance().isSpawnAllowed(sla.getLevel(), mobSpawnType, (EntityType<? extends Mob>) entityType, blockPos)) {
                cir.setReturnValue(false);
            }
        }

        public static void StructureTemplateMixin_method_17917(Rotation ignored0,
                                                               Mirror ignored1,
                                                               Vec3 ignored2,
                                                               boolean ignored3,
                                                               ServerLevelAccessor sla,
                                                               Entity entity,
                                                               CallbackInfo ci
        ) {
            if (entity instanceof Mob mob) {
                final EntityType<? extends Mob> mobType = (EntityType<? extends Mob>) mob.getType();
                if (!MFService.getInstance().isSpawnAllowed(sla.getLevel(), MobSpawnType.STRUCTURE, mobType, mob.blockPosition())) {
                    ci.cancel();
                }
            }
        }
    }
}