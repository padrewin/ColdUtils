package dev.padrewin.coldutils.mixin;
import dev.padrewin.coldutils.ModuleRuntime;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Pseudo @Mixin(targets={"net.minecraft.class_7172","net.minecraft.client.OptionInstance"},remap=false)
public abstract class FullbrightMixin {
    @Inject(method={"method_41753()Ljava/lang/Object;","get()Ljava/lang/Object;"},at=@At("RETURN"),cancellable=true)
    private void coldutils$brightness(CallbackInfoReturnable<Object> ci){ci.setReturnValue(ModuleRuntime.optionValue(this,ci.getReturnValue()));}
}
