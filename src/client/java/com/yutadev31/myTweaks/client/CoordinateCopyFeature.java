package com.yutadev31.myTweaks.client;

import java.util.Locale;

import org.lwjgl.glfw.GLFW;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

final class CoordinateCopyFeature {
    private static final String DEFAULT_FORMAT = "{x} {y} {z}";
    private static final KeyBinding.Category KEY_CATEGORY = KeyBinding.Category.create(Identifier.of("my-tweaks", "general"));
    private static final KeyBinding COPY_COORDINATES_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
        "座標をコピー",
        InputUtil.Type.KEYSYM,
        GLFW.GLFW_KEY_UNKNOWN,
        KEY_CATEGORY));

    private CoordinateCopyFeature() {
    }

    static void initialize() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (COPY_COORDINATES_KEY.wasPressed()) {
                copyCoordinates(client);
            }
        });
    }

    private static void copyCoordinates(MinecraftClient client) {
        PlayerEntity player = client.player;
        if (player == null) {
            return;
        }

        String copied = formatCoordinates(MyTweaksConfig.get().getCoordinateCopyFormat(), player);
        client.keyboard.setClipboard(copied);
        player.sendMessage(Text.literal("座標をコピーしました: " + copied), true);
    }

    private static String formatCoordinates(String format, PlayerEntity player) {
        String resolvedFormat = format == null || format.isBlank() ? DEFAULT_FORMAT : format;
        return resolvedFormat
            .replace("{x}", Integer.toString(player.getBlockX()))
            .replace("{y}", Integer.toString(player.getBlockY()))
            .replace("{z}", Integer.toString(player.getBlockZ()))
            .replace("{fx}", formatDouble(player.getX()))
            .replace("{fy}", formatDouble(player.getY()))
            .replace("{fz}", formatDouble(player.getZ()));
    }

    private static String formatDouble(double value) {
        return String.format(Locale.ROOT, "%.3f", value);
    }
}
