package net.pcal.mobfilter;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.SpawnSettings;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.level.ServerWorldProperties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

import static java.util.Objects.requireNonNull;
import static net.pcal.mobfilter.MFRules.FilterRuleAction.ALLOW_SPAWN;

@SuppressWarnings("SpellCheckingInspection")
abstract class MFRules {

    private static final Logger LOGGER = LogManager.getLogger(MFRules.class);

    public static class FilterRuleList {
        private final List<FilterRule> rules;

        FilterRuleList(List<FilterRule> rules) {
            this.rules = requireNonNull(rules);
        }

        public boolean isSpawnAllowed(SpawnRequest request) {
            request.logger().trace(() -> "[MobFilter] IS_SPAWN_ALLOWED " + request);
            for (FilterRule rule : rules) {
                request.logger().trace(() -> "[MobFilter]   RULE '" + rule.ruleName + "'");
                Boolean isSpawnAllowed = rule.isSpawnAllowed(request);
                if (isSpawnAllowed != null) {
                    request.logger().trace(() -> "[MobFilter]   RETURN " + isSpawnAllowed);
                    return isSpawnAllowed;
                }
            }
            request.logger().trace(() -> "[MobFilter]   RETURN true (no rules matched)");
            return true;
        }

        public int getSize() {
            return this.rules.size();
        }

    }


    public enum FilterRuleAction {
        ALLOW_SPAWN,
        DISALLOW_SPAWN
    }

    /**
     * One rule to be evaluated in the filter chain.
     */
    record FilterRule(String ruleName,
                      Collection<FilterCheck> checks,
                      FilterRuleAction action) {

        FilterRule {
            requireNonNull(ruleName);
            requireNonNull(checks);
            requireNonNull(action);
        }

        /**
         * Return whether the requested spawn should be allowed, or null if we don't have any opinion (i.e., because
         * the rule didn't match).
         */
        public Boolean isSpawnAllowed(SpawnRequest request) {
            for (final FilterCheck check : checks) {
                if (!check.isMatch(request)) return null;
            }
            return this.action == ALLOW_SPAWN;
        }

    }

    /**
     * Encapsualtes the parameters in a minecraft call to 'canSpawn'.
     */
    record SpawnRequest(ServerWorld serverWorld,
                        SpawnGroup spawnGroup,
                        StructureAccessor structureAccessor,
                        ChunkGenerator chunkGenerator,
                        SpawnSettings.SpawnEntry spawnEntry,
                        BlockPos blockPos,
                        double squaredDistance,
                        Logger logger) {

        /**
         * Return the entity id of the mob that is going to spawn.
         */
        public String getEntityId() {
            return String.valueOf(Registry.ENTITY_TYPE.getId(this.spawnEntry.type)); // FIXME is this right?
        }

        /**
         * Return the name of the world that the spawn is happening in.
         */
        public String getWorldName() {
            final ServerWorldProperties swp;
            try {
                swp = (ServerWorldProperties) this.serverWorld.getLevelProperties();
            } catch (ClassCastException cce) {
                LOGGER.warn("[MobFilter] serverWorld.getLevelProperties() is unexpected class: " +
                        this.serverWorld.getLevelProperties().getClass().getName());
                return null;
            }
            return swp.getLevelName();
        }

        /**
         * Return the id of the dimension that the spawn is happening in.
         */
        public String getDimensionId() {
            // FIXME how do you get the Identifier for a DimensionType?  Don't understand action 'effects' is
            // but it seems to work.  May break custom dimensions.
            return this.serverWorld.getDimension().getEffects().toString();
        }

        /**
         * Return the id of the biome that the spawn is happening in.
         */
        public String getBiomeId() {
            final Biome biome = this.serverWorld().getBiome(this.blockPos());
            return String.valueOf(BuiltinRegistries.BIOME.getId(biome)); // FIXME is this right?
        }

        /**
         * Return the id of the block that the spawn is happening on.
         */
        public String getBlockId() {
            final BlockEntity be = serverWorld.getBlockEntity(this.blockPos); // FIXME do we need to check at y+1?
            return String.valueOf(Registry.BLOCK_ENTITY_TYPE.getId(be.getType()));
        }
    }

    /**
     * Encapsulates a single condition check of a rule.
     */
    interface FilterCheck {
        boolean isMatch(SpawnRequest spawn);
    }

    // TODO seems like we could optimize by completely dropping rules with a worldName check that won't match
    // the current running world
    record WorldNameCheck(StringSet worldNames) implements FilterCheck {
        @Override
        public boolean isMatch(SpawnRequest req) {
            boolean isMatch = worldNames.contains(req.getWorldName());
            req.logger().trace(() -> "[MobFilter]     WorldNameCheck: " + req.getWorldName() + " in " + worldNames + " " + isMatch);
            return isMatch;
        }
    }

    record DimensionCheck(StringSet dimensionIds) implements FilterCheck {
        @Override
        public boolean isMatch(SpawnRequest req) {
            boolean isMatch = this.dimensionIds.contains(req.getDimensionId());
            req.logger().trace(() -> "[MobFilter]     DimensionCheck " + req.getDimensionId() + " in " + dimensionIds + " " + isMatch);
            return isMatch;
        }
    }

    record BiomeCheck(StringSet biomeIds) implements FilterCheck {
        @Override
        public boolean isMatch(SpawnRequest req) {
            boolean isMatch = this.biomeIds.contains(req.getBiomeId());
            req.logger().trace(() -> "[MobFilter]     BiomeCheck " + req.getBiomeId() + " in " + biomeIds+" "+isMatch);
            return isMatch;
        }
    }
    record SpawnGroupCheck(EnumSet<SpawnGroup> groups) implements FilterCheck {
        @Override
        public boolean isMatch(SpawnRequest req) {
            boolean isMatch = this.groups.contains(req.spawnGroup);
            req.logger().trace(() -> "[MobFilter]     SpawnGroupCheck: " + this.groups + " " + req.spawnGroup + " " + this.groups.contains(req.spawnGroup) + " " + isMatch);
            return isMatch;
        }
    }

    record EntityIdCheck(StringSet entityIds) implements FilterCheck {
        @Override
        public boolean isMatch(SpawnRequest req) {
            boolean isMatch = this.entityIds.contains(req.getEntityId());
            req.logger().trace(() -> "[MobFilter]     EntityNameCheck " + req.getEntityId() + " in " + entityIds+" "+isMatch);
            return isMatch;
        }
    }

    record BlockIdCheck(StringSet blockIds) implements FilterCheck {
        @Override
        public boolean isMatch(SpawnRequest req) {
            boolean isMatch = this.blockIds.contains(req.getBlockId());
            req.logger().trace(() -> "[MobFilter]     BlockIdCheck " + req.getEntityId() + " in " + blockIds+" "+isMatch);
            return isMatch;
        }
    }

    record BlockPosCheck(Direction.Axis axis, int min, int max) implements FilterCheck {
        @Override
        public boolean isMatch(SpawnRequest req) {
            int val = req.blockPos.getComponentAlongAxis(this.axis);
            boolean isMatch = min <= val && val <= max;
            req.logger().trace(() -> "[MobFilter]     BlockPosCheck " + axis + " " + min + " <= " + val + " <= " + max+ " "+isMatch);
            return isMatch;
        }
    }


    record LightLevelCheck(int min, int max) implements FilterCheck {
        @Override
        public boolean isMatch(SpawnRequest req) {
            int val = req.serverWorld().getLightLevel(req.blockPos);
            boolean isMatch = min <= val && val <= max;
            req.logger().trace(() -> "[MobFilter]     LightLevelCheck " + min + " <= " + val + " <= " + max+ " " +isMatch);
            return isMatch;

        }
    }

    record TimeOfDayCheck(long min, long max) implements FilterCheck {
        @Override
        public boolean isMatch(SpawnRequest req) {
            long val = req.serverWorld.getTimeOfDay();
            boolean isMatch = min <= val && val <= max;
            req.logger().trace(() -> "[MobFilter]     TimeOfDayCheck " + min + " <= " + val + " <= " + max+" "+isMatch);
            return isMatch;
        }
    }

    /**
     * An immutable set of strings with membership testing.  This gets used a lot and may be
     * in need of optimization.
     */
    @SuppressWarnings("ClassCanBeRecord")
    public static class StringSet {
        private final String[] strings;

        public static StringSet of(String[] strings) {
            return new StringSet(strings);
        }

        private StringSet(String[] strings) {
            this.strings = requireNonNull(strings);
        }

        public boolean contains(String value) {
            for (String a : this.strings) if (Objects.equals(a, value)) return true;
            return false;
        }

        public String toString() {
            return Arrays.toString(this.strings);
        }
    }
}


