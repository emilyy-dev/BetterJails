//
// This file is part of BetterJails, licensed under the MIT License.
//
// Copyright (c) 2024 emilyy-dev
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

import com.github.fefo.betterjails.api.model.jail.Jail;
import com.github.fefo.betterjails.api.model.prisoner.Prisoner;
import com.github.fefo.betterjails.api.model.prisoner.PrisonerManager;
import com.github.fefo.betterjails.api.util.ImmutableLocation;
import com.google.common.base.Preconditions;
import io.github.emilyydev.betterjails.BetterJailsPlugin;
import io.github.emilyydev.betterjails.util.DataHandler;
import io.github.emilyydev.betterjails.util.Util;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.Configuration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ApiPrisonerManager implements PrisonerManager {

  private final BetterJailsPlugin plugin;

  public ApiPrisonerManager(final BetterJailsPlugin plugin) {
    this.plugin = plugin;
  }

  @Override
  public @Nullable Prisoner getPrisoner(final @NotNull UUID uuid) {
    Objects.requireNonNull(uuid, "uuid");
    final Configuration config = this.plugin.dataHandler().retrieveJailedPlayer(uuid);
    if (!config.contains(DataHandler.UUID_FIELD)) {
      return null;
    }

    final ImmutableLocation lastLocation = ImmutableLocation.copyOf((Location) config.get(DataHandler.LAST_LOCATION_FIELD));
    final String name = config.getString(DataHandler.NAME_FIELD);
    final String group = config.getString(DataHandler.GROUP_FIELD);
    final List<String> parentGroups = config.getStringList(DataHandler.EXTRA_GROUPS_FIELD);
    final Jail jail = this.plugin.dataHandler().getJail(config.getString(DataHandler.JAIL_FIELD));
    final String jailedBy = config.getString(DataHandler.JAILED_BY_FIELD);
    final Instant jailedUntil =
        config.getBoolean(DataHandler.IS_RELEASED_FIELD)
            ? Instant.MIN
            : Instant.now().plusSeconds(this.plugin.dataHandler().getSecondsLeft(uuid, 0));

    return new ApiPrisoner(uuid, name, group, parentGroups, jail, jailedBy, jailedUntil, lastLocation);
  }

  @Override
  @SuppressWarnings("ConstantConditions")
  public @NotNull Prisoner jailPlayer(final @NotNull UUID uuid, final @NotNull Jail jail,
      final @NotNull Duration duration) {
    Objects.requireNonNull(uuid, "uuid");
    Objects.requireNonNull(jail, "jail");
    Objects.requireNonNull(duration, "duration");

    final Instant now = Instant.now();
    final Instant jailedUntil = now.plus(duration);
    Preconditions.checkState(jailedUntil.isAfter(now), "duration must be positive");

    final OfflinePlayer player = this.plugin.getServer().getOfflinePlayer(uuid);
    this.plugin.dataHandler().addJailedPlayer(player, jail.name(), Util.NIL_UUID, "api", duration.getSeconds(), true, player.getLocation());
    return getPrisoner(uuid);
  }

  @Override
  public boolean releasePrisoner(final @NotNull Prisoner prisoner) {
    Objects.requireNonNull(prisoner, "prisoner");
    return this.plugin.dataHandler().releaseJailedPlayer(prisoner.uuid(), Util.NIL_UUID, "api", true);
  }

  @Override
  public boolean isPlayerJailed(final @NotNull UUID uuid) {
    Objects.requireNonNull(uuid, "uuid");
    return this.plugin.dataHandler().isPlayerJailed(uuid);
  }

  @Override
  public @NotNull @Unmodifiable Collection<@NotNull Prisoner> getAllPrisoners() {
    return this.plugin.dataHandler().getAllJailedPlayers().keySet().stream()
        .map(this::getPrisoner)
        .filter(Objects::nonNull)
        .collect(Util.toImmutableSet());
  }
}
