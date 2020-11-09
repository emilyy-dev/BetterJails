package me.fefo.betterjails.common.abstraction;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang.Validate;
import org.jetbrains.annotations.NotNull;

public abstract class MessagingSubject {

  private final Audience audience;
  private final Identity identity;

  public MessagingSubject(@NotNull final Audience audience, @NotNull final Identity identity) {
    Validate.notNull(audience, "Subject audience cannot be null");
    Validate.notNull(identity, "Subject identity cannot be null");

    this.audience = audience;
    this.identity = identity;
  }

  public void send(@NotNull final Component message) {
    audience.sendMessage(identity, message);
  }

  public abstract String name();
}
