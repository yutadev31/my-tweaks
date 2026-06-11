package com.yutadev31.myTweaks.mixin.client;

import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MinecraftClient.class)
public interface MinecraftClientTitleInvoker {
    @Invoker("updateWindowTitle")
    void myTweaks$updateWindowTitle();
}
