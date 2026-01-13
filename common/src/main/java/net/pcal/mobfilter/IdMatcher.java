package net.pcal.mobfilter;

import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

/**
 * Determines whether a given minecraft id matches a configured list.  Supports
 * - exact string matching (e.g., "minecraft:cobblestone")
 * - anything-in-a-namespace matching (e.g., "minecraft:*")
 * and nothing else.
 */
interface IdMatcher {

    boolean isMatch(final Identifier id);

    static IdMatcher of(final String[] patterns) {

        final List<String> namespaces = new ArrayList<>();
        final List<Identifier> ids = new ArrayList<>();

        for (String pattern : patterns) {
            pattern = pattern.trim();
            if (pattern.endsWith(":*")) {
                namespaces.add(pattern.substring(0, pattern.length() - 2));
            } else if (pattern.contains(":")) {
                ids.add(Identifier.parse(pattern));
            } else {
                throw new IllegalArgumentException("Invalid id pattern: " + pattern);
            }
        }
        final Matcher<String> namespaceMatchers = Matcher.of(namespaces.toArray(new String[]{}));
        final Matcher<Identifier> idMatchers = Matcher.of(ids.toArray(new Identifier[]{}));

        return new IdMatcher() {
            @Override
            public boolean isMatch(final Identifier id) {
                return namespaceMatchers.isMatch(id.getNamespace()) || idMatchers.isMatch(id);
            }

            @Override
            public String toString() {
                return idMatchers.toString() + namespaceMatchers.toString();
            }
        };
    }
}
