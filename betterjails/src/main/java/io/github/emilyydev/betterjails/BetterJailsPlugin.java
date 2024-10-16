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
import io.github.emilyydev.betterjails.commands.CommandError;
import io.github.emilyydev.betterjails.commands.CommandHandler;
import io.github.emilyydev.betterjails.config.BetterJailsConfiguration;
import io.github.emilyydev.betterjails.config.SubCommandsConfiguration;
import io.github.emilyydev.betterjails.data.JailDataHandler;
import io.github.emilyydev.betterjails.data.PrisonerDataHandler;
import io.github.emilyydev.betterjails.interfaces.permission.PermissionInterface;
import io.github.emilyydev.betterjails.interfaces.storage.BukkitConfigurationStorage;
import io.github.emilyydev.betterjails.interfaces.storage.StorageAccess;
import io.github.emilyydev.betterjails.listeners.PlayerListeners;
import io.github.emilyydev.betterjails.listeners.PluginDisableListener;
import io.github.emilyydev.betterjails.listeners.UniqueIdCache;
import io.github.emilyydev.betterjails.util.Util;
import net.ess3.api.IEssentials;
import org.bstats.bukkit.Metrics;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.incendo.cloud.annotations.AnnotationParser;
import org.incendo.cloud.bukkit.CloudBukkitCapabilities;
import org.incendo.cloud.caption.CaptionFormatter;
import org.incendo.cloud.exception.ArgumentParseException;
import org.incendo.cloud.exception.CommandExecutionException;
import org.incendo.cloud.exception.handling.ExceptionController;
import org.incendo.cloud.exception.handling.ExceptionHandler;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.paper.LegacyPaperCommandManager;
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
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.regex.Pattern;

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
  private final StorageAccess storageAccess = new StorageAccess(new BukkitConfigurationStorage(this));
  private final PrisonerDataHandler prisonerData = new PrisonerDataHandler(this);
  private final JailDataHandler jailData = new JailDataHandler(this);
  private final BetterJailsApi api = new BetterJailsApi(new ApiJailManager(this.jailData), new ApiPrisonerManager(this));
  private final ApiEventBus eventBus = this.api.getEventBus();
  private final UniqueIdCache uniqueIdCache = new UniqueIdCache();
  private final boolean isTesting;
  private PermissionInterface permissionInterface = PermissionInterface.NULL;
  private Metrics metrics = null;
  private boolean failedToLoad = false;

  public BetterJailsPlugin() {
    this.isTesting = false;
    this.metrics = PluginMetrics.prepareMetrics(this);
  }

  public BetterJailsPlugin(final String str) { // for mockbukkit, dummy ctor to not enable bstats and cloud
    this.isTesting = true;
  }

  public UUID findUniqueId(final String name) {
    return this.uniqueIdCache.findUniqueId(name);
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

  public StorageAccess storageAccess() {
    return this.storageAccess;
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
    try {
      this.configuration.loadWithDefaults();
      this.subCommands.load();

      alertNewConfigAvailable();
    } catch (final IOException | InvalidConfigurationException ex) {
      this.failedToLoad = true;
      LOGGER.error("The configuration failed to load, the plugin will not load", ex);
      return;
    }

    getServer().getServicesManager().register(BetterJails.class, this.api, this, ServicePriority.Normal);
  }

  @Override
  public void onEnable() {
    if (this.failedToLoad) {
      return;
    }

    PluginDisableListener.create(this.eventBus).register(this);
    this.uniqueIdCache.register(this);

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
        // Jails must be loaded first, loading prisoners depends on jails already being loaded
        this.jailData.load();
        this.prisonerData.load();
      } catch (final IOException | RuntimeException ex) {
        LOGGER.error("Error loading plugin data, the plugin will disable", ex);
        pluginManager.disablePlugin(this);
      }
    });

    PlayerListeners.create(this).register();

    // cloud shits the bed because of brig issues
    if (!this.isTesting) {
      setupCommandHandler();
    }

    scheduler.runTaskTimer(this, this.prisonerData::timer, 0L, 20L);

    final Duration autoSavePeriod = this.configuration.autoSavePeriod();
    if (!autoSavePeriod.isZero()) {
      scheduler.runTaskTimer(this, this::saveAll, autoSavePeriod.getSeconds() * 20L, autoSavePeriod.getSeconds() * 20L);
    }

    scheduler.runTaskLater(this, () -> UpdateChecker.fetchRemoteVersion(this).thenAccept(version -> {
      if (!getDescription().getVersion().equals(version)) {
        server.getConsoleSender().sendMessage(Util.color("&7[&bBetterJails&7] &3New version &b%s &3for &bBetterJails &3available.", version));
      }
    }), 100L);
  }

  @Override
  public void onDisable() {
    if (this.failedToLoad) {
      return;
    }

    try {
      this.prisonerData.save().get();
    } catch (final InterruptedException | ExecutionException ex) {
      LOGGER.error("Could not save prisoner data files", ex);
    }

    try {
      this.jailData.save().get();
    } catch (final InterruptedException | ExecutionException ex) {
      LOGGER.error("Could not save jails data file", ex);
    }

    try {
      this.storageAccess.close();
    } catch (final InterruptedException ignored) {
    }

    this.permissionInterface.close();

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

  public void reload() throws IOException, InvalidConfigurationException {
    this.configuration.loadWithDefaults();
    this.subCommands.load();

    // Jails must be loaded first, loading prisoners depends on jails already being loaded
    this.jailData.load();
    this.prisonerData.load();

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

  private void setupCommandHandler() {
    final LegacyPaperCommandManager<CommandSender> commandManager =
        LegacyPaperCommandManager.createNative(this, ExecutionCoordinator.simpleCoordinator());
    if (commandManager.hasCapability(CloudBukkitCapabilities.NATIVE_BRIGADIER)) {
      commandManager.registerBrigadier();
    }

    final ExceptionController<CommandSender> exceptionController = commandManager.exceptionController();
    exceptionController.registerHandler(CommandExecutionException.class, ExceptionHandler.unwrappingHandler(CommandError.class));
    exceptionController.registerHandler(ArgumentParseException.class, ExceptionHandler.unwrappingHandler(CommandError.class));

    final CaptionFormatter<CommandSender, String> placeholderReplacingFormatter = CaptionFormatter.patternReplacing(Pattern.compile("[<{](\\S+)[}>]"));
    // stinky
    commandManager.captionFormatter((captionKey, recipient, caption, variables) ->
        Util.color(placeholderReplacingFormatter.formatCaption(captionKey, recipient, caption, variables))
    );

    commandManager.captionRegistry().registerProvider(
        (caption, recipient) -> this.configuration.messages().messageFormat(caption.key())
    );

    new AnnotationParser<>(commandManager, CommandSender.class)
        .parse(new CommandHandler(this));
  }
}
