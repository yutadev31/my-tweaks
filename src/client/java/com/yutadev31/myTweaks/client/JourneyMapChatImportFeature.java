package com.yutadev31.myTweaks.client;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.message.MessageHandler;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

final class JourneyMapChatImportFeature {
    private static final String IMPORT_COMMAND = "mytweaks-jm-import";
    private static boolean addingPrompt;

    private JourneyMapChatImportFeature() {
    }

    static void initialize() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
            dispatcher.register(ClientCommandManager.literal(IMPORT_COMMAND)
                .then(ClientCommandManager.argument("payload", StringArgumentType.word())
                    .executes(JourneyMapChatImportFeature::runImportCommand)))
        );
        ClientReceiveMessageEvents.CHAT.register((message, signedMessage, sender, params, receptionTimestamp) ->
            maybeShowImportPrompt(message, false)
        );
        ClientReceiveMessageEvents.GAME.register(JourneyMapChatImportFeature::maybeShowImportPrompt);
    }

    private static int runImportCommand(CommandContext<FabricClientCommandSource> context) {
        String payload = StringArgumentType.getString(context, "payload");
        Text result;
        try {
            String rawShare = new String(Base64.getUrlDecoder().decode(payload), StandardCharsets.UTF_8);
            result = XaeroJourneyMapImporter.importWaypoint(rawShare);
        } catch (IllegalArgumentException e) {
            result = Text.literal("共有データの読み取りに失敗しました。").formatted(Formatting.RED);
        }
        context.getSource().sendFeedback(result);
        return 1;
    }

    private static void maybeShowImportPrompt(Text message, boolean overlay) {
        if (overlay || addingPrompt) {
            return;
        }

        JourneyMapSharedWaypoint share = JourneyMapSharedWaypointParser.find(message.getString()).orElse(null);
        if (share == null) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.inGameHud == null) {
            return;
        }

        addingPrompt = true;
        try {
            client.inGameHud.getChatHud().addMessage(createImportPrompt(share));
        } finally {
            addingPrompt = false;
        }
    }

    private static Text createImportPrompt(JourneyMapSharedWaypoint share) {
        String payload = Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(share.raw().getBytes(StandardCharsets.UTF_8));
        MutableText action = Text.literal("[Xaeroに追加]").setStyle(
            Style.EMPTY
                .withColor(Formatting.GREEN)
                .withUnderline(true)
                .withClickEvent(new ClickEvent.RunCommand("/" + IMPORT_COMMAND + " " + payload))
                .withHoverEvent(new HoverEvent.ShowText(Text.literal("Xaero Minimapに追加")))
        );
        return Text.literal("JourneyMap共有を検出: ")
            .formatted(Formatting.GRAY)
            .append(Text.literal(share.name()).formatted(Formatting.AQUA))
            .append(Text.literal(" "))
            .append(action);
    }
}
