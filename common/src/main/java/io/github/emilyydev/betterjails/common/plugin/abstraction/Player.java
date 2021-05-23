//
// This file is part of BetterJails, licensed under the MIT License.
//
// Copyright (c) emilyy-dev
// Copyright (c) contributors
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.
//

package io.github.emilyydev.betterjails.common.plugin.abstraction;

import io.github.emilyydev.betterjails.common.message.Subject;
import io.github.emilyydev.betterjails.common.model.prisoner.Prisoner;
import net.kyori.adventure.audience.Audience;
import org.apache.commons.lang.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.UUID;

public abstract class Player<P> extends Subject {

  private final UUID uuid;
  private final WeakReference<P> playerReference;

  protected Player(final @NotNull UUID uuid, final @Nullable String name,
                   final @NotNull Audience audience, final @NotNull P player) {
    super(audience, name);
    Validate.notNull(uuid, "uuid");
    Validate.notNull(player, "player");

    this.uuid = uuid;
    this.playerReference = new WeakReference<>(player);
  }

  public @NotNull UUID uuid() {
    return this.uuid;
  }

  // Implementation detail: no-op if the player reference is null
  public abstract void teleport(Location location, World world);

  public boolean isJailed() {
    return false;
  }

  @Override
  public boolean isPlayerSubject() {
    return true;
  }

  @Override
  public Player<P> asPlayerSubject() {
    return this;
  }

  public Prisoner<P> asPrisoner() {
    return null;
  }

  protected final @Nullable P getPlayerHandle() {
    return this.playerReference.get();
  }
}
