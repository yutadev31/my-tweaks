package com.yutadev31.myTweaks.client;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

final class MyTweaksConfigScreen {
    private MyTweaksConfigScreen() {
    }

    static Screen create(Screen parent) {
        MyTweaksConfig config = MyTweaksConfig.get();
        ConfigBuilder builder = ConfigBuilder.create()
            .setParentScreen(parent)
            .setTitle(Text.literal("My Tweaks"));
        ConfigCategory general = builder.getOrCreateCategory(Text.literal("General"));
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        general.addEntry(entryBuilder
            .startBooleanToggle(Text.literal("ウェイポイント共有の読み込み"), config.isJourneyMapChatImportEnabled())
            .setDefaultValue(true)
            .setTooltip(Text.literal("JourneyMap共有文字列をチャットから検出して、Xaero Minimapへの追加ボタンを表示します。"))
            .setSaveConsumer(config::setJourneyMapChatImportEnabled)
            .build());

        builder.setSavingRunnable(MyTweaksConfig::save);
        return builder.build();
    }
}
