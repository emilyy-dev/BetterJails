//
// This file is part of BetterJails, licensed under the MIT License.
//
// Copyright (c) 2025 emilyy-dev
// Copyright (c) 2024 Emilia Kond
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

package io.github.emilyydev.betterjails.listeners;

import com.earth2me.essentials.User;
import com.github.fefo.betterjails.api.util.ImmutableLocation;
import io.github.emilyydev.betterjails.BetterJailsPlugin;
import io.github.emilyydev.betterjails.UpdateChecker;
import io.github.emilyydev.betterjails.api.impl.model.prisoner.ApiPrisoner;
import io.github.emilyydev.betterjails.config.SubCommandsConfiguration;
import io.github.emilyydev.betterjails.util.Util;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.UUID;

import static java.lang.invoke.MethodType.methodType;

public final class PlayerListeners implements Listener {

    private static final Logger LOGGER = LoggerFactory.getLogger("BetterJails");
    private static final MethodHandle SPAWN_EVENT_HANDLER;

    static {
        final MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle handler;

        try {
            // Try to use the new AsyncPlayerSpawnLocationEvent (1.21.4+)
            final Class<?> asyncEventClass = Class.forName("io.papermc.paper.event.player.AsyncPlayerSpawnLocationEvent");
            handler = lookup.findVirtual(PlayerListeners.class, "handleAsyncSpawnEvent", methodType(void.class, asyncEventClass));
            LOGGER.info("Using AsyncPlayerSpawnLocationEvent for spawn handling");
        } catch (final ClassNotFoundException | NoSuchMethodException | IllegalAccessException ex) {
            // Fallback to deprecated PlayerSpawnLocationEvent
            try {
                handler = lookup.findVirtual(PlayerListeners.class, "handleLegacySpawnEvent", methodType(void.class, PlayerSpawnLocationEvent.class));
                LOGGER.info("Using legacy PlayerSpawnLocationEvent for spawn handling");
            } catch (final NoSuchMethodException | IllegalAccessException ex2) {
                throw new ExceptionInInitializerError(ex2);
            }
        }

        SPAWN_EVENT_HANDLER = handler;
    }

    private final BetterJailsPlugin plugin;

    private PlayerListeners(final BetterJailsPlugin plugin) {
        this.plugin = plugin;
    }

    @Contract(value = "_ -> new", pure = true)
    public static @NotNull PlayerListeners create(final BetterJailsPlugin plugin) {
        return new PlayerListeners(plugin);
    }

    public void register() {
        final PluginManager pluginManager = this.plugin.getServer().getPluginManager();

        // Register the appropriate spawn event based on Paper version
        try {
            final Class<?> asyncEventClass = Class.forName("io.papermc.paper.event.player.AsyncPlayerSpawnLocationEvent");
            pluginManager.registerEvent(
                    (Class<? extends Event>) asyncEventClass, this, EventPriority.HIGH,
                    (l, e) -> {
                        try {
                            SPAWN_EVENT_HANDLER.invoke(l, e);
                        } catch (final Throwable throwable) {
                            throw new RuntimeException(throwable);
                        }
                    }, this.plugin
            );
        } catch (final ClassNotFoundException ex) {
            // Use legacy event
            pluginManager.registerEvent(
                    org.spigotmc.event.player.PlayerSpawnLocationEvent.class, this, EventPriority.HIGH,
                    (l, e) -> {
                        try {
                            SPAWN_EVENT_HANDLER.invoke(l, e);
                        } catch (final Throwable throwable) {
                            throw new RuntimeException(throwable);
                        }
                    }, this.plugin
            );
        }

        pluginManager.registerEvent(
                PlayerJoinEvent.class, this, EventPriority.NORMAL,
                (l, e) -> ((PlayerListeners) l).playerJoin((PlayerJoinEvent) e), this.plugin
        );
        pluginManager.registerEvent(
                PlayerQuitEvent.class, this, EventPriority.NORMAL,
                (l, e) -> ((PlayerListeners) l).playerQuit((PlayerQuitEvent) e), this.plugin
        );
        pluginManager.registerEvent(
                PlayerRespawnEvent.class, this, EventPriority.HIGH,
                (l, e) -> ((PlayerListeners) l).playerRespawn((PlayerRespawnEvent) e), this.plugin
        );
    }

    /**
     * Handles the new AsyncPlayerSpawnLocationEvent (Paper 1.21.4+)
     * This method is called via reflection.
     */
    @SuppressWarnings("unused")
    private void handleAsyncSpawnEvent(final Object event) {
        try {
            final MethodHandles.Lookup lookup = MethodHandles.lookup();
            final Class<?> eventClass = event.getClass();

            final MethodHandle getPlayer = lookup.findVirtual(eventClass, "getPlayer", methodType(Player.class));
            final MethodHandle getSpawnLocation = lookup.findVirtual(eventClass, "getSpawnLocation", methodType(Location.class));
            final MethodHandle setSpawnLocation = lookup.findVirtual(eventClass, "setSpawnLocation", methodType(void.class, Location.class));

            final Player player = (Player) getPlayer.invoke(event);
            final Location currentLocation = (Location) getSpawnLocation.invoke(event);

            final Location newLocation = handlePlayerSpawn(player, currentLocation);
            if (newLocation != null) {
                setSpawnLocation.invoke(event, newLocation);
            }
        } catch (final Throwable throwable) {
            LOGGER.error("Error handling async spawn event", throwable);
        }
    }

    /**
     * Handles the legacy PlayerSpawnLocationEvent (pre-1.21.4)
     * This method is called via reflection.
     */
    @SuppressWarnings("unused")
    private void handleLegacySpawnEvent(final @NotNull PlayerSpawnLocationEvent event) {
        final Player player = event.getPlayer();
        final Location currentLocation = event.getSpawnLocation();

        final Location newLocation = handlePlayerSpawn(player, currentLocation);
        if (newLocation != null) {
            event.setSpawnLocation(newLocation);
        }
    }

    /**
     * Common spawn handling logic for both event types.
     *
     * @param player          The player spawning
     * @param currentLocation The current spawn location
     * @return The new spawn location, or null if no change is needed
     */
    private @Nullable Location handlePlayerSpawn(final @NotNull Player player, final Location currentLocation) {
        final UUID uuid = player.getUniqueId();

        ApiPrisoner prisoner = this.plugin.prisonerData().getPrisoner(uuid);
        if (prisoner == null) {
            return null;
        }

        if (prisoner.released() || player.hasPermission("betterjails.jail.exempt")) {
            // The player has been released...
            // put them back where they were if there is no release location, and at the release location otherwise
            final ImmutableLocation lastLocation = prisoner.lastLocationNullable();
            final ImmutableLocation releaseLocation = prisoner.jail().releaseLocation();

            final Location newLocation;
            if (releaseLocation != null) {
                newLocation = releaseLocation.mutable();
            } else if (lastLocation != null) {
                newLocation = lastLocation.mutable();
            } else {
                newLocation = null;
            }

            this.plugin.prisonerData().releaseJailedPlayer(player, Util.NIL_UUID, null, false);
            return newLocation;
        }

        if (prisoner.unknownLastLocation()) {
            prisoner = prisoner.withLastLocation(ImmutableLocation.copyOf(currentLocation));

            // Must be delayed by 1 tick, otherwise player.isOnline() is false and stuff explodes
            final String jailedBy = prisoner.jailedBy() == null ? "" : prisoner.jailedBy();
            this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () -> {
                final SubCommandsConfiguration.SubCommands subCommands = this.plugin.subCommands().onJail();
                subCommands.executeAsPrisoner(this.plugin.getServer(), player, jailedBy);
                subCommands.executeAsConsole(this.plugin.getServer(), player, jailedBy);
            }, 1);
        }

        prisoner = prisoner.withTimeRunning();
        this.plugin.prisonerData().savePrisoner(prisoner).exceptionally(error -> {
            LOGGER.error("An error occurred saving data for prisoner {}", uuid, error);
            return null;
        });

        return prisoner.jail().location().mutable();
    }

    private void playerJoin(final @NotNull PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        final UUID uuid = player.getUniqueId();
        if (player.hasPermission("betterjails.receivebroadcast")) {
            this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () ->
                    UpdateChecker.fetchRemoteVersion(this.plugin).thenAccept(version -> {
                        final boolean versionChanged = !this.plugin.getDescription().getVersion().equals(version);
                        final Player callbackPlayer = this.plugin.getServer().getPlayer(uuid);
                        if (versionChanged && callbackPlayer != null) {
                            callbackPlayer.sendMessage(
                                    Util.color("&7[&bBetterJails&7] &3New version &b%s &3for &bBetterJails &3available.", version)
                            );
                        }
                    }), 100L);
        }
    }

    private void playerQuit(final @NotNull PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        final UUID uuid = player.getUniqueId();
        ApiPrisoner prisoner = this.plugin.prisonerData().getPrisoner(uuid);
        if (prisoner == null) {
            return;
        }

        if (!this.plugin.configuration().considerOfflineTime()) {
            prisoner = prisoner.withTimePaused();
            if (this.plugin.essentials != null) {
                final User user = this.plugin.essentials.getUser(uuid);
                user.setJailTimeout(0L);
                user.setJailed(true);
            }
        }

        this.plugin.prisonerData().savePrisoner(prisoner).exceptionally(error -> {
            LOGGER.error("An error occurred saving data for prisoner {}", uuid, error);
            return null;
        });
    }

    private void playerRespawn(final @NotNull PlayerRespawnEvent event) {
        final Player player = event.getPlayer();
        final UUID uuid = player.getUniqueId();
        final ApiPrisoner prisoner = this.plugin.prisonerData().getPrisoner(uuid);

        if (prisoner != null) {
            event.setRespawnLocation(prisoner.jail().location().mutable());
        }
    }
}