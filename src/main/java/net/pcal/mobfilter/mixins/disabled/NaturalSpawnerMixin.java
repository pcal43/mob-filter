package net.pcal.mobfilter.mixins.disabled;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.NaturalSpawner;
import net.pcal.mobfilter.MFService;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Consumer;

/**
 * DISABLED.  This is hopefully redundant (we try to catch spawn events earlier in the other mx with earlier checks)
 * and may have unanticipated side effects, so disabled in mixins.json.  Leaving it here as a note.
 */
@SuppressWarnings("ALL")
@Mixin(NaturalSpawner.class)
public abstract class NaturalSpawnerMixin {

    //isValidEmptySpawnBlock
}
