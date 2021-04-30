//
// This file is part of BetterJails, licensed under the MIT License.
//
// Copyright (c) 2021 emilyy-dev
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

package io.github.emilyydev.betterjails.api.impl.model.prisoner;

import io.github.emilyydev.betterjails.BetterJailsPlugin;
import com.github.fefo.betterjails.api.model.jail.Jail;
import com.github.fefo.betterjails.api.model.prisoner.Prisoner;
import com.github.fefo.betterjails.api.model.prisoner.PrisonerManager;
import com.github.fefo.betterjails.api.util.ImmutableLocation;
import io.github.emilyydev.betterjails.util.DataHandler;
import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.Configuration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public class ApiPrisonerManager implements PrisonerManager {

  private final BetterJailsPlugin plugin;

  public ApiPrisonerManager(final BetterJailsPlugin plugin) {
    this.plugin = plugin;
  }

  @Override
  public @Nullable Prisoner getPrisoner(final @NotNull UUID uuid) {
    Objects.requireNonNull(uuid, "uuid");
    final Configuration config = this.plugin.dataHandler.retrieveJailedPlayer(uuid);
    if (!config.contains(DataHandler.FIELD_UUID)) {
      return null;
    }

    final ImmutableLocation lastLocation = ImmutableLocation.copyOf((Location) config.get(DataHandler.FIELD_LASTLOCATION));
    final String group = config.getString(DataHandler.FIELD_GROUP);
    final String name = config.getString(DataHandler.FIELD_NAME);
    final Jail jail = this.plugin.dataHandler.getJail(config.getString(DataHandler.FIELD_JAIL));
    final String jailedBy = config.getString(DataHandler.FIELD_JAILEDBY);
    final Instant jailedUntil = config.getBoolean(DataHandler.FIELD_UNJAILED)
                                ? Instant.MIN
                                : Instant.now().plusSeconds(config.getLong(DataHandler.FIELD_SECONDSLEFT));

    return new ApiPrisoner(uuid, name, group, jail, jailedBy, jailedUntil, lastLocation);
  }

  @Override
  @SuppressWarnings("ConstantConditions")
  public @NotNull Prisoner jailPlayer(final @NotNull UUID uuid, final @NotNull Jail jail, final @NotNull Duration duration) {
    Objects.requireNonNull(uuid, "uuid");
    Objects.requireNonNull(jail, "jail");
    Objects.requireNonNull(duration, "duration");

    final Instant now = Instant.now();
    final Instant jailedUntil = now.plus(duration);
    Preconditions.checkState(jailedUntil.isAfter(now), "duration must be positive");

    final OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
    try {
      this.plugin.dataHandler.addJailedPlayer(player, jail.name(), "api", duration.getSeconds());
    } catch (final IOException exception) {
      throw new RuntimeException(exception);
    }

    return getPrisoner(uuid);
  }

  @Override
  public boolean releasePrisoner(final @NotNull Prisoner prisoner) {
    Objects.requireNonNull(prisoner, "prisoner");
    return this.plugin.dataHandler.removeJailedPlayer(prisoner.uuid());
  }

  @Override
  public boolean isPlayerJailed(final @NotNull UUID uuid) {
    Objects.requireNonNull(uuid, "uuid");
    return this.plugin.dataHandler.isPlayerJailed(uuid);
  }

  @Override
  public @NotNull @Unmodifiable Collection<@NotNull Prisoner> getAllPrisoners() {
    return Collections.unmodifiableSet(this.plugin.dataHandler.getAllJailedPlayers()
                                                              .keySet().stream()
                                                              .map(this::getPrisoner)
                                                              .filter(Objects::nonNull)
                                                              .collect(Collectors.toSet()));
  }
}
