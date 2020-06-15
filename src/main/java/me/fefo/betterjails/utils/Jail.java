package me.fefo.betterjails.utils;

import org.bukkit.Location;

public final class Jail {
  private final String name;
  private final Location location;

  public Jail(String name, Location location) {
    this.name = name;
    this.location = location;
  }

  public Location getLocation() { return location; }

  public String getName() { return name; }
}
