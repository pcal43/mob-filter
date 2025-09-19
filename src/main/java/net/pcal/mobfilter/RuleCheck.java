package net.pcal.mobfilter;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;

import java.util.EnumSet;

/**
 * Encapsulates a single condition check of a rule.
 */
interface RuleCheck {

    /**
     * @return the result of evaluating this check in the context of the given spawn attempt.
     */
    boolean isMatch(SpawnAttempt att);

    // ======================================================================
    // Implementation classes

    record DimensionCheck(IdMatcher dimensionMatcher) implements RuleCheck {
        @Override
        public boolean isMatch(final SpawnAttempt att) {
            final ResourceLocation dimensionId = att.getDimensionId();
            if (dimensionId == null) {
                att.getLogger().debug(() -> "[MobFilter] DimensionCheck: dimension ID could not be determined for " + att.getEntityType() + ", assuming match");
                return true;
            } else {
                final boolean isMatch = this.dimensionMatcher.isMatch(dimensionId);
                att.getLogger().trace(() -> "[MobFilter] DimensionCheck " + dimensionId + " in " + dimensionMatcher + " " + isMatch);
                return isMatch;
            }
        }
    }

    record BiomeCheck(IdMatcher biomeMatcher) implements RuleCheck {
        @Override
        public boolean isMatch(final SpawnAttempt att) {
            final ResourceLocation biomeId = att.getBiomeId();
            if (biomeId == null) {
                att.getLogger().debug(() -> "[MobFilter] BiomeCheck: biome ID could not be determined for " + att.getEntityType() + ", assuming match");
                return true;
            } else {
                final boolean isMatch = this.biomeMatcher.isMatch(biomeId);
                att.getLogger().trace(() -> "[MobFilter] BiomeCheck " + biomeId + " in " + biomeMatcher + " " + isMatch);
                return isMatch;
            }
        }
    }

    record SpawnReasonCheck(EnumSet<EntitySpawnReason> reasons) implements RuleCheck {
        @Override
        public boolean isMatch(final SpawnAttempt att) {
            final EntitySpawnReason spawnReason = att.getSpawnReason();
            final boolean isMatch;
            if (spawnReason == null) {
                att.getLogger().debug(() -> "[MobFilter] SpawnReasonCheck: reason could not be determined for " + att.getEntityType() + ", assuming match");
                isMatch = MFService.get().getDefaultRuleCheckResult();
            } else {
                isMatch = this.reasons.contains(spawnReason);
            }
            att.getLogger().trace(() -> "[MobFilter] SpawnReasonCheck: " + this.reasons + " " + spawnReason + " " + isMatch + " " + isMatch);
            return isMatch;
        }
    }

    record CategoryCheck(EnumSet<MobCategory> categories) implements RuleCheck {
        @Override
        public boolean isMatch(final SpawnAttempt att) {
            final MobCategory mobCategory = att.getMobCategory();
            final boolean isMatch;
            if (mobCategory == null) {
                att.getLogger().debug(() -> "[MobFilter] CategoryCheck: category could not be determined for " + att.getEntityType() + ", assuming match");
                isMatch = MFService.get().getDefaultRuleCheckResult();
            } else {
                isMatch = this.categories.contains(mobCategory);
            }
            att.getLogger().trace(() -> "[MobFilter] CategoryCheck: " + this.categories + " " + mobCategory + " " + isMatch + " " + isMatch);
            return isMatch;
        }
    }

    record EntityIdCheck(IdMatcher entityMatcher) implements RuleCheck {
        @Override
        public boolean isMatch(final SpawnAttempt att) {
            final ResourceLocation entityId = att.getEntityId();
            final boolean isMatch;
            if (entityId == null) {
                att.getLogger().debug(() -> "[MobFilter] EntityIdCheck: entity ID could not be determined for " + att.getEntityType() + ", assuming match");
                isMatch = MFService.get().getDefaultRuleCheckResult();
            } else {
                isMatch = this.entityMatcher.isMatch(entityId);
            }
            att.getLogger().trace(() -> "[MobFilter] EntityNameCheck: " + entityId + " in " + entityMatcher + " " + isMatch);
            return isMatch;
        }
    }

    record BlockIdCheck(IdMatcher blockMatcher) implements RuleCheck {
        @Override
        public boolean isMatch(final SpawnAttempt att) {
            final ResourceLocation blockId = att.getBlockId();
            final boolean isMatch;
            if (blockId == null) {
                att.getLogger().debug(() -> "[MobFilter] BlockIdCheck: block ID could not be determined for " + att.getEntityType() + ", assuming match");
                isMatch = MFService.get().getDefaultRuleCheckResult();
            } else {
                isMatch = this.blockMatcher.isMatch(blockId);
            }
            att.getLogger().trace(() -> "[MobFilter] BlockIdCheck: " + blockId + " in " + blockMatcher + " " + isMatch);
            return isMatch;
        }
    }

    record BlockPosCheck(Direction.Axis axis, int min, int max) implements RuleCheck {
        @Override
        public boolean isMatch(final SpawnAttempt att) {
            final BlockPos blockPos = att.getBlockPos();
            final Integer val;
            final boolean isMatch;
            if (blockPos == null) {
                att.getLogger().debug(() -> "[MobFilter] BlockPosCheck: no block position, assuming match");
                isMatch = MFService.get().getDefaultRuleCheckResult();
                val = null;
            } else {
                val = att.getBlockPos().get(this.axis);
                isMatch = min <= val && val <= max;
            }
            att.getLogger().trace(() -> "[MobFilter] BlockPosCheck: " + axis + " " + min + " <= " + val + " <= " + max + " " + isMatch);
            return isMatch;
        }
    }

    record LightLevelCheck(int min, int max) implements RuleCheck {
        @Override
        public boolean isMatch(final SpawnAttempt att) {
            final Integer val = att.getMaxLocalRawBrightness(att.getBlockPos());
            final boolean isMatch;
            if (val == null) {
                att.getLogger().debug(() -> "[MobFilter] LightLevelCheck: light level could not be determined, assuming match");
                isMatch = MFService.get().getDefaultRuleCheckResult();
            } else {
                isMatch = min <= val && val <= max;
            }
            att.getLogger().trace(() -> "[MobFilter] LightLevelCheck: " + min + " <= " + val + " <= " + max + " " + isMatch);
            return isMatch;
        }
    }

    record SkylightLevelCheck(int min, int max) implements RuleCheck {
        @Override
        public boolean isMatch(SpawnAttempt att) {
            final Integer val = att.getBrightness(LightLayer.SKY, att.getBlockPos());
            final boolean isMatch;
            if (val == null) {
                att.getLogger().debug(() -> "[MobFilter] SkylightLevelCheck: skylight level could not be determined, assuming match");
                isMatch = MFService.get().getDefaultRuleCheckResult();
            } else {
                isMatch = min <= val && val <= max;
            }
            att.getLogger().trace(() -> "[MobFilter] SkylightLevelCheck: " + min + " <= " + val + " <= " + max + " " + isMatch);
            return isMatch;
        }
    }

    record MoonPhaseCheck(Matcher<Integer> matcher) implements RuleCheck {
        @Override
        public boolean isMatch(final SpawnAttempt att) {
            final Integer val = att.getMoonPhase();
            final boolean isMatch;
            if (val == null) {
                att.getLogger().debug(() -> "[MobFilter] MoonPhaseCheck: moon phase could not be determined, assuming match");
                isMatch = MFService.get().getDefaultRuleCheckResult();
            } else {
                isMatch = matcher.isMatch(val);
            }
            att.getLogger().trace(() -> "[MobFilter] MoonPhaseCheck: " + matcher + " contains " + val + " " + isMatch);
            return isMatch;
        }
    }

    enum WeatherType {
        CLEAR,
        RAIN,
        THUNDER,
        SNOW
    }

    record WeatherCheck(Matcher<WeatherType> matcher) implements RuleCheck {
        @Override
        public boolean isMatch(final SpawnAttempt att) {
            final WeatherType weather = getWeatherType(att);
            final boolean isMatch;
            if (weather == null) {
                att.getLogger().debug(() -> "[MobFilter] WeatherCheck: weather could not be determined, assuming match");
                isMatch = MFService.get().getDefaultRuleCheckResult();
            } else {
                isMatch = matcher.isMatch(weather);
            }
            att.getLogger().trace(() -> "[MobFilter] WeatherCheck: " + matcher + " at " + weather + ": " + isMatch);
            return isMatch;
        }

        private static WeatherType getWeatherType(final SpawnAttempt att) {
            final BlockPos blockpos = att.getBlockPos();
            if (blockpos == null) {
                att.getLogger().debug(() -> "[MobFilter] WeatherCheck: no block position");
                return null;
            }
            final Boolean isThundering = att.isThundering();
            if (isThundering == null) {
                att.getLogger().debug(() -> "[MobFilter] WeatherCheck: isThundering could not be determined");
                return null;
            } else if (isThundering) {
                return WeatherType.THUNDER;
            }
            final Boolean isRaining = att.isRainingAt(att.getBlockPos());
            if (isRaining == null) {
                att.getLogger().debug(() -> "[MobFilter] WeatherCheck: isRaining could not be determined");
                return null;
            } else if (isRaining) {
                // Check for snow
                final Biome biome = att.getBiome(att.getBlockPos());
                if (biome == null) {
                    att.getLogger().debug(() -> "[MobFilter] WeatherCheck: biome could not be determined");
                    return null;
                } else if (biome.hasPrecipitation() && biome.coldEnoughToSnow(att.getBlockPos(), att.getBlockPos().getY())) {
                    return WeatherType.SNOW;
                } else {
                    return WeatherType.RAIN;
                }
            }
            return WeatherType.CLEAR;
        }
    }

    record TimeOfDayCheck(long min, long max) implements RuleCheck {
        @Override
        public boolean isMatch(final SpawnAttempt att) {
            final Long dayTime = att.getDayTime();
            final Long val;
            final boolean isMatch;
            if (dayTime == null) {
                att.getLogger().debug(() -> "[MobFilter] TimeOfDayCheck: day time could not be determined, assuming match");
                isMatch = MFService.get().getDefaultRuleCheckResult();
                val = null;
            } else {
                final long TICKS_PER_DAY = 24000;
                val = dayTime % TICKS_PER_DAY; // apparently getDayTime() is same as getWorldTime()?
                isMatch = min <= val && val <= max;
            }
            att.getLogger().trace(() -> "[MobFilter] TimeOfDayCheck: " + min + " <= " + val + " <= " + max + " " + isMatch);
            return isMatch;
        }
    }

    record RandomCheck(double odds) implements RuleCheck {
        @Override
        public boolean isMatch(SpawnAttempt att) {
            double r = Math.random();
            boolean isMatch = r < odds;
            att.getLogger().trace(() -> "[MobFilter] RadomCheck: " + r + " < " + odds + " " + isMatch);
            return isMatch;
        }
    }

    /**
     * TODO this one really could be evaluated statically at world initialization.  But the code doesn't have support
     * for anything like that right now.
     */
    record WorldNameCheck(Matcher<String> worldNames) implements RuleCheck {

        @Override
        public boolean isMatch(final SpawnAttempt att) {
            final String worldName = att.getWorldName();
            final boolean isMatch;
            if (worldName == null) {
                att.getLogger().debug(() -> "[MobFilter] WorldNameCheck: world name could not be determined for " + att.getEntityType() + ", assuming match");
                isMatch = MFService.get().getDefaultRuleCheckResult();
            } else {
                isMatch = worldNames.isMatch(worldName);
            }
            att.getLogger().trace(() -> "[MobFilter] WorldNameCheck: " + worldName + " in " + worldNames + " " + isMatch);
            return isMatch;
        }
    }
}
