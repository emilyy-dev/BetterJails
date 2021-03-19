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

package com.github.fefo6644.betterjails.common.platform;

import com.github.fefo6644.betterjails.common.configuration.ConfigurationAdapter;
import com.github.fefo6644.betterjails.common.message.MessagingSubject;
import com.github.fefo6644.betterjails.common.model.cell.CellManager;
import com.github.fefo6644.betterjails.common.model.prisoner.PrisonerManager;
import com.github.fefo6644.betterjails.common.platform.abstraction.PlatformAdapter;
import com.github.fefo6644.betterjails.common.platform.abstraction.Player;
import com.github.fefo6644.betterjails.common.platform.abstraction.TaskScheduler;
import com.github.fefo6644.betterjails.common.storage.StorageProvider;
import com.google.common.collect.ImmutableList;
import net.kyori.adventure.platform.AudienceProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;

public class BetterJailsPlugin {

  public static final Logger LOGGER = LoggerFactory.getLogger(BetterJailsPlugin.class);

  private final BetterJailsBootstrap bootstrapPlugin;

  private ConfigurationAdapter configurationAdapter;
  private TaskScheduler taskScheduler;

  private StorageProvider storageProvider;
  private CellManager cellManager;
  private PrisonerManager prisonerManager;

  public BetterJailsPlugin(final BetterJailsBootstrap bootstrapPlugin) {
    this.bootstrapPlugin = bootstrapPlugin;
  }

  public void load() {
    this.taskScheduler = this.bootstrapPlugin.getTaskScheduler();
  }

  public void enable() {
    this.configurationAdapter = this.bootstrapPlugin.getConfigurationAdapter();
  }

  public void disable() {
    getAudienceProvider().close();
  }

  public @NotNull BetterJailsBootstrap getBootstrapPlugin() {
    return this.bootstrapPlugin;
  }

  public @NotNull AudienceProvider getAudienceProvider() {
    return this.bootstrapPlugin.getAudienceProvider();
  }

  public @NotNull Path getPluginFolder() {
    return this.bootstrapPlugin.getPluginFolder();
  }

  public @NotNull StorageProvider getStorageProvider() {
    return this.storageProvider;
  }

  public @NotNull CellManager getCellManager() {
    return this.cellManager;
  }

  public @NotNull PrisonerManager getPrisonerManager() {
    return this.prisonerManager;
  }

  public @NotNull ConfigurationAdapter getConfigurationAdapter() {
    return this.configurationAdapter;
  }

  public @NotNull TaskScheduler getTaskScheduler() {
    return this.taskScheduler;
  }

  public <S, P, L, W> @NotNull PlatformAdapter<S, P, L, W> getPlatformAdapter() {
    return this.bootstrapPlugin.getPlatformAdapter();
  }

  public @NotNull MessagingSubject getConsoleSubject() {
    return this.bootstrapPlugin.getConsoleSubject();
  }

  public @NotNull Logger getLogger() {
    return LOGGER;
  }

  public @NotNull String getVersion() {
    return this.bootstrapPlugin.getVersion();
  }

  public @NotNull List<String> getAuthors() {
    return ImmutableList.of("Fefo6644");
  }

  public @Nullable InputStream getResource(final String name) {
    return this.getClass().getClassLoader().getResourceAsStream(name);
  }

  public @NotNull List<Player> getOnlinePlayers() {
    return this.bootstrapPlugin.getOnlinePlayers();
  }
}
