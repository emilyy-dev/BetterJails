package me.fefo.betterjails.common.model.prisoner;

import me.fefo.betterjails.common.model.cell.Cell;

import java.util.Hashtable;
import java.util.Map;

public final class PrisonerManager {

  private final Map<Prisoner, Cell> jailedPlayers = new Hashtable<>();

  public PrisonerManager() {

  }
}
