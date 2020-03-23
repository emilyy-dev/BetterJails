package ar.fefo.betterjails.utils;

import org.bukkit.Location;

public class Jail {
    private String name;
    private Location location;

    Jail(String name, Location location) {
        this.name = name;
        this.location = location;
    }

    public Location getLocation() {
        return location;
    }

    public String getName() {
        return name;
    }
}
