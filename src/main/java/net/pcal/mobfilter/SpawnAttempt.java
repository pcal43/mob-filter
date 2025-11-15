package net.pcal.mobfilter;

import org.apache.logging.log4j.Logger;

/**
 * Encapsulates information about a pending attempt by the game to spawn an entity.  Any of these
 * methods might return null for various reasons; be careful.
 */
public interface SpawnAttempt {
    /**
     * Returns the entity's resource location ID.
     */
    MinecraftId getEntityId();

    /**
     * Returns the name of the world.
     */
    String getWorldName();

    /**
     * Returns the dimension's resource location ID.
     */
    MinecraftId getDimensionId();

    /**
     * Returns the block's resource location ID.
     */
    MinecraftId getBlockId();

    /**
     * Returns the reason for the entity spawn.
     */
    Enum<?> getSpawnReason();

    /**
     * @return  the category of the mob being spawned, or null if
     * it couldn't be determined.
     */
    Enum<?> getMobCategory();

    /**
     * @return the X-value of the block under the spawned mob, or null if
     * it couldn't be determined.
     */
    Integer getBlockX();

    /**
     * @return the Y-value of the block under the spawned mob, or null if
     * it couldn't be determined.
     */
    Integer getBlockY();

    /**
     * @return the Z-value of the block under the spawned mob, or null if
     * it couldn't be determined.
     */
    Integer getBlockZ();

    /**
     * Returns the current moon phase.
     */
    Integer getMoonPhase();

    /**
     * @return the sky light-level at the spawn position, or null if it
     * couldn't be determined.
     */
    Integer getSkylightLevel();

    /**
     * @return the max local brightness at the spawn position, or null if it
     * couldn't be determined.
     */
    Integer getLightLevel();

    /**
     * @return an enum value indicating the weeather type at the spawn
     * position, or or null if it couldn't be determined.
     */
    WeatherType getWeather();

    /**
     * Returns the difficulty setting of the current world.
     */
    Enum<?> getDifficulty();

    /**
     * Returns the biome's resource location ID.
     */
    MinecraftId getBiomeId();

    /**
     * Returns the current day time.
     */
    Long getDayTime();

    /**
     * Returns a logger instance for MobFilter-specific messages .
     */
    Logger getLogger();

    default String getBlockPosition() {
        return getBlockX() + ", " + getBlockY() + ", " + getBlockZ();
    }
}
