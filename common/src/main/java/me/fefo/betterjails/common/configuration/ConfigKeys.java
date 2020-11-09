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

package me.fefo.betterjails.common.configuration;

import me.fefo.betterjails.common.model.BackupLocation;
import me.fefo.betterjails.common.util.CollectionUtils;

import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import static me.fefo.betterjails.common.configuration.ConfigKey.configKey;
import static me.fefo.betterjails.common.configuration.ConfigKey.notReloadable;

public final class ConfigKeys {
//
//  /**
//   *
//   */
//  public static final ConfigKey<BackupLocation> BACKUP_LOCATION =
//      configKey("backup-location", BackupLocation.from("world", 0.0, 0.0, 0.0, 0.0f, 0.0f), BackupLocation::create);
//
//  /**
//   *
//   */
//  public static final ConfigKey<Boolean> OFFLINE_TIME = notReloadable("offline-time", Boolean.FALSE, getBoolean());
//
//  /**
//   *
//   */
//  public static final ConfigKey<Duration> DISCARD_DATA_AFTER = configKey("discard-data-after", Duration.ZERO,
//                                                                                 input -> Duration.ZERO); // will figure out later
//
//  /**
//   *
//   */
//  public static final ConfigKey<Boolean> CHANGE_GROUP = notReloadable("change-group", Boolean.FALSE, getBoolean());
//
//  /**
//   *
//   */
//  public static final ConfigKey<String> PRISONER_GROUP = notReloadable("prisoner-group", "prisoner", Function.identity());
//
//  /**
//   *
//   */
//  public static final ConfigKey<GroupChangingBehavior> GROUP_CHANGING_BEHAVIOR =
//      notReloadable("group-changing-behavior", GroupChangingBehavior.ADD, GroupChangingBehavior::find);
//
//  /**
//   *
//   */
//  public static final ConfigKey<List<String>, List<String>> GROUPS_WHITELIST = notReloadable("groups-whitelist", Collections.emptyList(,
//                                                                                                       Function.identity()));
//
//  /**
//   *
//   */
//  public static final ConfigKey<List<String>, List<String>> GROUPS_BLACKLIST = notReloadable("groups-blacklist", Collections.emptyList(),
//                                                                                                       Function.identity());
//
//  /**
//   *
//   */
//  public static final ConfigKey<Boolean, Boolean> CELL_SPECIFIC_PERMISSIONS = configKey("cell-specific-permissions", Boolean.FALSE, Function.identity());
//
//  /**
//   *
//   */
//  public static final ConfigKey<ListingStyle, String> LISTJAILS_STYLE = configKey("listjails-style", ListingStyle.LINE, ListingStyle::find);
//
//  /**
//   *
//   */
//  public static final ConfigKey<String, String> LISTJAILS_COLOR = configKey("listjails-color", "&a", Function.identity());
//
//  /**
//   *
//   */
//  public static final ConfigKey<Set<ConfigKey<String, String>>, ConfigurationSection> LISTJAILS_SEPARATORS =
//      configKey("listjails-separators",
//                CollectionUtils.immutableSet(LinkedHashSet::new,
//                                             configKey("listjails-separators.list", "&7· ", Function.identity()),
//                                             configKey("listjails-separators.line", "&7, ", Function.identity())),
//                ConfigKey::fromConfigurationSection);
}
