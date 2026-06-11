package com.yutadev31.myTweaks.client;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

import net.fabricmc.loader.api.FabricLoader;

public final class MyTweaksConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
        .getConfigDir()
        .resolve("my-tweaks-client.json");
    private static MyTweaksConfig instance;

    private boolean journeyMapChatImportEnabled = true;
    private String windowTitleSuffix = "";
    private boolean itemTooltipTotalCountEnabled = true;
    private String coordinateCopyFormat = "{x}, {y}, {z}";

    private MyTweaksConfig() {
    }

    public static void load() {
        if (Files.notExists(CONFIG_PATH)) {
            instance = new MyTweaksConfig();
            save();
            return;
        }

        try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
            MyTweaksConfig loaded = GSON.fromJson(reader, MyTweaksConfig.class);
            instance = loaded != null ? loaded : new MyTweaksConfig();
        } catch (Exception e) {
            if (!(e instanceof JsonParseException)) {
                e.printStackTrace();
            }
            instance = new MyTweaksConfig();
            save();
        }
    }

    public static MyTweaksConfig get() {
        if (instance == null) {
            load();
        }
        return instance;
    }

    public static void save() {
        MyTweaksConfig config = get();
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                GSON.toJson(config, writer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isJourneyMapChatImportEnabled() {
        return journeyMapChatImportEnabled;
    }

    public void setJourneyMapChatImportEnabled(boolean enabled) {
        this.journeyMapChatImportEnabled = enabled;
    }

    public String getWindowTitleSuffix() {
        return windowTitleSuffix;
    }

    public void setWindowTitleSuffix(String suffix) {
        this.windowTitleSuffix = suffix == null ? "" : suffix;
    }

    public boolean isItemTooltipTotalCountEnabled() {
        return itemTooltipTotalCountEnabled;
    }

    public void setItemTooltipTotalCountEnabled(boolean enabled) {
        this.itemTooltipTotalCountEnabled = enabled;
    }

    public String getCoordinateCopyFormat() {
        return coordinateCopyFormat;
    }

    public void setCoordinateCopyFormat(String format) {
        this.coordinateCopyFormat = format == null || format.isBlank() ? "{x} {y} {z}" : format;
    }
}
