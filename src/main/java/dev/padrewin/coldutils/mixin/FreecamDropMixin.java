package dev.padrewin.coldutils.mixin;
import dev.padrewin.coldutils.ModuleRuntime;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Pseudo @Mixin(targets={"net.minecraft.class_746","net.minecraft.client.player.LocalPlayer"},remap=false)
public abstract class FreecamDropMixin {
    @Inject(method={"method_7290(Z)Z","drop(Z)Z"},at=@At("HEAD"),cancellable=true)
    private void coldutils$drop(boolean all,CallbackInfoReturnable<Boolean> ci){if(ModuleRuntime.freecam())ci.setReturnValue(false);}
}
