# Debugging NeoForge ModDev Plugin

## Where `runClient` Comes From

The `runClient` task is automatically created by the **NeoForge ModDev Gradle Plugin** (`net.neoforged.moddev` version `2.0.38-beta`).

When you configure:
```groovy
neoForge {
    runs {
        client {
            type = "client"
            gameDirectory = file("${project.projectDir}/run")
        }
    }
}
```

The plugin automatically creates:
- `:forge:runClient` - The main task to run the client
- `:forge:prepareClientRun` - Prepares files needed for the run
- `:forge:writeClientLegacyClasspath` - Writes the classpath file

## How to Debug

### 1. Check the Classpath

The moddev plugin uses the build/classes directory directly in development mode:

```bash
# Check what's on the classpath
./gradlew :forge:runClient --info 2>&1 | grep -E "classpath|build/classes"
```

You should see:
- `/home/pcal/dev/mob-filter/forge/build/classes/java/main` (forge module classes)
- `/home/pcal/dev/mob-filter/forge/build/resources/main` (forge resources)
- `/home/pcal/dev/mob-filter/common/build/libs/common-*.jar` (common module as jar)

### 2. Check Mod Discovery Configuration

The moddev plugin uses `-Dfml.modFolders` to tell NeoForge where to find mods:

```bash
# Check the VM args
cat forge/build/moddev/clientRunVmArgs.txt | grep modFolders
```

### 3. Verify Classes Are Compiled

```bash
# Check if your @Mod class is compiled
ls -la forge/build/classes/java/main/net/pcal/mobfilter/forge/ForgeMobFilterMod.class

# Check if resources are processed
ls -la forge/build/resources/main/META-INF/neoforge.mods.toml
cat forge/build/resources/main/META-INF/neoforge.mods.toml
```

### 4. Enable Debug Logging

Add to your `gradle.properties`:
```properties
org.gradle.logging.level=debug
```

Or run with:
```bash
./gradlew :forge:runClient --debug 2>&1 | tee runClient-debug.log
```

### 5. Check NeoForge Mod Discovery Logs

When Minecraft starts, look for:
- `[MOD]` prefixes in the logs
- Mod discovery messages
- Any errors about missing mods or classes

### 6. Inspect the Run Configuration

```bash
# See what tasks runClient depends on
./gradlew :forge:runClient --dry-run

# See the full command that will be executed
./gradlew :forge:runClient --info 2>&1 | grep "Command:"
```

### 7. Check if Mod is Being Scanned

The moddev plugin should automatically include your mod's classes directory. If it's not being discovered:

1. Verify the `@Mod` annotation is present and correct
2. Check that `neoforge.mods.toml` is in `src/main/resources/META-INF/`
3. Ensure the modId in the annotation matches the modId in the toml file
4. Verify the class is on the classpath (see step 1)

### 8. Common Issues

**Issue: Mod class not being loaded**
- Check: Is the class compiled? (`ls forge/build/classes/java/main/net/pcal/mobfilter/forge/`)
- Check: Is it on the classpath? (see step 1)
- Check: Does the `@Mod` annotation have the correct modId?

**Issue: Resources not found**
- Check: Is `processResources` running? (`./gradlew :forge:processResources`)
- Check: Is the version placeholder replaced? (`cat forge/build/resources/main/META-INF/neoforge.mods.toml`)

**Issue: Common module classes not found**
- Check: Is common module jar on classpath? (see step 1)
- Check: Is common module being built? (`./gradlew :common:jar`)

## Understanding the ModDev Plugin

The moddev plugin:
1. Uses `build/classes` directories directly (not jars) for development
2. Automatically includes your project's classes and resources on the classpath
3. Creates a development environment that mirrors production
4. Uses NeoForge's mod discovery system to find `@Mod` annotated classes

The key difference from production:
- **Development**: Classes from `build/classes/java/main` + resources from `build/resources/main`
- **Production**: Everything packaged in a jar file

## About the `bin` Directory

If you see a `bin` directory alongside `build`, it's likely from your IDE (Eclipse, IntelliJ, etc.) compiling classes separately. This is **not** a problem because:

1. Gradle uses `build/classes/java/main` (not `bin`)
2. The classpath in `runClient` explicitly uses `forge/build/classes/java/main`
3. The `bin` directory is already in `.gitignore`

However, if you want to clean it up:
```bash
rm -rf forge/bin
```

The `bin` directory won't interfere with mod discovery, but it's good practice to keep your workspace clean.

