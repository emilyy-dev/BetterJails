package me.fefo.betterjails.common.abstraction;

public interface PlatformAdapter<P, L, W> {

  P adaptPlayer(Player player);
  L adaptLocation(Location location);
  W adaptWorld(World world);

  Player adaptPlayer(P player);
  Location adaptLocation(L location);
  World adaptWorld(W world);
}
