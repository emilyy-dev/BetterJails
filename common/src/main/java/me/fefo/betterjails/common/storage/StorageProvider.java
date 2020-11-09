/*
 * This file is part of the BetterJails (https://github.com/Fefo6644/BetterJails).
 *
 *  Copyright (c) 2020 Fefo6644 <federico.lopez.1999@outlook.com>
 *  Copyright (c) 2020 contributors
 *
 *  BetterJails is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, version 3.
 *
 *  BetterJails is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package me.fefo.betterjails.common.storage;

import org.apache.commons.lang.Validate;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public abstract class StorageProvider {

  protected final File file;
  protected final String what;

  protected StorageProvider(@NotNull final String what, @NotNull final File file) {
    Validate.notNull(what, "");
    Validate.notNull(file, "File name for " + what + " cannot be null");

    this.what = what;
    this.file = file;
  }

  public String getWhat() {
    return what;
  }

  public File getFile() {
    return file;
  }

  public abstract FileInputStream load() throws FileNotFoundException;
  public abstract void save();
}
