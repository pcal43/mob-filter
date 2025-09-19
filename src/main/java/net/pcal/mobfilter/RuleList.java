package net.pcal.mobfilter;

import java.util.Collections;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Encapsulates a list of filter rules to be evaluated against a running world.
 */
public class RuleList {

    private final List<Rule> rules;

    RuleList(List<Rule> rules) {
        this.rules = requireNonNull(rules);
    }

    /**
     * @return whether the spawn attempt should be allowed according the rules in this list.
     */
    public boolean isSpawnAllowed(final SpawnAttempt att) {
        att.getLogger().trace(() -> "[MobFilter] IS_SPAWN_ALLOWED " + att);
        for (Rule rule : rules) {
            att.getLogger().trace(() -> "[MobFilter]   RULE '" + rule.getName() + "'");
            Boolean isSpawnAllowed = rule.isSpawnAllowed(att);
            if (isSpawnAllowed != null) {
                att.getLogger().trace(() -> "[MobFilter]   RETURN " + isSpawnAllowed);
                return isSpawnAllowed;
            }
        }
        att.getLogger().trace(() -> "[MobFilter]   RETURN true (no rules matched)");
        return true;
    }

    public int getSize() {
        return this.rules.size();
    }

    List<Rule> getRules() {
        return Collections.unmodifiableList(rules);
    }
}
