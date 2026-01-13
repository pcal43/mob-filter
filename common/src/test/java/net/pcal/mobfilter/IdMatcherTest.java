package net.pcal.mobfilter;

import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class IdMatcherTest {

    @Test
    public void testMatching() {

        assertFalse(IdMatcher.of(new String[] {}).isMatch(r("minecraft:cobblestone")));

        assertTrue(IdMatcher.of(new String[] { "minecraft:cobblestone" }).isMatch(r("minecraft:cobblestone")));
        assertFalse(IdMatcher.of(new String[] { "minecraft:cobblestone" }).isMatch(r("minecraft:stone")));

        assertTrue(IdMatcher.of(new String[] { "minecraft:*" }).isMatch(r("minecraft:cobblestone")));
        assertTrue(IdMatcher.of(new String[] { "minecraft:*" }).isMatch(r("minecraft:stone")));

        assertTrue(IdMatcher.of(new String[] { "minecraft:cobblestone", "mymod:*" }).isMatch(r("minecraft:cobblestone")));
        assertTrue(IdMatcher.of(new String[] { "minecraft:cobblestone", "mymod:*" }).isMatch(r("mymod:magicblock")));
        assertFalse(IdMatcher.of(new String[] { "minecraft:cobblestone", "mymod:*" }).isMatch(r("minecraft:redstone")));
    }

    private static Identifier r(String val) {
        return Identifier.parse(val);
    }


}