package net.pcal.mobfilter;

import java.util.List;

import static java.util.Objects.requireNonNull;
import static net.pcal.mobfilter.Rule.RuleAction.ALLOW_SPAWN;

/**
 * One rule to be evaluated in the filter chain.
 */
record Rule(String name,
            List<RuleCheck> checks,
            RuleAction action) {

    Rule {
        requireNonNull(name);
        requireNonNull(checks);
        requireNonNull(action);
    }

    /**
     * Return whether the requested spawn should be allowed, or null if we don't have any opinion (i.e., because
     * the rule didn't match).
     */
    public Boolean isSpawnAllowed(final SpawnAttempt att) {
        for (final RuleCheck check : checks) {
            if (!check.isMatch(att)) return null;
        }
        return this.action == ALLOW_SPAWN;
    }

    public String getName() {
        return this.name;
    }

    public enum RuleAction {
        ALLOW_SPAWN,
        DISALLOW_SPAWN
    }
}
