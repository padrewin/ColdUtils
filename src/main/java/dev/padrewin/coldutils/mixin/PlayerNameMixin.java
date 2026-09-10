package dev.padrewin.coldutils.mixin;

import dev.padrewin.coldutils.ColdUtils;
import dev.padrewin.coldutils.PlayerEsp;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = {"net.minecraft.class_922", "net.minecraft.client.renderer.entity.LivingEntityRenderer",
        "net.minecraft.client.render.entity.LivingEntityRenderer"}, remap = false)
public abstract class PlayerNameMixin {
    @Inject(method = {"method_4055(Lnet/minecraft/class_1309;D)Z",
            "shouldShowName(Lnet/minecraft/world/entity/LivingEntity;D)Z",
            "hasLabel(Lnet/minecraft/entity/LivingEntity;D)Z"},
            at = @At("HEAD"), cancellable = true, require = 1)
    private void coldutils$name(@Coerce Object entity, double distanceSquared,
                                CallbackInfoReturnable<Boolean> cir) {
        // Disabling ESP names restores vanilla labels rather than hiding normal player names.
        if (ColdUtils.settings().espNames() && PlayerEsp.matches(entity)) cir.setReturnValue(true);
    }
}
