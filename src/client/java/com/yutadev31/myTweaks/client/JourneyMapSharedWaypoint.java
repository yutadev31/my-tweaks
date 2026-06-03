package com.yutadev31.myTweaks.client;

import net.minecraft.util.Identifier;

record JourneyMapSharedWaypoint(String raw, String name, int x, Integer y, int z, Identifier dimensionId) {

    boolean hasY() {
        return y != null;
    }
}
