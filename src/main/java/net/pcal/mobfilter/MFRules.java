package net.pcal.mobfilter;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ServerLevelData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

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

        List<FilterRule> getRules() {
            return Collections.unmodifiableList(rules);
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
                      List<FilterCheck> checks,
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
    record SpawnRequest(ServerLevel serverWorld,
                        EntitySpawnReason spawnReason,
                        MobCategory category,
                        EntityType<?> entityType,
                        BlockPos blockPos,
                        Logger logger) {

        /**
         * Return the entity id of the mob that is going to spawn.
         */
        public ResourceLocation getEntityId() {
            return BuiltInRegistries.ENTITY_TYPE.getKey(entityType); // FIXME is this right?
        }

        /**
         * Return the name of the world that the spawn is happening in.
         */
        public String getWorldName() {
            final ServerLevelData swp;
            try {
                swp = (ServerLevelData) this.serverWorld.getLevelData();
            } catch (ClassCastException cce) {
                LOGGER.warn("[MobFilter] serverWorld.getLevelProperties() is unexpected class: " +
                        this.serverWorld.getLevelData().getClass().getName());
                return null;
            }
            return swp.getLevelName();
        }

        /**
         * Return the id of the dimension that the spawn is happening in.
         */
        public ResourceLocation getDimensionId() {
            return this.serverWorld.dimension().location();
        }

        /**
         * Return the id of the biome that the spawn is happening in.
         */
        public ResourceLocation getBiomeId() {
            final Biome biome = serverWorld.getBiome(this.blockPos()).value();
            // FIXME? I'm not entirely sure this is correct
            return serverWorld.registryAccess().lookupOrThrow(Registries.BIOME).getKey(biome);
        }

        /**
         * Return the id of the block that the spawn is happening on.
         */
        public ResourceLocation getBlockId() {
            final BlockState bs = serverWorld.getBlockState(this.blockPos.below());
            final Block block = bs.getBlock();
            return BuiltInRegistries.BLOCK.getKey(block);
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
    record WorldNameCheck(Matcher<String> worldNames) implements FilterCheck {
        @Override
        public boolean isMatch(SpawnRequest req) {
            boolean isMatch = worldNames.isMatch(req.getWorldName());
            req.logger().trace(() -> "[MobFilter]     WorldNameCheck: " + req.getWorldName() + " in " + worldNames + " " + isMatch);
            return isMatch;
        }
    }

    record DimensionCheck(IdMatcher dimensionMatcher) implements FilterCheck {
        @Override
        public boolean isMatch(SpawnRequest req) {
            boolean isMatch = this.dimensionMatcher.isMatch(req.getDimensionId());
            req.logger().trace(() -> "[MobFilter]     DimensionCheck " + req.getDimensionId() + " in " + dimensionMatcher + " " + isMatch);
            return isMatch;
        }
    }

    record BiomeCheck(IdMatcher biomeMatcher) implements FilterCheck {
        @Override
        public boolean isMatch(SpawnRequest req) {
            boolean isMatch = this.biomeMatcher.isMatch(req.getBiomeId());
            req.logger().trace(() -> "[MobFilter]     BiomeCheck " + req.getBiomeId() + " in " + biomeMatcher +" "+isMatch);
            return isMatch;
        }
    }

    record SpawnReasonCheck(EnumSet<EntitySpawnReason> reasons) implements FilterCheck {
        @Override
        public boolean isMatch(SpawnRequest req) {
            boolean isMatch = this.reasons.contains(req.spawnReason);
            req.logger().trace(() -> "[MobFilter]     SpawnReasonCheck: " + this.reasons + " " + req.spawnReason + " " + isMatch + " " + isMatch);
            return isMatch;
        }
    }

    record CategoryCheck(EnumSet<MobCategory> categories) implements FilterCheck {
        @Override
        public boolean isMatch(SpawnRequest req) {
            boolean isMatch = this.categories.contains(req.category);
            req.logger().trace(() -> "[MobFilter]     CategoryCheck: " + this.categories + " " + req.category + " " + isMatch + " " + isMatch);
            return isMatch;
        }
    }

    record EntityIdCheck(IdMatcher entityMatcher) implements FilterCheck {
        @Override
        public boolean isMatch(SpawnRequest req) {
            boolean isMatch = this.entityMatcher.isMatch(req.getEntityId());
            req.logger().trace(() -> "[MobFilter]     EntityNameCheck " + req.getEntityId() + " in " + entityMatcher +" "+isMatch);
            return isMatch;
        }
    }

    record BlockIdCheck(IdMatcher blockMatcher) implements FilterCheck {
        @Override
        public boolean isMatch(SpawnRequest req) {
            boolean isMatch = this.blockMatcher.isMatch(req.getBlockId());
            req.logger().trace(() -> "[MobFilter]     BlockIdCheck " + req.getEntityId() + " in " + blockMatcher +" "+isMatch);
            return isMatch;
        }
    }

    record BlockPosCheck(Direction.Axis axis, int min, int max) implements FilterCheck {
        @Override
        public boolean isMatch(SpawnRequest req) {
            int val = req.blockPos.get(this.axis);
            boolean isMatch = min <= val && val <= max;
            req.logger().trace(() -> "[MobFilter]     BlockPosCheck " + axis + " " + min + " <= " + val + " <= " + max+ " "+isMatch);
            return isMatch;
        }
    }

    record LightLevelCheck(int min, int max) implements FilterCheck {
        @Override
        public boolean isMatch(SpawnRequest req) {
            int val = req.serverWorld.getMaxLocalRawBrightness(req.blockPos);
            boolean isMatch = min <= val && val <= max;
            req.logger().trace(() -> "[MobFilter]     LightLevelCheck " + min + " <= " + val + " <= " + max+ " " +isMatch);
            return isMatch;
        }
    }

    record MoonPhaseCheck(Matcher<Integer> matcher) implements FilterCheck {
        @Override
        public boolean isMatch(SpawnRequest req) {
            int val = req.serverWorld.getMoonPhase();
            boolean isMatch = matcher.isMatch(val);
            req.logger().trace(() -> "[MobFilter]     MoonPhaseCheck " + matcher + " contains " + val + " " + isMatch);
            return isMatch;
        }
    }

    record TimeOfDayCheck(long min, long max) implements FilterCheck {
        @Override
        public boolean isMatch(SpawnRequest req) {
            final long TICKS_PER_DAY = 24000;
            long val = req.serverWorld.getDayTime() % TICKS_PER_DAY; // apparently getDayTime() is same as getWorldTime()?
            boolean isMatch = min <= val && val <= max;
            req.logger().trace(() -> "[MobFilter]     TimeOfDayCheck " + min + " <= " + val + " <= " + max+" "+isMatch);
            return isMatch;
        }
    }
}


