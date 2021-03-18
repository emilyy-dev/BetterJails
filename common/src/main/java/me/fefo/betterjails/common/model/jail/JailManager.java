package me.fefo.betterjails.common.model.jail;

import static me.fefo.betterjails.common.util.Utils.fireCancellableEvent;
import static me.fefo.betterjails.common.util.Utils.sanitize;

import java.util.Hashtable;
import java.util.Map;
import me.fefo.betterjails.bukkit.BetterJailsBukkit;
import me.fefo.betterjails.common.abstraction.Player;
import me.fefo.betterjails.common.events.JailCreationEvent;
import me.fefo.betterjails.common.logging.LogTypes;
import me.fefo.betterjails.common.logging.SQLite;
import me.fefo.betterjails.common.storage.StorageProvider;
import org.apache.commons.lang.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class JailManager {
    private static final String VALID_JAIL_NAME = "^[a-zA-Z0-9-_]{1,32}$";

    private final StorageProvider storage;
    private final Map<String, Jail> loadedJails = new Hashtable<>();
    private final BetterJailsBukkit bjb;

    public JailManager(final StorageProvider storage, final BetterJailsBukkit bjb) {
        this.storage = storage;
        this.bjb = bjb;
    }

    /**
     * Creates and saves into the cache a new jail with the given name.
     * @param name The name of the jail.
     * @param overwrite Whether or not to write over an existing jail with the new one.
     * @return The newly created jail, or the old jail if overwrite is set to true and
     * there's a jail with that name already.
     * @throws IllegalArgumentException If the name is not valid for use.
     */
    public @Nullable Jail createJail(@NotNull final String name,
                                    final Player creator,
                                    final boolean overwrite) throws IllegalArgumentException {
        Validate.notNull(name);

        final String sanitized = sanitize(name);
        if (!sanitized.matches(VALID_JAIL_NAME)) {
            throw new IllegalArgumentException("Jail name \"" + name + "\" is not valid");
        }

        final Jail jail = new Jail(sanitized);
        final JailCreationEvent jce = new JailCreationEvent(
            creator.getUuid(),
            sanitized,
            System.currentTimeMillis()
        );

        if (fireCancellableEvent(jce)) {
            bjb.getSqLite().log(LogTypes.CancelledJailCreation, jce);
            return null;
        }

        if (overwrite) {
            loadedJails.put(sanitized, jail);
        } else {
            loadedJails.putIfAbsent(sanitized, jail);
        }

        bjb.getSqLite().log(LogTypes.CreateJail, jce);

        return loadedJails.get(sanitized);
    }

    public @Nullable Jail getJail(@NotNull final String name) {
        Validate.notNull(name, "Jail name cannot be null.");
        return loadedJails.get(sanitize(name));
    }
}
