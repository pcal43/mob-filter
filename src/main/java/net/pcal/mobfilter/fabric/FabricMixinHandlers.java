package net.pcal.mobfilter.fabric;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.pcal.mobfilter.MobFilterService;
import net.pcal.mobfilter.SpawnAttempt;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.pcal.mobfilter.fabric.FabricMixinHandlers.MinecraftThreadType.SERVER;
import static net.pcal.mobfilter.fabric.FabricMixinHandlers.MinecraftThreadType.WORLDGEN;

/**
 * Implementation code for the mixins.  Structured this way just so it can
 * be hot-swappable.
 */
public class FabricMixinHandlers {

    // ===================================================================================
    // Singleton

    private static final class SingletonHolder {
        private static final FabricMixinHandlers INSTANCE;

        static {
            INSTANCE = new FabricMixinHandlers();
        }
    }

    public static FabricMixinHandlers get() {
        return SingletonHolder.INSTANCE;
    }

    // ===================================================================================
    // Fields

    /**
     * Because the spawnReason is not available at out interception point in the minecraft code,
     * we track it in a ThreadLocal.  Basically best-effort but it seems to work.
     */
    private final ThreadLocal<EntitySpawnReason> spawnReason = new ThreadLocal<>();

    private final Logger logger = LogManager.getLogger(FabricMixinHandlers.class);


    // ===================================================================================
    // Public

    /**
     * Broad categories of vanilla minecraft thready types.  We care because some kinds of filtering
     * can't be done in the worldgen thread.
     */
    enum MinecraftThreadType {
        SERVER,
        WORLDGEN
    }


    public void WorldGenRegion_addFreshEntity(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (!this.isSpawnAllowed(((WorldGenRegion)(Object)this).getLevel(), entity, WORLDGEN)) {
            entity.remove(Entity.RemovalReason.DISCARDED);
            cir.setReturnValue(false);
        }
    }

    public void ServerLevel_addFreshEntity(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (!this.isSpawnAllowed((ServerLevel) (Object) this, entity, SERVER)) {
            entity.remove(Entity.RemovalReason.DISCARDED);
            cir.setReturnValue(false);
        }
    }

    /**
     * Intercept calls to EntityType.create so we can try to track EntitySpawnReason.
     */
    public void EntityType_create(Level level, EntitySpawnReason reason, CallbackInfoReturnable<Entity> cir) {
        if (level.isClientSide()) return;
        if (!(cir.getReturnValue() instanceof Mob)) return;
        if (reason == null) {
            logger.debug(() -> "[MobFilter] Ignoring attempt to set null spawnReason");
            return;
        } else if (this.spawnReason.get() != null && this.spawnReason.get() != reason) {
            logger.trace(() -> "[MobFilter] Unexpectedly changing existing spawnReason  " +
                    " from " + this.spawnReason.get() + " to " + reason);
        }
        this.spawnReason.set(reason);
    }

    // ===================================================================================
    // Private

    private boolean isSpawnAllowed(final ServerLevel serverLevel,
                                   final Entity entity,
                                   final MinecraftThreadType threadTypeGuess) {
        if (serverLevel.isClientSide()) return true; // no filtering on client
        if (!(entity instanceof Mob)) return true; // we only care about mobs
        final EntitySpawnReason reason = spawnReason.get();
        if (reason == null) {
            logger.debug(() -> "[MobFilter] No spawnReason was set for " + entity.getType());
        } else {
            spawnReason.remove();
        }
        final EntityType<?> entityType = entity.getType();
        final SpawnAttempt att;
        if (determineThreadType(threadTypeGuess) == SERVER) {
            att = new MainThreadSpawnAttempt(serverLevel, reason, entityType.getCategory(), entityType, entity.blockPosition(), logger);
        } else {
            att = new WorldgenThreadSpawnAttempt(reason, entityType.getCategory(), entityType, entity.blockPosition(), logger);
        }
        return MobFilterService.get().isSpawnAllowed(att);
    }

    /**
     * Determine which type of thread we're running in.  The 'guess' is based on where in the minecraft code the
     * mixin executed, and it's probably right.  But because the consequence of guessing wrong can cause the entire
     * game to deadlock, we need to err on the side of caution.
     */
    private MinecraftThreadType determineThreadType(final MinecraftThreadType threadTypeGuess) {
        final String threadName = Thread.currentThread().getName();
        final boolean threadNameLooksLikeWorldgen = threadName.contains("Worker"); // I guess?
        if (threadTypeGuess == WORLDGEN) {
            if (!threadNameLooksLikeWorldgen) {
                logger.debug(() -> "[MobFilter] Thread guess is " + WORLDGEN + " but the name is " + threadName);
            }
            // WORLDGEN is the least-risky case, so let's just go with the guess in any case
            return WORLDGEN;
        } else {
            // However, if we think we're in the MAIN thread, let's double check the name of the current thread.
            // It's difficult to be certain whether any of the mixin code is guaranteed to only run in the MAIN thread,
            // so let's err on the side of caution and double-check the thrad name.  This is not very robust
            // but AFAICT the vanilla code gives us no better way to check.
            if (threadNameLooksLikeWorldgen) {
                logger.debug(() -> "[MobFilter] Overriding guessed MAIN thread to WORLDGEN because current thread name is " + threadName);
                return WORLDGEN;
            } else {
                return SERVER;
            }
        }
    }
}
