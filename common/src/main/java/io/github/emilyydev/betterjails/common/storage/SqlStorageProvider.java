//
// This file is part of BetterJails, licensed under the MIT License.
//
// Copyright (c) emilyy-dev
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

package io.github.emilyydev.betterjails.common.storage;

import io.github.emilyydev.betterjails.common.model.cell.Cell;
import io.github.emilyydev.betterjails.common.model.prisoner.Prisoner;
import com.google.common.collect.BiMap;

import java.io.IOException;
import java.util.Collection;
import java.util.UUID;

/**
 * Generic storage provider for any SQL medium (H2, SQLite, MariaDB, PostgreSQL, MySQL).
 */
public class SqlStorageProvider implements StorageProvider {

  @Override
  public Collection<Cell> loadAllCells() throws IOException {
    return null;
  }

  @Override
  public void loadCell(final String name) throws IOException {

  }

  @Override
  public void saveCell(final Cell cell) throws IOException {

  }

  @Override
  public <P> Collection<Prisoner<P>> loadAllPrisoners() throws IOException {
    return null;
  }

  @Override
  public <P> Prisoner<P> loadPrisoner(final UUID uuid) throws IOException {
    return null;
  }

  @Override
  public <P> void savePrisoner(final Prisoner<P> prisoner) throws IOException {

  }

  @Override
  public BiMap<UUID, String> loadAllPlayers() throws IOException {
    return null;
  }

  @Override
  public UUID lookupUuid(final String name) throws IOException {
    return null;
  }

  @Override
  public String lookupName(final UUID uuid) throws IOException {
    return null;
  }

  @Override
  public void savePlayer(final UUID uuid, final String name) throws IOException {

  }
}
