package net.pcal.mobfilter;

public interface Platform {

    MinecraftId parseMinecraftId(String pattern);

    Class<? extends Enum<?>> getSpawnReasonEnum();

    Class<? extends Enum<?>> getMobCategoryEnum();

    Class<? extends Enum<?>> getDifficultyEnum();
}
