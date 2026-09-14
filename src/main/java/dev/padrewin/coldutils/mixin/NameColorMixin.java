package dev.padrewin.coldutils.mixin;

import dev.padrewin.coldutils.ColdUtils;
import dev.padrewin.coldutils.NameColor;
import dev.padrewin.coldutils.PlayerEsp;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = {"net.minecraft.class_897", "net.minecraft.client.renderer.entity.EntityRenderer",
        "net.minecraft.client.render.entity.EntityRenderer"}, remap = false)
public abstract class NameColorMixin {
    @Inject(method = {"method_62426(Lnet/minecraft/class_1297;)Lnet/minecraft/class_2561;",
            "getNameTag(Lnet/minecraft/world/entity/Entity;)Lnet/minecraft/network/chat/Component;",
            "getDisplayName(Lnet/minecraft/entity/Entity;)Lnet/minecraft/text/Text;"},
            at = @At("RETURN"), cancellable = true, require = 1)
    private void coldutils$nameColor(@Coerce Object entity, CallbackInfoReturnable<Object> cir) {
        if (ColdUtils.settings().espNames() && !ColdUtils.settings().preserveServerNameColors()
                && PlayerEsp.matches(entity)) {
            cir.setReturnValue(NameColor.apply(cir.getReturnValue(), ColdUtils.settings().nameColor()));
        }
    }
}
