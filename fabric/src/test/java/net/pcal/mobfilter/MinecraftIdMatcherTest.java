package net.pcal.mobfilter;

import net.pcal.mobfilter.fabric.FabricPlatform;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MinecraftIdMatcherTest {

    @Test
    public void testMatching() {

        final Platform p = FabricPlatform.get(); // FIXME should make a mock Platform instead

        Assertions.assertFalse(IdMatcher.of(new String[] {}, p).isMatch(r("minecraft:cobblestone")));

        assertTrue(IdMatcher.of(new String[] { "minecraft:cobblestone" }, p).isMatch(r("minecraft:cobblestone")));
        assertFalse(IdMatcher.of(new String[] { "minecraft:cobblestone" }, p).isMatch(r("minecraft:stone")));

        assertTrue(IdMatcher.of(new String[] { "minecraft:*" }, p).isMatch(r("minecraft:cobblestone")));
        assertTrue(IdMatcher.of(new String[] { "minecraft:*" }, p).isMatch(r("minecraft:stone")));

        assertTrue(IdMatcher.of(new String[] { "minecraft:cobblestone", "mymod:*" }, p).isMatch(r("minecraft:cobblestone")));
        assertTrue(IdMatcher.of(new String[] { "minecraft:cobblestone", "mymod:*" }, p).isMatch(r("mymod:magicblock")));
        assertFalse(IdMatcher.of(new String[] { "minecraft:cobblestone", "mymod:*" }, p).isMatch(r("minecraft:redstone")));
    }

    private static MinecraftId r(String val) {
        return FabricPlatform.get().parseMinecraftId(val);
    }
}