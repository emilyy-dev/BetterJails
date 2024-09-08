//
// This file is part of BetterJails, licensed under the MIT License.
//
// Copyright (c) 2024 emilyy-dev
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

package io.github.emilyydev.betterjails.interfaces.storage;

import com.github.fefo.betterjails.api.model.jail.Jail;
import io.github.emilyydev.betterjails.api.impl.model.prisoner.ApiPrisoner;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface StorageInterface {

  void savePrisoner(ApiPrisoner prisoner) throws Exception;
  void savePrisoners(Map<UUID, ApiPrisoner> prisoners) throws Exception;
  void deletePrisoner(ApiPrisoner prisoner) throws Exception;
  Map<UUID, ApiPrisoner> loadPrisoners() throws Exception;

  void saveJail(Jail jail) throws Exception;
  void saveJails(Map<String, Jail> jails) throws Exception;
  void deleteJail(Jail jail) throws Exception;
  Map<String, Jail> loadJails() throws Exception;

  // TODO(rymiel): There's asymmetry between these: ApiPrisoner vs Jail, this is because not all data needed here
  //   is stored in Prisoner. Perhaps some stuff from ApiPrisoner should be exposed in Prisoner.
}
