package net.pcal.mobfilter;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import org.apache.logging.log4j.Logger;

/**
 * Encapsulates information about a pending attempt by the game to spawn an entity.  Any of these
 * methods might return null for various reasons; be careful.
 */
interface SpawnAttempt {
    /**
     * Returns the entity's resource location ID.
     */
    @Deprecated
    ResourceLocation getEntityId();

    /**
     * @return whether the dimension at the id of the entity being spawned
     * matches the given matcher, or null if it couldn't be determined.
     */
    void isEntity(IdMatcher matcher);

    /**
     * Returns the entity type.
     */
    @Deprecated
    EntityType<?> getEntityType();

    /**
     * @return the name of the EntityType being spawned.  Useful only for
     * logging/debugging.
     */
    String getEntityName();

    /**
     * Returns the name of the world.
     */
    @Deprecated
    String getWorldName();

    /**
     * @return whether the name of the current world matches
     * the given matcher, or null if it couldn't be determined.
     */
    void isWorldName(Matcher<String> matcher);

    /**
     * Returns the dimension's resource location ID.
     */
    @Deprecated
    ResourceLocation getDimensionId();

    /**
     * @return whether the id of the dimension at the spawn position matches
     * the given matcher, or null if it couldn't be determined.
     */
    Boolean isDimension(IdMatcher matcher);

    /**
     * Returns the block's resource location ID.
     */
    @Deprecated
    ResourceLocation getBlockId();

    /**
     * @return whether the id of the block under the spawn position matches
     * the given matcher, or null if it couldn't be determined.
     */
    Boolean isBlock(IdMatcher matcher);

    /**
     * Returns the reason for the entity spawn.
     */
    @Deprecated
    EntitySpawnReason getSpawnReason();

    /**
     * @return whether the reason for the spawn matches the
     * given matcher, or null if it couldn't be determined.
     */
    Boolean isSpawnReason(Matcher<Enum<?>> matcher);

    /**
     * Returns the mob category.
     */
    @Deprecated
    MobCategory getMobCategory();

    /**
     * @return whether the category of the mob being spawned matches the
     * given matcher, or null if it couldn't be determined.
     */
    Boolean isMobCategory(Matcher<Enum<?>> matcher);

    /**
     * Returns the block position.
     */
    @Deprecated
    BlockPos getBlockPos();

    /**
     * @return true if the spawn block position x value is within the given
     * range, or null if it couldn't be determined.
     */
    Boolean isBlockX(int min, int max);

    /**
     * @return true if the spawn block position y value is within the given
     * range, or null if it couldn't be determined.
     */
    Boolean isBlockY(int min, int max);

    /**
     * @return true if the spawn block position z value is within the given
     * range, or null if it couldn't be determined.
     */
    Boolean isBlockZ(int min, int max);

    /**
     * Returns the current moon phase.
     */
    @Deprecated
    Integer getMoonPhase();

    /**
     * @return true if the phase of the moon is within the given range, or
     * null if it couldn't be determined.
     */
    Boolean isMoonPhase(int min, int max);

    /**
     * Returns the brightness at the given light layer and block position.
     */
    @Deprecated
    Integer getBrightness(LightLayer lightLayer, BlockPos blockPos);

    /**
     * @return true if the sky light-level at the spawn position is within
     * the given range, or null if it couldn't be determined.
     */
    Boolean isSkyLightLevelIn(int min, int max);

    /**
     * Returns the maximum local raw brightness at the given block position.
     */
    @Deprecated
    Integer getMaxLocalRawBrightness(BlockPos blockPos);

    /**
     * @return true if the light level at the spawn position is within
     * the given range, or null if it couldn't be determined.
     */
    Boolean isLightLevel(int min, int max);

    /**
     * Returns whether it is currently thundering, or null if it couldn't
     * be determined.
     */
    @Deprecated
    Boolean isThundering();

    /**
     * Returns whether it is raining at the given block position, or null
     * if it couldn't be determined.
     */
    @Deprecated
    Boolean isRainingAt(BlockPos blockPos);

    /**
     * Returns the difficulty setting of the current world.
     */
    @Deprecated
    Difficulty getDifficulty();

    /**
     * @return true if the difficulty light-level at the spawn position is within
     * the given range, or null if it couldn't be determined.
     */
    Boolean isDifficultyIn(Matcher<Enum<?>> matcher);

    /**
     * Returns the biome at the given block position.
     */
    @Deprecated
    Biome getBiome(BlockPos blockPos);

    /**
     * @return whether the weather at the spawn position is within
     * the given range, or null if it couldn't be determined.
     */
    Boolean isWeather(Matcher<RuleCheck.WeatherType> matcher);

    /**
     * Returns the biome's resource location ID.
     */
    @Deprecated
    ResourceLocation getBiomeId();

    /**
     * @return whether the biome at the spawn position matches the
     * the given matcher, or null if it couldn't be determined.
     */
    Boolean isBiome(IdMatcher matcher);

    /**
     * Returns the current day time.
     */
    @Deprecated
    Long getDayTime();

    /**
     * @return true if the time of day is within the given range, or
     * null if it couldn't be determined.
     */
    Boolean isDayTime(int min, int max);

    /**
     * Returns a logger instance for MobFilter-specific messages .
     */
    Logger getLogger();
}
