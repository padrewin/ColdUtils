package dev.padrewin.coldutils.mixin;
import dev.padrewin.coldutils.ModuleRuntime;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Pseudo @Mixin(targets={"net.minecraft.class_743","net.minecraft.client.player.KeyboardInput"},remap=false)
public abstract class FreecamInputMixin {
    @Inject(method={"method_3129()V","tick()V"},at=@At("TAIL"))
    private void coldutils$input(CallbackInfo ci){ModuleRuntime.clearInput(this);}
}
