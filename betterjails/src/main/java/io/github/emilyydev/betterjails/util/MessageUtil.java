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

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Utility class for handling messages with MiniMessage and Adventure API.
 * Provides backwards compatibility with legacy color codes.
 */
public final class MessageUtil {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY_SERIALIZER =
            LegacyComponentSerializer.legacyAmpersand();

    private static BukkitAudiences audiences;

    private MessageUtil() {
    }

    /**
     * Initializes the Adventure audiences.
     * Must be called during plugin enable.
     *
     * @param plugin The plugin instance
     */
    public static void init(final Plugin plugin) {
        if (audiences == null) {
            audiences = BukkitAudiences.create(plugin);
        }
    }

    /**
     * Closes the Adventure audiences.
     * Must be called during plugin disable.
     */
    public static void close() {
        if (audiences != null) {
            audiences.close();
            audiences = null;
        }
    }

    /**
     * Gets the audience for a command sender.
     *
     * @param sender The command sender
     * @return The audience
     */
    public static @NotNull Audience audience(final CommandSender sender) {
        return audiences.sender(sender);
    }

    /**
     * Parses a message with MiniMessage format.
     * Falls back to legacy color codes if MiniMessage parsing fails.
     *
     * @param message The message to parse
     * @return The parsed component
     */
    public static @NotNull ComponentLike parse(final String message) {
        try {
            return MINI_MESSAGE.deserialize(message);
        } catch (final Exception ex) {
            // Fallback to legacy format for backwards compatibility
            return LEGACY_SERIALIZER.deserialize(message);
        }
    }

    /**
     * Parses a message with MiniMessage format and placeholders.
     *
     * @param message      The message to parse
     * @param placeholders The placeholder resolvers
     * @return The parsed component
     */
    public static @NotNull Component parse(final String message, final TagResolver... placeholders) {
        try {
            return MINI_MESSAGE.deserialize(message, placeholders);
        } catch (final Exception ex) {
            // Fallback to legacy format
            return LEGACY_SERIALIZER.deserialize(message);
        }
    }

    /**
     * Sends a message to an audience.
     *
     * @param audience The audience
     * @param message  The message
     */
    public static void send(final @NotNull Audience audience, final String message) {
        audience.sendMessage(parse(message));
    }

    /**
     * Sends a message to a command sender.
     *
     * @param sender  The command sender
     * @param message The message
     */
    public static void send(final CommandSender sender, final String message) {
        send(audience(sender), message);
    }

    /**
     * Sends a message with placeholders to an audience.
     *
     * @param audience     The audience
     * @param message      The message
     * @param placeholders The placeholder resolvers
     */
    public static void send(final @NotNull Audience audience, final String message, final TagResolver... placeholders) {
        audience.sendMessage(parse(message, placeholders));
    }

    /**
     * Sends a message with placeholders to a command sender.
     *
     * @param sender       The command sender
     * @param message      The message
     * @param placeholders The placeholder resolvers
     */
    public static void send(final CommandSender sender, final String message, final TagResolver... placeholders) {
        send(audience(sender), message, placeholders);
    }

    /**
     * Creates a string placeholder.
     *
     * @param key   The placeholder key
     * @param value The placeholder value
     * @return The tag resolver
     */
    @Contract("_, _ -> new")
    public static @NotNull TagResolver placeholder(final String key, final String value) {
        return Placeholder.unparsed(key, value);
    }

    /**
     * Creates a component placeholder.
     *
     * @param key       The placeholder key
     * @param component The component value
     * @return The tag resolver
     */
    @Contract("_, _ -> new")
    public static @NotNull TagResolver placeholder(final String key, final Component component) {
        return Placeholder.component(key, component);
    }

    /**
     * Converts a legacy color code string to a MiniMessage format string.
     * This is a utility for migrating old configurations.
     *
     * @param legacy The legacy formatted string
     * @return The MiniMessage formatted string
     */
    public static @NotNull String legacyToMiniMessage(final String legacy) {
        final Component component = LEGACY_SERIALIZER.deserialize(legacy);
        return MINI_MESSAGE.serialize(component);
    }

    /**
     * Converts a MiniMessage format string to a legacy color code string.
     * This is for backwards compatibility with systems expecting legacy format.
     *
     * @param miniMessage The MiniMessage formatted string
     * @return The legacy formatted string
     */
    public static @NotNull String miniMessageToLegacy(final String miniMessage) {
        final Component component = MINI_MESSAGE.deserialize(miniMessage);
        return LEGACY_SERIALIZER.serialize(component);
    }

    /**
     * Broadcasts a message to all online players and console who have the specified permission.
     *
     * @param message    The message to broadcast (MiniMessage format or legacy & codes)
     * @param permission The permission required to receive the message
     */
    public static void broadcast(final String message, final String permission) {
        if (audiences == null) {
            throw new IllegalStateException("BukkitAudiences not initialized!");
        }

        Audience filtered = audiences.filter(audience ->
                audience.hasPermission(permission)
        );

        send(filtered, message);
    }

    /**
     * Broadcasts a pre-parsed component to all who have the permission.
     *
     * @param component  The component to broadcast
     * @param permission The permission required to receive the message
     */
    public static void broadcast(final ComponentLike component, final String permission) {
        if (audiences == null) {
            throw new IllegalStateException("BukkitAudiences not initialized!");
        }

        Audience filtered = audiences.filter(audience ->
                audience.hasPermission(permission)
        );

        filtered.sendMessage(component);
    }

    /**
     * Returns the BukkitAudiences instance (for advanced use).
     * Only available after init() has been called.
     */
    public static @NotNull BukkitAudiences audiences() {
        if (audiences == null) {
            throw new IllegalStateException("BukkitAudiences not initialized! Call MessageUtil.init(plugin) in onEnable()");
        }
        return audiences;
    }
}