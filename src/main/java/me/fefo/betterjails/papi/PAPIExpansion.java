package me.fefo.betterjails.papi;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.fefo.betterjails.Main;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class PAPIExpansion extends PlaceholderExpansion {
  private static final String TOTAL_JAILED_PLAYERS = "total_jailed_players";
  private static final String ONLINE_JAILED_PLAYERS = "online_jailed_players";
  private static final String TOTAL_JAILS = "total_jails";
  private final Main main;

  public PAPIExpansion(@NotNull Main main) { this.main = main; }

  @Override
  public String getIdentifier() { return "betterjails"; }

  @Override
  public String getAuthor() { return main.getDescription().getAuthors().get(0); }

  @Override
  public String getVersion() { return main.getDescription().getVersion(); }

  @Override
  public String onPlaceholderRequest(@Nullable Player p,
                                     @NotNull String placeholder) {
    switch (placeholder) {
      case TOTAL_JAILED_PLAYERS:
        return String.valueOf(main.dataHandler.getTotalJailedPlayers());

      case ONLINE_JAILED_PLAYERS:
        int c = 0;
        for (Player player : main.getServer().getOnlinePlayers()) {
          if (main.dataHandler.isPlayerJailed(player.getUniqueId())) {
            ++c;
          }
        }
        return String.valueOf(c);

      case TOTAL_JAILS:
        return String.valueOf(main.dataHandler.getJails().size());

      default:
        return null;
    }
  }
}
