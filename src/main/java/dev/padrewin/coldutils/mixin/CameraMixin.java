package dev.padrewin.coldutils.mixin;
import dev.padrewin.coldutils.ModuleRuntime;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Pseudo @Mixin(targets={"net.minecraft.class_4184","net.minecraft.client.Camera"},remap=false)
public abstract class CameraMixin {
    // 26.2 applies alignment before building its projection and frustum; older clients use setup.
    @Inject(method={"method_19321","setup","alignWithEntity"},at=@At("TAIL"),require=1)
    private void coldutils$camera(CallbackInfo ci){ModuleRuntime.camera(this);}
}
