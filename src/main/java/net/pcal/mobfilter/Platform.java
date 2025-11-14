package net.pcal.mobfilter;

public interface Platform {

    MinecraftId parseMinecraftId(String pattern);

    Class<Enum<?>> getSpawnReasonEnum();

    Class<Enum<?>> getMobCategoryEnum();

    Class<Enum<?>> getDifficultyEnum();
}
