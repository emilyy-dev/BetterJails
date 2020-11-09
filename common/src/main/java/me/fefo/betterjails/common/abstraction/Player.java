package me.fefo.betterjails.common.abstraction;

import me.fefo.betterjails.common.model.prisoner.Prisoner;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identity;
import org.apache.commons.lang.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public abstract class Player extends MessagingSubject {

  private final UUID uuid;
  private final String name;

  public Player(@NotNull final UUID uuid, @Nullable final String name, @NotNull final Audience audience) {
    super(audience, Identity.identity(uuid));

    Validate.notNull(uuid);
    Validate.notNull(name);

    this.uuid = uuid;
    this.name = name;
  }

  public @NotNull UUID getUuid() {
    return uuid;
  }

  public @Nullable String getName() {
    return name;
  }

  @Override
  public String name() {
    return name;
  }

  abstract void teleport(Location location);

  public boolean isJailed() {
    return false;
  }

  public Prisoner asPrisoner() {
    return isJailed() ? (Prisoner) this : null;
  }
}
