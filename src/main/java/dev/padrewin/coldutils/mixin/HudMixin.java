package dev.padrewin.coldutils.mixin;
import dev.padrewin.coldutils.WorldOverlay;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Pseudo @Mixin(targets={"net.minecraft.class_329","net.minecraft.client.gui.Hud"},remap=false)
public abstract class HudMixin {
    @Inject(method={"method_1753","extractRenderState"},at=@At("TAIL"))
    private void coldutils$overlay(@Coerce Object graphics,@Coerce Object delta,CallbackInfo ci){WorldOverlay.render(graphics);}
}
