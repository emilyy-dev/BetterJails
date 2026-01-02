//
// This file is part of BetterJails, licensed under the MIT License.
//
// Copyright (c) 2025 emilyy-dev
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.
//

package io.github.emilyydev.betterjails.util;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Manages jail time limits based on permissions.
 * Allows limiting how long certain users/groups can jail others for.
 */
public final class JailLimitManager {

    private static final String LIMIT_PERMISSION_PREFIX = "betterjails.jaillimit.";
    private static final Pattern TIME_PATTERN = Pattern.compile("^(\\d+)([smhdw])$");
    private static final Duration UNLIMITED = Duration.ofDays(365 * 100); // 100 years effectively unlimited

    private JailLimitManager() {
    }

    /**
     * Gets the maximum jail duration a sender is allowed to impose.
     *
     * @param sender The command sender
     * @return The maximum duration, or empty if unlimited
     */
    public static Optional<Duration> getMaxDuration(final CommandSender sender) {
        if (!(sender instanceof Player)) {
            // Console has no limits
            return Optional.empty();
        }

        final Player player = (Player) sender;

        // If player has unlimited permission, return empty
        if (player.hasPermission(LIMIT_PERMISSION_PREFIX + "unlimited")) {
            return Optional.empty();
        }

        Duration maxDuration = null;

        // Check all permissions for jail limit permissions
        for (final PermissionAttachmentInfo permInfo : player.getEffectivePermissions()) {
            if (!permInfo.getValue()) {
                continue;
            }

            final String permission = permInfo.getPermission();
            if (!permission.startsWith(LIMIT_PERMISSION_PREFIX)) {
                continue;
            }

            final String timePart = permission.substring(LIMIT_PERMISSION_PREFIX.length());
            final Optional<Duration> duration = parseDuration(timePart);

            if (duration.isPresent()) {
                if (maxDuration == null || duration.get().compareTo(maxDuration) > 0) {
                    maxDuration = duration.get();
                }
            }
        }

        return Optional.ofNullable(maxDuration);
    }

    /**
     * Checks if a sender can jail for the specified duration.
     *
     * @param sender            The command sender
     * @param requestedDuration The requested jail duration
     * @return true if allowed, false otherwise
     */
    public static boolean canJailFor(final CommandSender sender, final Duration requestedDuration) {
        final Optional<Duration> maxDuration = getMaxDuration(sender);

        // If no limit, allow
        return maxDuration.map(duration -> requestedDuration.compareTo(duration) <= 0).orElse(true);

        // Check if requested duration is within limit
    }

    /**
     * Parses a duration string from a permission.
     * Format: <number><unit> where unit is s/m/h/d/w
     *
     * @param input The input string
     * @return The parsed duration, or empty if invalid
     */
    private static Optional<Duration> parseDuration(final String input) {
        if ("unlimited".equalsIgnoreCase(input)) {
            return Optional.of(UNLIMITED);
        }

        final Matcher matcher = TIME_PATTERN.matcher(input.toLowerCase());
        if (!matcher.matches()) {
            return Optional.empty();
        }

        final long value = Long.parseLong(matcher.group(1));
        final String unit = matcher.group(2);

        final Duration duration;
        switch (unit) {
            case "s":
                duration = Duration.ofSeconds(value);
                break;
            case "m":
                duration = Duration.ofMinutes(value);
                break;
            case "h":
                duration = Duration.ofHours(value);
                break;
            case "d":
                duration = Duration.ofDays(value);
                break;
            case "w":
                duration = Duration.ofDays(value * 7);
                break;
            default:
                return Optional.empty();
        }

        return Optional.of(duration);
    }

    /**
     * Formats a duration for display.
     *
     * @param duration The duration to format
     * @return The formatted string
     */
    public static @NotNull String formatDuration(final @NotNull Duration duration) {
        if (duration.compareTo(UNLIMITED) >= 0) {
            return "unlimited";
        }

        final long days = duration.toDays();
        if (days > 0) {
            if (days % 7 == 0) {
                return (days / 7) + "w";
            }
            return days + "d";
        }

        final long hours = duration.toHours();
        if (hours > 0) {
            return hours + "h";
        }

        final long minutes = duration.toMinutes();
        if (minutes > 0) {
            return minutes + "m";
        }

        return duration.getSeconds() + "s";
    }
}