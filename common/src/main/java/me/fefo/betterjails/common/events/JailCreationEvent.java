package me.fefo.betterjails.common.events;

import java.sql.Time;
import java.util.UUID;
import org.bukkit.event.Cancellable;

public class JailCreationEvent extends BJEvent implements Cancellable {
    private final UUID creator;
    private final String name;
    private final long timestamp;

    public JailCreationEvent(UUID creator, String name, long timestamp) {
        this.creator = creator;
        this.name = name;
        this.timestamp = timestamp;
    }

    public UUID getCreator() {
        return creator;
    }

    public String getName() {
        return name;
    }

    public long getTimestamp() {
        return timestamp;
    }


    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public void setCancelled(boolean cancel) {

    }
}
