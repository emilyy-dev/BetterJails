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

package com.github.fefo6644.betterjails.common.storage;

import com.github.fefo6644.betterjails.common.model.cell.Cell;
import com.github.fefo6644.betterjails.common.model.prisoner.Prisoner;
import com.google.common.collect.BiMap;

import java.io.IOException;
import java.util.Collection;
import java.util.UUID;

public interface StorageProvider {

  /* cell methods */
  Collection<Cell> loadAllCells() throws IOException;
  void loadCell(String name) throws IOException;
  void saveCell(Cell cell) throws IOException;

  /* prisoner methods */
  <P> Collection<Prisoner<P>> loadAllPrisoners() throws IOException;
  <P> Prisoner<P> loadPrisoner(UUID uuid) throws IOException;
  <P> void savePrisoner(Prisoner<P> prisoner) throws IOException;

  /* uuid <--> name bimap cache methods */
  BiMap<UUID, String> loadAllPlayers() throws IOException;
  UUID lookupUuid(String name) throws IOException;
  String lookupName(UUID uuid) throws IOException;
  void savePlayer(UUID uuid, String name) throws IOException;
}
