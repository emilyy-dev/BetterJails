package me.fefo.betterjails.bukkit.platform;

import me.fefo.betterjails.common.abstraction.PlatformAdapter;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public final class BukkitPlatformAdapter implements PlatformAdapter<Player, Location, World> {

  @Override
  public Player adaptPlayer(@Nullable final me.fefo.betterjails.common.abstraction.Player player) {
    if (player == null) {
      return null;
    }
  }

  @Override
  public Location adaptLocation(@Nullable final me.fefo.betterjails.common.abstraction.Location location) {
    if (location == null) {
      return null;
    }
  }

  @Override
  public World adaptWorld(@Nullable final me.fefo.betterjails.common.abstraction.World world) {
    if (world == null) {
      return null;
    }
  }

  @Override
  public me.fefo.betterjails.common.abstraction.Player adaptPlayer(@Nullable final Player player) {
    if (player == null) {
      return null;
    }
  }

  @Override
  public me.fefo.betterjails.common.abstraction.Location adaptLocation(@Nullable final Location location) {
    if (location == null) {
      return null;
    }
  }

  @Override
  public me.fefo.betterjails.common.abstraction.World adaptWorld(@Nullable final World world) {
    if (world == null) {
      return null;
    }
  }
}
