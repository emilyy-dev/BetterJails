//
// This file is part of BetterJails, licensed under the MIT License.
//
// Copyright (c) Fefo6644 <federico.lopez.1999@outlook.com>
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

package com.github.fefo6644.betterjails.bukkit.hook.papi;

import com.github.fefo6644.betterjails.common.platform.BetterJailsPlugin;
import com.google.common.base.Joiner;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class PapiExpansion extends PlaceholderExpansion {

  private static final Joiner JOINER = Joiner.on(", ");
  private static final String TOTAL_JAILED_PLAYERS = "total_jailed_players";
  private static final String ONLINE_JAILED_PLAYERS = "online_jailed_players";
  private static final String TOTAL_JAILS = "total_jails";
  private final BetterJailsPlugin plugin;

  public PapiExpansion(@NotNull final BetterJailsPlugin plugin) {
    this.plugin = plugin;
  }

  @Override
  public String getIdentifier() {
    return "betterjails"; }

  @Override
  public @NotNull String getAuthor() {
    return JOINER.join(this.plugin.getAuthors()); }

  @Override
  public @NotNull String getVersion() {
    return this.plugin.getVersion(); }

  @Override
  public String onPlaceholderRequest(@Nullable Player p, @NotNull String placeholder) {
    switch (placeholder) {
      default:
        return null;
    }
  }
}
