package me.fefo.betterjails.hook.papi;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.fefo.betterjails.BetterJails;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class PAPIExpansion extends PlaceholderExpansion {
  private static final String TOTAL_JAILED_PLAYERS = "total_jailed_players";
  private static final String ONLINE_JAILED_PLAYERS = "online_jailed_players";
  private static final String TOTAL_JAILS = "total_jails";
  private final BetterJails plugin;

  public PAPIExpansion(@NotNull BetterJails plugin) { this.plugin = plugin; }

  @Override
  public String getIdentifier() { return "betterjails"; }

  @Override
  public @NotNull String getAuthor() { return plugin.getDescription().getAuthors().get(0); }

  @Override
  public @NotNull String getVersion() { return plugin.getDescription().getVersion(); }

  @Override
  public String onPlaceholderRequest(@Nullable Player p,
                                     @NotNull String placeholder) {
    switch (placeholder) {
      default:
        return null;
    }
  }
}
