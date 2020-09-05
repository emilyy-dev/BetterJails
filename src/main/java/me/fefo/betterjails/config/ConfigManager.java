package me.fefo.betterjails.config;

import me.fefo.betterjails.util.BackupLocation;

import java.util.Collections;
import java.util.Map;

import static me.fefo.betterjails.config.ConfigKey.configKey;

public final class ConfigManager {
  public static final ConfigKey<BackupLocation> BACKUP_LOCATION = configKey("backupLocation",
                                                                            BackupLocation.from("world",
                                                                                                0, 0, 0,
                                                                                                0, 0));
  public static final ConfigKey<Boolean> OFFLINE_TIME = configKey("offlineTime", false);
  public static final ConfigKey<Boolean> JAIL_EVEN_IF_THE_USER_NEVER_JOINED_THE_SERVER_BEFORE = configKey("jailEvenIfTheUserNeverJoinedTheServerBefore",
                                                                                                          false);
  public static final ConfigKey<Boolean> CHANGE_GROUP = configKey("changeGroup", false);
  public static final ConfigKey<String> PRISONER_GROUP = configKey("prisonerGroup", "prisoner");
  public static final ConfigKey<Integer> AUTO_SAVE_TIME_IN_MINUTES = configKey("autoSaveTimeInMinutes", 5);
  public static final ConfigKey<Map<String, String>> MESSAGES = configKey("messages", Collections.emptyMap());
}
