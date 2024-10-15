package com.github.alantr7.bukkitplugin.versions;

import lombok.Getter;
import org.intellij.lang.annotations.RegExp;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Objects;

public class Version {

    public static final byte ALPHA = (byte) 0;

    public static final byte BETA = (byte) 1;

    public static final byte RELEASE = (byte) 255;

    @RegExp
    private static final String VERSION_REGEX = "\\d+(.\\d+)*([ab])?";

    public static final Version INVALID = new Version(new Short[0], ALPHA);

    private final Short[] segments;

    @Getter
    private final byte channel;

    @Getter
    private final String stringified;

    private Version(Short[] segments, byte channel) {
        this.segments = segments;
        this.channel = channel;
        this.stringified = String.join(".", Arrays.stream(segments).map(String::valueOf).toArray(String[]::new)) + getChannelChar(channel);
    }

    public static Version from(String input) {
        if (!input.matches(VERSION_REGEX)) {
            return INVALID;
        }

        byte channel;
        if (input.endsWith("a") || input.endsWith("b")) {
            channel = input.endsWith("a") ? ALPHA : BETA;
            input = input.substring(0, input.length() - 1);
        } else {
            channel = RELEASE;
        }

        String[] rawSegments = input.contains(".") ? input.split("\\.") : new String[] { input };
        Short[] segments = new Short[rawSegments.length];

        for (int i = 0; i < segments.length; i++) {
            segments[i] = toSegment(rawSegments[i]);
        }

        return new Version(segments, channel);
    }

    private static short toSegment(String input) {
        return Short.parseShort(input);
    }

    public boolean isValid() {
        return INVALID != this;
    }

    public boolean isOlderThan(@NotNull Version version) {
        return compare(version) < 0;
    }

    public boolean isNewerThan(@NotNull Version version) {
        return compare(version) > 0;
    }

    private int compare(@NotNull Version version) {
        if (this == INVALID)
            return -1;

        if (version == INVALID)
            return 1;

        var compSegments = version.segments;
        int min = Math.min(segments.length, compSegments.length);

        for (int i = 0; i < min; i++) {
            if (segments[i] < compSegments[i])
                return -1;

            if (segments[i] > compSegments[i])
                return 1;
        }

        if (version.segments.length == this.segments.length)
            return compareChannel(channel, version.channel);

        Short[] greaterSegment = segments.length == min ? compSegments : segments;
        for (int i = min; i < greaterSegment.length; i++) {
            if (greaterSegment[i] != 0)
                return greaterSegment == segments ? 1 : -1;
        }

        return compareChannel(channel, version.channel);
    }

    private static int compareChannel(byte ch1, byte ch2) {
        return Integer.compare(ch1 & 0xff, ch2 & 0xff);
    }

    private static String getChannelChar(byte channel) {
        return switch (channel) {
            case ALPHA -> "a";
            case BETA -> "b";
            default -> "";
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Version version = (Version) o;
        return compare(version) == 0;
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(channel);
        result = 31 * result + Arrays.hashCode(segments);
        return result;
    }

    @Override
    public String toString() {
        return this.stringified;
    }

}
