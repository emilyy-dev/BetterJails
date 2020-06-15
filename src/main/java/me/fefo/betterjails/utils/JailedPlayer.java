package me.fefo.betterjails.utils;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class JailedPlayer {
  // I have no idea what this was going to be for :\
  // Probably the player UUID mapped to a hash of something (????)
  private static final ConcurrentHashMap<UUID, Integer> somethingLmao = new ConcurrentHashMap<>();

  private final UUID uuid;
  private final String name;
  private final String jail;
  private final String jailedBy;
  private final long jailedUntil;
  private final boolean unjailed = false;
  private final Location lastLocation;
  private final String group;

  public JailedPlayer(@NotNull UUID uuid,
                      @NotNull String name,
                      @NotNull String jail,
                      @NotNull String jailedBy,
                      @NotNull Long secondsLeft,
                      @NotNull Location lastLocation,
                      @Nullable String group) {
    this.uuid = uuid;
    this.name = name;
    this.jail = jail;
    this.jailedBy = jailedBy;
    this.jailedUntil = Instant.now().toEpochMilli() + secondsLeft * 1000L;
    this.lastLocation = lastLocation;
    this.group = group;
  }
}
