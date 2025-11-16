# Release Module

This module creates a single universal JAR that works on both **Fabric** and **NeoForge** mod loaders.

## What It Does

The `release` module combines all three modules (`common`, `fabric`, and `neoforge`) into a single JAR file that can be used on both Fabric and NeoForge.

### Structure

The universal JAR contains:

1. **All common module classes** - extracted at the root level, including:
   - Mixin classes
   - Core logic (Config, MixinService, SpawnAttempt, etc.)
   - Resource files (mobfilter.mixins.json, default configs, etc.)

2. **Fabric-specific classes**:
   - `FabricModInitializer` and `FabricClientModInitializer`
   - `fabric.mod.json` metadata file

3. **NeoForge-specific classes**:
   - `ForgeMobFilterMod`
   - `META-INF/neoforge.mods.toml` metadata file

## Building

To build the universal JAR:

```bash
./gradlew :release:build
```

Or to build only the universal JAR without tests:

```bash
./gradlew :release:universalJar
```

The resulting JAR will be located at:
```
release/build/libs/mobfilter-<version>.jar
```

## How It Works

1. The build depends on all three modules being built first
2. It extracts classes from the `common` module JAR
3. It extracts Fabric-specific classes and metadata from the `fabric` module JAR
4. It extracts NeoForge-specific classes and metadata from the `neoforge` module JAR
5. It combines everything into a single JAR with:
   - All classes at the root level (no nested JARs)
   - Both `fabric.mod.json` and `META-INF/neoforge.mods.toml` present
   - Proper manifest attributes

## Using the Universal JAR

The universal JAR can be installed on either:

- **Fabric**: The Fabric mod loader will recognize `fabric.mod.json` and load the mod using `FabricModInitializer`
- **NeoForge**: The NeoForge mod loader will recognize `META-INF/neoforge.mods.toml` and load the mod using `ForgeMobFilterMod`

Both loaders will have access to all the common code (mixins, core logic, etc.) since it's extracted at the root level.

## Module Layout

```
mobfilter/
├── common/          # Shared code (mixins, core logic)
├── fabric/          # Fabric-specific initialization
├── neoforge/        # NeoForge-specific initialization
└── release/         # Combines all three into universal JAR
```

## Notes

- The universal JAR is approximately 81KB in size
- Duplicate files are automatically excluded
- Signature files (META-INF/*.SF, *.DSA, *.RSA) are excluded to avoid conflicts
- The default `jar` task is disabled in favor of the `universalJar` task

