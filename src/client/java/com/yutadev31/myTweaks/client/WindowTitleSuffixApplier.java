package com.yutadev31.myTweaks.client;

import com.yutadev31.myTweaks.mixin.client.MinecraftClientTitleInvoker;
import net.minecraft.client.MinecraftClient;

public final class WindowTitleSuffixApplier {
    private WindowTitleSuffixApplier() {
    }

    public static String append(String title) {
        String suffix = MyTweaksConfig.get().getWindowTitleSuffix().trim();
        if (suffix.isEmpty()) {
            return title;
        }
        return title + " " + suffix;
    }

    static void refresh() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) {
            return;
        }
        ((MinecraftClientTitleInvoker) client).myTweaks$updateWindowTitle();
    }
}
