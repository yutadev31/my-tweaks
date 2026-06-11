package com.yutadev31.myTweaks.client;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.MinecraftClient;
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

        general.addEntry(entryBuilder
            .startStrField(Text.literal("ウィンドウタイトル末尾"), config.getWindowTitleSuffix())
            .setDefaultValue("")
            .setTooltip(Text.literal("複数の起動構成を同時に開くとき用に、Minecraftウィンドウタイトルの末尾へ追加する文字列です。"))
            .setSaveConsumer(config::setWindowTitleSuffix)
            .build());

        general.addEntry(entryBuilder
            .startBooleanToggle(Text.literal("アイテム総数をツールチップ表示"), config.isItemTooltipTotalCountEnabled())
            .setDefaultValue(true)
            .setTooltip(Text.literal("インベントリやチェストでアイテムをホバーしたとき、同じアイテムが現在の画面内に合計何個あるかを表示します。"))
            .setSaveConsumer(config::setItemTooltipTotalCountEnabled)
            .build());

        general.addEntry(entryBuilder
            .startStrField(Text.literal("座標コピーのフォーマット"), config.getCoordinateCopyFormat())
            .setDefaultValue("{x} {y} {z}")
            .setTooltip(
                Text.literal("座標コピーボタンで使う文字列です。"),
                Text.literal("使用可能: {x} {y} {z} は整数座標、{fx} {fy} {fz} は小数座標です。"),
                Text.literal("キー割り当ては操作設定から「座標をコピー」を設定してください。"))
            .setSaveConsumer(config::setCoordinateCopyFormat)
            .build());

        builder.setSavingRunnable(() -> {
            MyTweaksConfig.save();
            WindowTitleSuffixApplier.refresh();
        });
        return builder.build();
    }
}
