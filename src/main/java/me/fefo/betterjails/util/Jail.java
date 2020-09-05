package me.fefo.betterjails.util;

import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.Hashtable;
import java.util.Map;

public final class Jail {
  private static final Map<String, Jail> JAILS = new Hashtable<>();

  public static Jail getJail(@NotNull final String name) {
    return JAILS.get(name);
  }

  public static Jail createJail(@NotNull final String name,
                                @NotNull final Location location) {
    Validate.notNull(name);
    Validate.notNull(location);

    final Jail jail = new Jail(name, location);
    JAILS.put(name, jail);
    return jail;
  }

  private final String name;
  private final Location location;

  private Jail(String name, Location location) {
    this.name = name;
    this.location = location;
  }

  public Location getLocation() { return location; }

  public String getName() { return name; }
}
