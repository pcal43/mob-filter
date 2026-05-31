# Mob Filter

Mob Filter is a Minecraft mod that provides rule-based control over mob
spawning.

The mod allows server operators and players to prevent specific mobs from
spawning, create safe zones, restrict spawning by biome or dimension, and
apply advanced filtering rules based on environmental conditions such as
time of day, light level, weather, and moon phase. The primary goal is to
provide a powerful, flexible, and predictable system for controlling mob
spawning behavior. :contentReference[oaicite:0]{index=0}

Mob Filter supports both simple configuration for common use cases and
advanced rule-based configuration for more complex scenarios. :contentReference[oaicite:1]{index=1}

## Design Philosophy

When making changes, prioritize:

1. Predictability over cleverness.
2. Flexibility without unnecessary complexity.
3. Server administrator control.
4. Performance during spawn evaluation.
5. Backwards compatibility.

Mob filtering rules should behave consistently and be easy to reason about.

Avoid introducing special cases that make rule evaluation difficult to
understand.

Favor simple and composable filtering primitives over narrowly targeted
features.

## Project Structure

This project supports multiple mod loaders.

- `common/` contains shared filtering logic.
- `fabric/` contains Fabric-specific code.
- `neoforge/` contains NeoForge-specific code.
- `docs/` contains user-facing documentation.

Whenever possible, filtering behavior should be implemented in `common/`.

Loader-specific code should remain isolated to the appropriate platform
module.

Documentation is considered a first-class part of the project and should be
updated when user-visible behavior changes.

## Repository Scope

This repository contains the source code for Mob Filter.

Only inspect files tracked by git.

Ignore:

- `build/`
- `.gradle/`
- `run/`
- `logs/`
- generated resources
- IDE metadata
- temporary files
- crash reports
- test output

Do not spend time analyzing generated files or build outputs.

## Cost-Aware Development

Repository-wide scans are expensive and should be avoided.

Before exploring the repository:

- Prefer targeted analysis.
- Read only files likely to be relevant.
- Start from files explicitly mentioned in the task.
- Follow references outward only as needed.
- Do not read entire directory trees unless necessary.

When discovering files, prefer:

```bash
git ls-files
