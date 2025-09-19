package net.pcal.mobfilter;

import com.google.common.collect.ImmutableList;
import org.apache.logging.log4j.Level;

import java.util.Collections;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Encapsulates a list of filter rules to be evaluated against a running world.
 */
public class RuleList {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private final ImmutableList.Builder<Rule> rules = ImmutableList.builder();
        private Level logLevel = Level.INFO;

        void addRule(Rule rule) {
            this.rules.add(requireNonNull(rule));
        }
        void setLogLevel(Level logLevel) {
            this.logLevel = logLevel;
        }

        RuleList build() {
            return new RuleList(this.rules.build(), this.logLevel);
        }

    }

    private final List<Rule> rules;
    private final Level logLevel;

    RuleList(List<Rule> rules, Level logLevel) {
        this.rules = requireNonNull(rules);
        this.logLevel = requireNonNull(logLevel);
    }

    List<Rule> getRules() {
        return Collections.unmodifiableList(rules);
    }

    public Level getLogLevel() {
        return this.logLevel;
    }
}
