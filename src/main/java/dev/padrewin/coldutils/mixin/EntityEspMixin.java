package dev.padrewin.coldutils.mixin;

import dev.padrewin.coldutils.ColdUtils;
import dev.padrewin.coldutils.PlayerEsp;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Optional class names cover production intermediary, Mojang and Yarn runtimes. */
@Pseudo
@Mixin(targets = {"net.minecraft.class_1297", "net.minecraft.world.entity.Entity",
        "net.minecraft.entity.Entity"}, remap = false)
public abstract class EntityEspMixin {
    @Inject(method = {"method_5851()Z", "isCurrentlyGlowing()Z", "isGlowing()Z"},
            at = @At("HEAD"), cancellable = true, require = 1)
    private void coldutils$glowing(CallbackInfoReturnable<Boolean> cir) {
        if (ColdUtils.settings().espEnabled() && PlayerEsp.matches(this)) cir.setReturnValue(true);
    }

    @Inject(method = {"method_22861()I", "getTeamColor()I", "getTeamColorValue()I"},
            at = @At("HEAD"), cancellable = true, require = 1)
    private void coldutils$color(CallbackInfoReturnable<Integer> cir) {
        if (ColdUtils.settings().espEnabled() && PlayerEsp.matches(this)) {
            cir.setReturnValue(ColdUtils.settings().espColor());
        }
    }
}
