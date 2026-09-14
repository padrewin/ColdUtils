package dev.padrewin.coldutils.mixin;
import dev.padrewin.coldutils.ModuleRuntime;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Pseudo @Mixin(targets={"net.minecraft.class_309","net.minecraft.client.KeyboardHandler"},remap=false)
public abstract class KeyboardMixin {
    @Inject(method={"method_1466","keyPress"},at=@At("HEAD"),cancellable=true)
    private void coldutils$key(long window,int action,@Coerce Object event,CallbackInfo ci){if(ModuleRuntime.keyboard(event,action))ci.cancel();}
}
