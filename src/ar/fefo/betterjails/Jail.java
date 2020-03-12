package ar.fefo.betterjails;

import org.bukkit.Location;

class Jail {
    private String name;
    private Location location;

    Jail(String name, Location location) {
        this.name = name;
        this.location = location;
    }

    Location getLocation() { return location; }
    String getName() { return name; }
}
