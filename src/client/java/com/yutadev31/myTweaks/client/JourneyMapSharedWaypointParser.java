package com.yutadev31.myTweaks.client;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.util.Identifier;

final class JourneyMapSharedWaypointParser {
    private static final Pattern SHARE_PATTERN = Pattern.compile(
        "\\[(?:name:\"((?:\\\\.|[^\"\\\\])*)\",\\s*)?x:(-?\\d+)(?:,\\s*y:(-?\\d+))?,\\s*z:(-?\\d+)(?:,\\s*dim:([a-z0-9_.-]+:[a-z0-9_./-]+))?\\]"
    );

    private JourneyMapSharedWaypointParser() {
    }

    static Optional<JourneyMapSharedWaypoint> find(String message) {
        Matcher matcher = SHARE_PATTERN.matcher(message);
        if (!matcher.find()) {
            return Optional.empty();
        }

        Identifier dimensionId = matcher.group(5) == null ? null : Identifier.tryParse(matcher.group(5));
        if (matcher.group(5) != null && dimensionId == null) {
            return Optional.empty();
        }

        return Optional.of(new JourneyMapSharedWaypoint(
            matcher.group(0),
            defaultName(matcher.group(1), matcher.group(2), matcher.group(4)),
            Integer.parseInt(matcher.group(2)),
            matcher.group(3) == null ? null : Integer.valueOf(matcher.group(3)),
            Integer.parseInt(matcher.group(4)),
            dimensionId
        ));
    }

    private static String defaultName(String rawName, String x, String z) {
        if (rawName == null || rawName.isBlank()) {
            return "Waypoint " + x + ", " + z;
        }
        return unescapeName(rawName);
    }

    private static String unescapeName(String value) {
        return value.replace("\\\"", "\"").replace("\\\\", "\\");
    }
}
