package net.pcal.mobfilter;

import java.util.ArrayList;
import java.util.List;

/**
 * Determines whether a given minecraft id matches a configured list.  Supports
 * - exact string matching (e.g., "minecraft:cobblestone")
 * - anything-in-a-namespace matching (e.g., "minecraft:*")
 * and nothing else.
 */
public interface IdMatcher {

    boolean isMatch(final MinecraftId id);

    static IdMatcher of(final String[] patterns, final Platform platform) {

        final List<String> namespaces = new ArrayList<>();
        final List<MinecraftId> ids = new ArrayList<>();

        for (String pattern : patterns) {
            pattern = pattern.trim();
            if (pattern.endsWith(":*")) {
                namespaces.add(pattern.substring(0, pattern.length() - 2));
            } else if (pattern.contains(":")) {
                ids.add(platform.parseMinecraftId(pattern));
            } else {
                throw new IllegalArgumentException("Invalid id pattern: " + pattern);
            }
        }
        final Matcher<String> namespaceMatchers = Matcher.of(namespaces.toArray(new String[]{}));
        final Matcher<MinecraftId> idMatchers = Matcher.of(ids.toArray(new MinecraftId[]{}));

        return new IdMatcher() {
            @Override
            public boolean isMatch(final MinecraftId id) {
                return namespaceMatchers.isMatch(id.getNamespace()) || idMatchers.isMatch(id);
            }

            @Override
            public String toString() {
                return idMatchers.toString() + namespaceMatchers.toString();
            }
        };
    }
}
