package dev.padrewin.coldutils.mixin;
import dev.padrewin.coldutils.ModuleRuntime;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Pseudo @Mixin(targets={"net.minecraft.class_310","net.minecraft.client.Minecraft"},remap=false)
public abstract class ClientTickMixin {
    @Inject(method={"method_1574()V","tick()V"},at=@At("HEAD"))
    private void coldutils$tick(CallbackInfo ci){ModuleRuntime.tick();}
    @Inject(method={"method_1536()Z","startAttack()Z"},at=@At("HEAD"),cancellable=true)
    private void coldutils$attack(CallbackInfoReturnable<Boolean> ci){if(ModuleRuntime.freecam())ci.setReturnValue(false);}
    @Inject(method={"method_1590(Z)V","continueAttack(Z)V"},at=@At("HEAD"),cancellable=true)
    private void coldutils$breaking(boolean down,CallbackInfo ci){if(ModuleRuntime.freecam())ci.cancel();}
    @Inject(method={"method_1583()V","startUseItem()V"},at=@At("HEAD"),cancellable=true)
    private void coldutils$use(CallbackInfo ci){if(ModuleRuntime.freecam())ci.cancel();}
}
