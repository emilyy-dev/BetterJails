package me.fefo.betterjails.util;

import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.Hashtable;
import java.util.Map;
import java.util.UUID;

public final class JailedPlayer {
  private final Map<Player, Jail> JAILED_PLAYERS = new Hashtable<>();

  private final UUID uuid;
  private final String name;
  private final String jail;
  private final String jailedBy;
  private final long jailedUntil;
  private final boolean unjailed = false;
  private final Location lastLocation;
  private final String group;

  public JailedPlayer(@NotNull final UUID uuid,
                      @NotNull final String name,
                      @NotNull final String jail,
                      @NotNull final String jailedBy,
                      @NotNull final Location lastLocation,
                      final long secondsLeft,
                      @Nullable final String group) {
    Validate.notNull(uuid);
    Validate.notNull(name);
    Validate.notNull(jail);
    Validate.notNull(jailedBy);
    Validate.notNull(lastLocation);

    this.uuid = uuid;
    this.name = name;
    this.jail = jail;
    this.jailedBy = jailedBy;
    this.lastLocation = lastLocation;
    this.jailedUntil = Instant.now().toEpochMilli() + secondsLeft * 1000L;
    this.group = group;
  }

  public UUID getUuid() {
    return uuid;
  }
}
