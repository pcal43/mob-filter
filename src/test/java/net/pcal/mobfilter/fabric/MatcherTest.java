package net.pcal.mobfilter.fabric;

import net.pcal.mobfilter.Matcher;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MatcherTest {

    @Test
    public void testStringMatching() {

        Assertions.assertFalse(Matcher.of(new String[] {}).isMatch("foo"));

        assertTrue(Matcher.of(new String[] { "foo" }).isMatch("foo"));
        assertFalse(Matcher.of(new String[] { "foo" }).isMatch("bop"));

        assertTrue(Matcher.of(new String[] { "foo", "bar", "baz" }).isMatch("bar"));
        assertFalse(Matcher.of(new String[] { "foo", "bar", "baz" }).isMatch("bop"));

        assertTrue(Matcher.of(new String[] { "foo", "bar", "baz", "boo", "fez", "feh", "meh" }).isMatch("bar"));
        assertFalse(Matcher.of(new String[] { "foo", "bar", "baz", "boo", "fez", "feh", "meh" }).isMatch("bop"));
    }
}
