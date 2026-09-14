package dev.padrewin.coldutils.mixin;
import dev.padrewin.coldutils.ModuleRuntime;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Pseudo @Mixin(targets={"net.minecraft.class_1297","net.minecraft.world.entity.Entity"},remap=false)
public abstract class FreecamTurnMixin {
    @Inject(method={"method_5872(DD)V","turn(DD)V"},at=@At("HEAD"),cancellable=true)
    private void coldutils$turn(double x,double y,CallbackInfo ci){if(ModuleRuntime.turn(this,x,y))ci.cancel();}
}
