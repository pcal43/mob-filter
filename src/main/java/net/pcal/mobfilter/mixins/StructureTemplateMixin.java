package net.pcal.mobfilter.mixins;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.pcal.mobfilter.MFService.MixinBodies.StructureTemplateMixin_method_17917;

/**
 * This intercepts entity generation during structure generation.
 */
@SuppressWarnings("ALL")
@Mixin(StructureTemplate.class)
public abstract class StructureTemplateMixin {

    @Inject(method = "method_17917", at = @At("HEAD"), cancellable = true)
    private static void mf_method_17917(Rotation ignored0, Mirror ignored1, Vec3 ignored2, boolean ignored3,
                                        ServerLevelAccessor sla, Entity entity, CallbackInfo ci
    ) {
        StructureTemplateMixin_method_17917(ignored0, ignored1, ignored2, ignored3, sla, entity, ci);
    }
}
