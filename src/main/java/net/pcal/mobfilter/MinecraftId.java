package net.pcal.mobfilter;

/**
 * Platform-independent interface for working with ids. (i.e., ResourceLocations).
 */
public interface MinecraftId {

    String getNamespace();

    boolean equals(Object other);
}
