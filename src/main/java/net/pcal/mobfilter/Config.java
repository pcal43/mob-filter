package net.pcal.mobfilter;

import com.google.common.collect.ImmutableList;
import org.apache.logging.log4j.Level;

import java.util.Collections;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Runtime configuration state for the mod.  The filter rules, mainly.
 */
class Config {

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

        Config build() {
            return new Config(this.rules.build(), this.logLevel);
        }

    }

    private final List<Rule> rules;
    private final Level logLevel;

    Config(List<Rule> rules, Level logLevel) {
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
