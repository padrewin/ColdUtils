package dev.padrewin.coldutils.mixin;
import dev.padrewin.coldutils.ModuleRuntime;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Pseudo @Mixin(targets={"net.minecraft.class_757","net.minecraft.client.renderer.GameRenderer"},remap=false)
public abstract class FreecamHandMixin {
    @Inject(method={"method_3172","renderItemInHand"},at=@At("HEAD"),cancellable=true)
    private void coldutils$hand(CallbackInfo ci){if(ModuleRuntime.freecam())ci.cancel();}
}
