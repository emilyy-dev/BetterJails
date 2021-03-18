package me.fefo.betterjails.bukkit;

import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

public class Utils {
    public Utils() {

    }

    public static boolean fireCancellableEventBukkit(@NotNull Cancellable event) {
        Bukkit.getPluginManager().callEvent((Event) event);
        return event.isCancelled();
    }

}
