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

package io.github.emilyydev.betterjails;

import com.github.fefo.betterjails.api.BetterJails;
import com.github.fefo.betterjails.api.event.plugin.PluginSaveDataEvent;
import com.github.fefo.betterjails.api.util.ImmutableLocation;
import io.github.emilyydev.betterjails.api.impl.BetterJailsApi;
import io.github.emilyydev.betterjails.api.impl.event.ApiEventBus;
import io.github.emilyydev.betterjails.api.impl.model.jail.ApiJailManager;
import io.github.emilyydev.betterjails.api.impl.model.prisoner.ApiPrisonerManager;
import io.github.emilyydev.betterjails.commands.CommandHandler;
import io.github.emilyydev.betterjails.commands.CommandTabCompleter;
import io.github.emilyydev.betterjails.config.BetterJailsConfiguration;
import io.github.emilyydev.betterjails.config.SubCommandsConfiguration;
import io.github.emilyydev.betterjails.data.JailDataHandler;
import io.github.emilyydev.betterjails.data.PrisonerDataHandler;
import io.github.emilyydev.betterjails.interfaces.permission.PermissionInterface;
import io.github.emilyydev.betterjails.interfaces.storage.BukkitConfigurationStorage;
import io.github.emilyydev.betterjails.interfaces.storage.StorageAccessor;
import io.github.emilyydev.betterjails.listeners.PlayerListeners;
import io.github.emilyydev.betterjails.listeners.PluginDisableListener;
import io.github.emilyydev.betterjails.util.Util;
import net.ess3.api.IEssentials;
import org.bstats.bukkit.Metrics;
import org.bukkit.Server;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

@SuppressWarnings("DesignForExtension")
public class BetterJailsPlugin extends JavaPlugin implements Executor {

  private static final Logger LOGGER = LoggerFactory.getLogger("BetterJails");

  static {
    ConfigurationSerialization.registerClass(ImmutableLocation.class);
  }

  public IEssentials essentials = null;

  private final Path pluginDir = getDataFolder().toPath();
  private final BetterJailsConfiguration configuration = new BetterJailsConfiguration(this.pluginDir);
  private final SubCommandsConfiguration subCommands = new SubCommandsConfiguration(this.pluginDir);
  private final StorageAccessor storageAccessor = new StorageAccessor(new BukkitConfigurationStorage(this));
  private final PrisonerDataHandler prisonerData = new PrisonerDataHandler(this);
  private final JailDataHandler jailData = new JailDataHandler(this);
  private final BetterJailsApi api = new BetterJailsApi(new ApiJailManager(this.jailData), new ApiPrisonerManager(this));
  private final ApiEventBus eventBus = this.api.getEventBus();
  private PermissionInterface permissionInterface = PermissionInterface.NULL;
  private Metrics metrics = null;

  public BetterJailsPlugin() {
    this.metrics = Util.prepareMetrics(this);
  }

  public BetterJailsPlugin(final String str) { // for mockbukkit, just a dummy ctor to not enable bstats
  }

  public PrisonerDataHandler prisonerData() {
    return this.prisonerData;
  }

  public JailDataHandler jailData() {
    return this.jailData;
  }

  public BetterJailsApi api() {
    return this.api;
  }

  public ApiEventBus eventBus() {
    return this.eventBus;
  }

  public PermissionInterface permissionInterface() {
    return this.permissionInterface;
  }

  public StorageAccessor storageAccessor() {
    return this.storageAccessor;
  }

  public void resetPermissionInterface(final PermissionInterface permissionInterface) {
    this.permissionInterface.close();
    this.permissionInterface = permissionInterface;
  }

  public BetterJailsConfiguration configuration() {
    return this.configuration;
  }

  public SubCommandsConfiguration subCommands() {
    return this.subCommands;
  }

  public Path getPluginDir() {
    return this.pluginDir;
  }

  @Override
  public void execute(final @NotNull Runnable command) {
    getServer().getScheduler().runTask(this, command);
  }

  @Override
  public void onLoad() {
    getServer().getServicesManager().register(BetterJails.class, this.api, this, ServicePriority.Normal);
  }

  @Override
  public void onEnable() {
    PluginDisableListener.create(this.eventBus).register(this);

    this.configuration.load();
    this.subCommands.load();

    final Server server = getServer();
    final PluginManager pluginManager = server.getPluginManager();
    if (pluginManager.isPluginEnabled("Essentials")) {
      this.essentials = (IEssentials) pluginManager.getPlugin("Essentials");
      LOGGER.info("Hooked with Essentials successfully!");
    }

    if (this.configuration.permissionHookEnabled()) {
      final Optional<String> maybePrisonerGroup = this.configuration.prisonerPermissionGroup();
      if (maybePrisonerGroup.isPresent()) {
        this.permissionInterface = PermissionInterface.determinePermissionInterface(this, maybePrisonerGroup.get());
        if (this.permissionInterface != PermissionInterface.NULL) {
          LOGGER.info("Hooked with \"{}\" successfully!", this.permissionInterface.name());
        } else {
          LOGGER.warn("Hook with a permission interface failed!");
          LOGGER.warn("Option \"changeGroup\" in config.yml is set to true but no supported permission plugin (or Vault) is installed");
          LOGGER.warn("Group changing feature will not be used!");
        }
      } else {
        LOGGER.warn("Option \"changeGroup\" in config.yml is set to true but no prisoner permission group was configured");
        LOGGER.warn("Group changing feature will not be used!");
      }
    }

    final BukkitScheduler scheduler = server.getScheduler();

    // delay data loading to allow plugins to load additional worlds that might have jails in them
    scheduler.runTask(this, () -> {
      try {
        alertNewConfigAvailable();
        // Jails must be loaded first, loading prisoners depends on jails already being loaded
        this.jailData.init();
        this.prisonerData.init();
      } catch (final IOException | InvalidConfigurationException | RuntimeException ex) {
        LOGGER.error("Error loading plugin data", ex);
        pluginManager.disablePlugin(this);
      }
    });

    PlayerListeners.create(this).register();

    final CommandHandler commandHandler = new CommandHandler(this);
    final CommandTabCompleter tabCompleter = new CommandTabCompleter(this);
    for (final String commandName : getDescription().getCommands().keySet()) {
      final PluginCommand command = getCommand(commandName);
      if (command != null) {
        command.setExecutor(commandHandler);
        command.setTabCompleter(tabCompleter);
      }
    }

    scheduler.runTaskTimer(this, this.prisonerData::timer, 0L, 20L);

    final Duration autoSavePeriod = this.configuration.autoSavePeriod();
    if (!autoSavePeriod.isZero()) {
      scheduler.runTaskTimer(this, this::saveAll, autoSavePeriod.getSeconds() * 20L, autoSavePeriod.getSeconds() * 20L);
    }

    if (!getDescription().getVersion().endsWith("-SNAPSHOT")) {
      scheduler.runTaskLater(this, () -> Util.checkVersion(this, version -> {
        if (!getDescription().getVersion().equals(version)) {
          server.getConsoleSender().sendMessage(Util.color("&7[&bBetterJails&7] &3New version &b%s &3for &bBetterJails &3available.", version));
        }
      }), 100L);
    }
  }

  @Override
  public void onDisable() {
    try {
      this.prisonerData.save().get();
      this.jailData.save().get();
    } catch (final InterruptedException | ExecutionException ex) {
      LOGGER.error("Could not save data files", ex);
    }

    try {
      this.storageAccessor.close();
    } catch (final InterruptedException ignored) {
    }

    this.eventBus.unsubscribeAll();
    if (this.metrics != null) {
      this.metrics.shutdown();
    }
  }

  public CompletableFuture<Void> saveAll() {
    final CompletableFuture<Void> prisonerSaveFuture = this.prisonerData.save();
    return this.jailData.save().thenCompose(v -> prisonerSaveFuture).whenCompleteAsync((v, error) -> {
      this.eventBus.post(PluginSaveDataEvent.class);
      if (error != null) {
        LOGGER.error("An error occurred while saving plugin data", error);
      }
    }, this);
  }

  public void reload() throws IOException {
    this.configuration.load();
    this.subCommands.load();
    this.prisonerData.reload();
    this.jailData.reload();

    if (this.configuration.permissionHookEnabled()) {
      this.configuration.prisonerPermissionGroup().ifPresent(prisonerGroup ->
          this.resetPermissionInterface(PermissionInterface.determinePermissionInterface(this, prisonerGroup))
      );
    } else {
      this.resetPermissionInterface(PermissionInterface.NULL);
    }
  }

  // TODO keep this or perform some kind of automatic migration?
  private void alertNewConfigAvailable() throws IOException, InvalidConfigurationException {
    final YamlConfiguration bundledConfig = new YamlConfiguration();
    try (
        final InputStream in = this.getResource("config.yml");
        final Reader reader = new InputStreamReader(Objects.requireNonNull(in, "bundled config not present"), StandardCharsets.UTF_8)
    ) {
      bundledConfig.load(reader);
    }

    final FileConfiguration existingConfig = this.getConfig();
    if (!bundledConfig.getKeys(true).equals(existingConfig.getKeys(true))) {
      LOGGER.warn("New config.yml found!");
      LOGGER.warn("Make sure to make a backup of your settings before deleting your current config.yml!");
    }
  }
}
