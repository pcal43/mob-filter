package net.pcal.mobfilter.forge;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobSpawnType;
import net.pcal.mobfilter.MinecraftId;
import net.pcal.mobfilter.Platform;

public class ForgePlatform implements Platform {

    // ===================================================================================
    // Singleton

    private static final class SingletonHolder {
        private static final ForgePlatform INSTANCE;

        static {
            INSTANCE = new ForgePlatform();
        }
    }

    public static ForgePlatform get() {
        return SingletonHolder.INSTANCE;
    }

    // ===================================================================================
    // Platform implementation

    @Override
    public MinecraftId parseMinecraftId(String id) {
        return new ForgeMinecraftId(ResourceLocation.parse(id));
    }

    @Override
    public Class<? extends Enum<?>> getSpawnReasonEnum() {
        return MobSpawnType.class;
    }

    @Override
    public Class<? extends Enum<?>> getMobCategoryEnum() {
        return MobCategory.class;
    }

    @Override
    public  Class<? extends Enum<?>> getDifficultyEnum() {
        return Difficulty.class;
    }


    // ===================================================================================
    // Package

    static MinecraftId id(ResourceLocation loc) {
        return new ForgeMinecraftId(loc);
    }

    // ===================================================================================
    // Private

    private record ForgeMinecraftId(ResourceLocation loc) implements MinecraftId {

        @Override
        public String getNamespace() {
            return loc.getNamespace();
        }

        @Override
        public String toString() {
            return loc.toString();
        }
    }
}

