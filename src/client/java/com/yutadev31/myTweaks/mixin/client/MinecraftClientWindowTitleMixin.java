package com.yutadev31.myTweaks.mixin.client;

import com.yutadev31.myTweaks.client.WindowTitleSuffixApplier;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(MinecraftClient.class)
abstract class MinecraftClientWindowTitleMixin {
    @ModifyArg(
        method = "updateWindowTitle",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/util/Window;setTitle(Ljava/lang/String;)V"
        )
    )
    private String myTweaks$appendWindowTitleSuffix(String title) {
        return WindowTitleSuffixApplier.append(title);
    }
}
