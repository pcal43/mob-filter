package net.pcal.mobfilter;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ServerLevelData;
import org.apache.logging.log4j.Logger;

import static java.util.Objects.requireNonNull;

/**
 * Encapsulates information about a pending attempt by the game to spawn an entity.  Any of these
 * methods might return null for various reasons; be careful.
 */
interface SpawnAttempt {
    /**
     * Returns the entity's resource location ID.
     */
    ResourceLocation getEntityId();

    /**
     * Returns the entity type.
     */
    EntityType<?> getEntityType();

    /**
     * Returns the name of the world.
     */
    String getWorldName();

    /**
     * Returns the dimension's resource location ID.
     */
    ResourceLocation getDimensionId();

    /**
     * Returns the block's resource location ID.
     */
    ResourceLocation getBlockId();

    /**
     * Returns the reason for the entity spawn.
     */
    EntitySpawnReason getSpawnReason();

    /**
     * Returns the mob category.
     */
    MobCategory getMobCategory();

    /**
     * Returns the block position.
     */
    BlockPos getBlockPos();

    /**
     * Returns the current moon phase.
     */
    Integer getMoonPhase();

    /**
     * Returns the brightness at the given light layer and block position.
     */
    Integer getBrightness(LightLayer lightLayer, BlockPos blockPos);

    /**
     * Returns the maximum local raw brightness at the given block position.
     */
    Integer getMaxLocalRawBrightness(BlockPos blockPos);

    /**
     * Returns whether it is currently thundering.
     */
    Boolean isThundering();

    /**
     * Returns whether it is raining at the given block position.
     */
    Boolean isRainingAt(BlockPos blockPos);

    /**
     * Returns the difficulty setting of the current world.
     */
    Difficulty getDifficulty();

    /**
     * Returns the biome at the given block position.
     */
    Biome getBiome(BlockPos blockPos);

    /**
     * Returns the biome's resource location ID.
     */
    ResourceLocation getBiomeId();

    /**
     * Returns the current day time.
     */
    Long getDayTime();

    /**
     * Returns a logger instance for MobFilter-specific messages .
     */
    Logger getLogger();


    // ======================================================================
    // Implementation classes

    /**
     * Implementation of SpawnAttempt for the main game thread.  All attributes of the world are available.
     */
    class MainThreadSpawnAttempt implements SpawnAttempt {

        private final ServerLevel serverWorld;
        private final EntitySpawnReason spawnReason;
        private final MobCategory category;
        private final EntityType<?> entityType;
        private final BlockPos blockPos;
        private final Logger logger;

        MainThreadSpawnAttempt(final ServerLevel serverWorld,
                               final EntitySpawnReason spawnReason,
                               final MobCategory category,
                               final EntityType<?> entityType,
                               final BlockPos blockPos,
                               final Logger logger) {
            this.serverWorld = requireNonNull(serverWorld);
            this.spawnReason = spawnReason;
            this.category = category;
            this.entityType = entityType;
            this.blockPos = blockPos;
            this.logger = requireNonNull(logger);
        }

        @Override
        public ResourceLocation getEntityId() {
            return BuiltInRegistries.ENTITY_TYPE.getKey(entityType); // FIXME is this right?
        }

        @Override
        public EntityType<?> getEntityType() {
            return this.entityType;
        }

        @Override
        public String getWorldName() {
            final ServerLevelData swp;
            try {
                swp = (ServerLevelData) this.serverWorld.getLevelData();
            } catch (ClassCastException cce) {
                this.logger.warn("[MobFilter] serverWorld.getLevelProperties() is unexpected class: " +
                        this.serverWorld.getLevelData().getClass().getName());
                return null;
            }
            return swp.getLevelName();
        }

        @Override
        public ResourceLocation getDimensionId() {
            return this.serverWorld.dimension().location();
        }

        @Override
        public ResourceLocation getBlockId() {
            final BlockState bs = serverWorld.getBlockState(this.blockPos.below());
            final Block block = bs.getBlock();
            return BuiltInRegistries.BLOCK.getKey(block);
        }

        @Override
        public EntitySpawnReason getSpawnReason() {
            return spawnReason;
        }

        @Override
        public MobCategory getMobCategory() {
            return category;
        }

        @Override
        public Logger getLogger() {
            return this.logger;
        }

        @Override
        public BlockPos getBlockPos() {
            return this.blockPos;
        }

        @Override
        public Integer getMoonPhase() {
            return serverWorld.getMoonPhase();
        }

        @Override
        public Integer getBrightness(LightLayer lightLayer, BlockPos blockPos) {
            return serverWorld.getBrightness(lightLayer, blockPos);
        }

        @Override
        public Integer getMaxLocalRawBrightness(BlockPos blockPos) {
            return serverWorld.getMaxLocalRawBrightness(blockPos);
        }

        @Override
        public Boolean isThundering() {
            return serverWorld.isThundering();
        }

        @Override
        public Boolean isRainingAt(BlockPos blockPos) {
            return serverWorld.isRainingAt(blockPos);
        }

        @Override
        public Difficulty getDifficulty() {
            return serverWorld.getDifficulty();
        }

        @Override
        public Biome getBiome(BlockPos blockPos) {
            final Holder<Biome> holder = serverWorld.getBiome(this.blockPos);
            //noinspection ConstantValue
            if (holder == null) {
                this.logger.debug(()->"[MobFilter] null biome returned at " + this.blockPos);
                return null;
            } else {
                return holder.value();
            }
        }

        @Override
        public ResourceLocation getBiomeId() {
            final Biome biome = this.getBiome(blockPos);
            if (biome == null) return null;
            // FIXME? I'm not entirely sure this is correct
            return serverWorld.registryAccess().lookupOrThrow(Registries.BIOME).getKey(biome);
        }

        @Override
        public Long getDayTime() {
            return serverWorld.getDayTime();
        }
    }

    /**
     * Implementation of SpawnAttempt for the world generation thread.  Because we cannot safely access the world
     * state during worldgen, many of the attributes here must return null.
     */
    class WorldgenThreadSpawnAttempt implements SpawnAttempt {

        private final EntitySpawnReason spawnReason;
        private final MobCategory category;
        private final EntityType<?> entityType;
        private final BlockPos blockPos;
        private final Logger logger;

        WorldgenThreadSpawnAttempt(final EntitySpawnReason spawnReason,
                                   final MobCategory category,
                                   final EntityType<?> entityType,
                                   final BlockPos blockPos,
                                   final Logger logger) {
            this.spawnReason = spawnReason;
            this.category = category;
            this.entityType = entityType;
            this.blockPos = blockPos;
            this.logger = requireNonNull(logger);
        }

        /**
         * Return the entity id of the mob that is going to spawn.
         */
        @Override
        public ResourceLocation getEntityId() {
            return BuiltInRegistries.ENTITY_TYPE.getKey(entityType); // FIXME is this right?
        }

        /**
         * Return the entity id of the mob that is going to spawn.
         */
        @Override
        public EntityType<?> getEntityType() {
            return this.entityType;
        }

        @Override
        public EntitySpawnReason getSpawnReason() {
            return spawnReason;
        }

        @Override
        public MobCategory getMobCategory() {
            return category;
        }

        @Override
        public BlockPos getBlockPos() {
            return this.blockPos;
        }

        @Override
        public Logger getLogger() {
            return this.logger;
        }

        // ======================================================================
        // Unsupported during world generation

        @Override
        public String getWorldName() {
            this.logger.debug(()->"[MobFilter] worldName cannot be evaluated during world generation");
            return null;
        }

        @Override
        public ResourceLocation getDimensionId() {
            this.logger.debug(()->"[MobFilter] dimensionId cannot be evaluated during world generation");
            return null;
        }

        @Override
        public ResourceLocation getBlockId() {
            this.logger.debug(()->"[MobFilter] blockId cannot be evaluated during world generation");
            return null;
        }

        @Override
        public Integer getMoonPhase() {
            this.logger.debug(()->"[MobFilter] moonPhase cannot be evaluated during world generation");
            return null;
        }

        @Override
        public Integer getBrightness(LightLayer lightLayer, BlockPos blockPos) {
            this.logger.debug(()->"[MobFilter] brightness cannot be evaluated during world generation");
            return null;
        }

        @Override
        public Integer getMaxLocalRawBrightness(BlockPos blockPos) {
            this.logger.debug(()->"[MobFilter] maxLocalRawBrightness cannot be evaluated during world generation");
            return null;
        }

        @Override
        public Boolean isThundering() {
            this.logger.debug(()->"[MobFilter] thundering cannot be evaluated during world generation");
            return null;
        }

        @Override
        public Boolean isRainingAt(BlockPos blockPos) {
            this.logger.debug(()->"[MobFilter] isRainingAt cannot be evaluated during world generation");
            return null;
        }

        @Override
        public Difficulty getDifficulty() {
            this.logger.debug(()->"[MobFilter] difficulty cannot be evaluated during world generation");
            return null;
        }

        @Override
        public ResourceLocation getBiomeId() {
            this.logger.debug("[MobFilter] biomeId cannot be evaluated during world generation");
            return null;
        }

        @Override
        public Biome getBiome(BlockPos blockPos) {
            this.logger.debug(()->"[MobFilter] biome cannot be evaluated during world generation");
            return null;
        }

        @Override
        public Long getDayTime() {
            this.logger.debug(()->"[MobFilter] dayTime cannot be evaluated during world generation");
            return null;
        }
    }
}
