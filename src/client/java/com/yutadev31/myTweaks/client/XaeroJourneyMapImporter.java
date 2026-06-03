package com.yutadev31.myTweaks.client;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;

import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;

final class XaeroJourneyMapImporter {
    private XaeroJourneyMapImporter() {
    }

    static Text importWaypoint(String rawShare) {
        try {
            JourneyMapSharedWaypoint share = JourneyMapSharedWaypointParser.find(rawShare)
                .orElseThrow(() -> new IllegalArgumentException("JourneyMap形式のウェイポイントが見つかりません。"));
            importIntoXaero(share);
            return Text.literal("Xaero Minimapにウェイポイントを追加しました: " + share.name())
                .formatted(Formatting.GREEN);
        } catch (Exception e) {
            return Text.literal("Xaeroへの追加に失敗しました: " + e.getMessage())
                .formatted(Formatting.RED);
        }
    }

    private static void importIntoXaero(JourneyMapSharedWaypoint share) throws Exception {
        Object hudMod = getXaeroHudMod();
        Object session = getMinimapSession(hudMod)
            .orElseThrow(() -> new IllegalStateException("Xaero Minimapのセッションが見つかりません。"));
        Object worldManager = invoke(session, "getWorldManager");
        Object currentWorld = invoke(worldManager, "getCurrentWorld");
        if (currentWorld == null) {
            throw new IllegalStateException("Xaeroの現在ワールドが取得できません。");
        }

        RegistryKey<World> dimensionKey = RegistryKey.of(
            RegistryKeys.WORLD,
            share.dimensionId() == null ? World.OVERWORLD.getValue() : share.dimensionId()
        );
        Object targetWorld = resolveTargetWorld(hudMod, session, worldManager, currentWorld, dimensionKey);
        String setId = (String) invoke(currentWorld, "getCurrentWaypointSetId");
        if (setId == null || setId.isBlank()) {
            throw new IllegalStateException("Xaeroのウェイポイントセットが取得できません。");
        }

        Object waypointSet = invoke(targetWorld, "getWaypointSet", setId);
        if (waypointSet == null) {
            invoke(targetWorld, "addWaypointSet", setId);
            waypointSet = invoke(targetWorld, "getWaypointSet", setId);
        }

        Object waypoint = createWaypoint(share);
        invoke(waypointSet, "add", waypoint);
        invoke(invoke(session, "getWorldManagerIO"), "saveWorld", targetWorld);
    }

    private static Object resolveTargetWorld(
        Object hudMod,
        Object session,
        Object worldManager,
        Object currentWorld,
        RegistryKey<World> dimensionKey
    ) throws Exception {
        Object updater = invoke(session, "getWorldStateUpdater");
        boolean worldMapEnabled = (boolean) invoke(invoke(hudMod, "getSupportMods"), "worldmap");
        String worldNode = (String) invoke(updater, "getPotentialWorldNode", dimensionKey, worldMapEnabled);
        if (worldNode == null) {
            throw new IllegalStateException("共有ウェイポイントの保存先worldを解決できません。");
        }

        Object currentContainerPath = invoke(updater, "getPotentialContainerPath");
        Object targetPath = invoke(currentContainerPath, "resolve", worldNode);
        Object targetWorld = invoke(worldManager, "getWorld", targetPath);
        if (targetWorld == null) {
            targetWorld = invoke(worldManager, "addWorld", targetPath);
        }
        invoke(targetWorld, "setDimId", dimensionKey);

        Object currentSet = invoke(targetWorld, "getWaypointSet", invoke(currentWorld, "getCurrentWaypointSetId"));
        if (currentSet == null) {
            invoke(targetWorld, "setCurrentWaypointSetId", invoke(currentWorld, "getCurrentWaypointSetId"));
        }
        return targetWorld;
    }

    private static Object createWaypoint(JourneyMapSharedWaypoint share) throws Exception {
        Class<?> waypointClass = Class.forName("xaero.common.minimap.waypoints.Waypoint");
        Constructor<?> constructor = waypointClass.getConstructor(
            int.class, int.class, int.class, String.class, String.class, int.class
        );
        Object waypoint = constructor.newInstance(
            share.x(),
            share.hasY() ? share.y() : 0,
            share.z(),
            share.name(),
            createInitials(share.name()),
            0
        );
        invoke(waypoint, "setYIncluded", share.hasY());
        return waypoint;
    }

    private static String createInitials(String name) {
        String compact = name.strip();
        if (compact.isEmpty()) {
            return "?";
        }
        int end = compact.offsetByCodePoints(0, Math.min(2, compact.codePointCount(0, compact.length())));
        return compact.substring(0, end);
    }

    private static Object getXaeroHudMod() throws Exception {
        Class<?> hudModClass = Class.forName("xaero.common.HudMod");
        Field instanceField = hudModClass.getField("INSTANCE");
        Object hudMod = instanceField.get(null);
        if (hudMod == null) {
            throw new IllegalStateException("Xaero Minimapがロードされていません。");
        }
        return hudMod;
    }

    private static Optional<Object> getMinimapSession(Object hudMod) throws Exception {
        Object moduleManager = invoke(invoke(hudMod, "getHud"), "getModuleManager");
        Iterable<?> modules = (Iterable<?>) invoke(moduleManager, "getModules");
        for (Object module : modules) {
            Object session = invoke(module, "getCurrentSession");
            if (session != null && session.getClass().getName().equals("xaero.hud.minimap.module.MinimapSession")) {
                return Optional.of(session);
            }
        }
        return Optional.empty();
    }

    private static Object invoke(Object target, String name, Object... args) throws Exception {
        Method method = findMethod(target.getClass(), name, args);
        if (method == null) {
            throw new NoSuchMethodException(target.getClass().getName() + "#" + name);
        }
        method.setAccessible(true);
        return method.invoke(target, args);
    }

    private static Method findMethod(Class<?> type, String name, Object[] args) {
        Method fallback = null;
        for (Method method : type.getMethods()) {
            if (method.getName().equals(name) && method.getParameterCount() == args.length) {
                if (parametersMatch(method.getParameterTypes(), args)) {
                    return method;
                }
                if (fallback == null) {
                    fallback = method;
                }
            }
        }
        for (Method method : type.getDeclaredMethods()) {
            if (method.getName().equals(name) && method.getParameterCount() == args.length) {
                if (parametersMatch(method.getParameterTypes(), args)) {
                    return method;
                }
                if (fallback == null) {
                    fallback = method;
                }
            }
        }
        return fallback;
    }

    private static boolean parametersMatch(Class<?>[] parameterTypes, Object[] args) {
        for (int i = 0; i < parameterTypes.length; i++) {
            if (!isAssignable(parameterTypes[i], args[i])) {
                return false;
            }
        }
        return true;
    }

    private static boolean isAssignable(Class<?> parameterType, Object arg) {
        if (arg == null) {
            return !parameterType.isPrimitive();
        }
        Class<?> boxedType = box(parameterType);
        return boxedType.isInstance(arg);
    }

    private static Class<?> box(Class<?> type) {
        if (!type.isPrimitive()) {
            return type;
        }
        if (type == boolean.class) {
            return Boolean.class;
        }
        if (type == byte.class) {
            return Byte.class;
        }
        if (type == short.class) {
            return Short.class;
        }
        if (type == int.class) {
            return Integer.class;
        }
        if (type == long.class) {
            return Long.class;
        }
        if (type == float.class) {
            return Float.class;
        }
        if (type == double.class) {
            return Double.class;
        }
        if (type == char.class) {
            return Character.class;
        }
        return type;
    }
}
