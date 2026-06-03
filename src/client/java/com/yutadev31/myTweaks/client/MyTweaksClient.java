package com.yutadev31.myTweaks.client;

import net.fabricmc.api.ClientModInitializer;

public class MyTweaksClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        JourneyMapChatImportFeature.initialize();
    }
}
