package net.pcal.mobfilter;


import com.google.common.collect.ImmutableList;
import net.pcal.mobfilter.Rule.RuleAction;
import net.pcal.mobfilter.RuleCheck.EntityIdCheck;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import java.util.List;
import java.util.ArrayList;

import static java.util.Objects.requireNonNull;
import static net.pcal.mobfilter.Rule.RuleAction.ALLOW_SPAWN;
import static net.pcal.mobfilter.Rule.RuleAction.DISALLOW_SPAWN;

/**
 * Object model for the config file.
 */
class SimpleConfigLoader {

    /**
     * Loads the given config file line by line.  Ignores trimmed lines that are empty or start with a '#'.
     * For other lines, an IdMatcher is created and used to create a rule with an EntityIdCheck based on that matcher.
     * If the line starts with '!', the rule action is ALLOW_SPAWN, otherwise it is DISALLOW_SPAWN.
     */
    static void loadRules(final InputStream in, final RuleList.Builder configBuilder) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new java.io.InputStreamReader(requireNonNull(in), StandardCharsets.UTF_8))) {
            int i = 0;
            List<String> groupIds = null;
            Boolean currentPolarity = null;
            while (true) {
                String line = reader.readLine();
                if (line == null) break; // end-of-file
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                final boolean groupPolarity = line.startsWith("!");
                final String idPattern = groupPolarity ? line.substring(1).trim() : line;
                if (idPattern.isEmpty()) continue;

                if (groupIds == null) groupIds = new ArrayList<>();
                if (currentPolarity == null) currentPolarity = groupPolarity;

                // If polarity changes, flush group
                if (currentPolarity != groupPolarity) {
                    if (!groupIds.isEmpty()) {
                        RuleCheck check = new EntityIdCheck(IdMatcher.of(groupIds.toArray(new String[0])));
                        configBuilder.addRule(new Rule("simple-" + i, ImmutableList.of(check),  currentPolarity ? ALLOW_SPAWN : DISALLOW_SPAWN));
                        i++;
                        groupIds.clear();
                    }
                    currentPolarity = groupPolarity;
                }
                groupIds.add(idPattern);
            }
            // EOF, flush anything left
            if (groupIds != null && !groupIds.isEmpty()) {
                RuleCheck check = new EntityIdCheck(IdMatcher.of(groupIds.toArray(new String[0])));
                configBuilder.addRule(new Rule("simple-" + i, ImmutableList.of(check),  currentPolarity ? ALLOW_SPAWN : DISALLOW_SPAWN));
            }
        }
    }
}
