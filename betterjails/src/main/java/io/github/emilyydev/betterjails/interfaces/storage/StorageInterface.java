package io.github.emilyydev.betterjails.interfaces.storage;

import com.github.fefo.betterjails.api.model.jail.Jail;
import io.github.emilyydev.betterjails.api.impl.model.prisoner.ApiPrisoner;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface StorageInterface {
  CompletableFuture<Void> savePrisoner(ApiPrisoner prisoner);
  CompletableFuture<Void> deletePrisoner(ApiPrisoner prisoner);
  CompletableFuture<Void> loadPrisoners(Map<UUID, ApiPrisoner> out);

  CompletableFuture<Void> saveJails(Map<String, Jail> jails);
  CompletableFuture<Void> loadJails(Map<String, Jail> out);

  // TODO(rymiel): There's asymmetry between these: "save one, delete one, load all" vs "save all, load all"
  //   there's also some extra asymmetry between ApiPrisoner vs Jail, this is because not all data needed here
  //   is stored in Prisoner. Perhaps some stuff from ApiPrisoner should be exposed in Prisoner.
  //   also, not sure if out parameters are a good idea.
}
