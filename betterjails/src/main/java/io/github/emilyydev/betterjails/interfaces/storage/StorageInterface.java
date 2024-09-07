package io.github.emilyydev.betterjails.interfaces.storage;

import com.github.fefo.betterjails.api.model.jail.Jail;
import io.github.emilyydev.betterjails.api.impl.model.prisoner.ApiPrisoner;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface StorageInterface {
  CompletableFuture<Void> savePrisoner(ApiPrisoner prisoner);
  CompletableFuture<Void> savePrisoners(Map<UUID, ApiPrisoner> prisoners);
  CompletableFuture<Void> deletePrisoner(ApiPrisoner prisoner);
  CompletableFuture<Map<UUID, ApiPrisoner>> loadPrisoners();

  CompletableFuture<Void> saveJail(Jail jail);
  CompletableFuture<Void> saveJails(Map<String, Jail> jails);
  CompletableFuture<Void> deleteJail(Jail jail);
  CompletableFuture<Map<String, Jail>> loadJails();

  // TODO(rymiel): There's asymmetry between these: ApiPrisoner vs Jail, this is because not all data needed here
  //   is stored in Prisoner. Perhaps some stuff from ApiPrisoner should be exposed in Prisoner.
}
