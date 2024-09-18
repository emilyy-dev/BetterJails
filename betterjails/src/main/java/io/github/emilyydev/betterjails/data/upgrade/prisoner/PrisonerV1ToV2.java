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

package io.github.emilyydev.betterjails.data.upgrade.prisoner;

import com.github.fefo.betterjails.api.util.ImmutableLocation;
import com.google.common.collect.ImmutableMap;
import io.github.emilyydev.betterjails.BetterJailsPlugin;
import io.github.emilyydev.betterjails.data.upgrade.DataUpgrader;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * * Simple upgrading for renaming fields to a more proper and conventional naming
 * * unjailed -> seconds-left = 0
 * * last-location = backup location -> no last location (unknown)
 * * last-location = null -> damn
 */
public final class PrisonerV1ToV2 implements DataUpgrader {

  private static final Logger LOGGER = LoggerFactory.getLogger("BetterJails");

  private static final String V1_UNJAILED_FIELD = "unjailed";
  private static final String V1_LASTLOCATION_FIELD = "lastlocation";
  private static final String V1_JAILEDBY_FIELD = "jailedby";
  private static final String V1_SECONDSLEFT_FIELD = "secondsleft";

  private static final String V2_UUID_FIELD = "uuid";
  private static final String V2_NAME_FIELD = "name";
  private static final String V2_LAST_LOCATION_FIELD = "last-location";
  private static final String V2_JAILED_BY_FIELD = "jailed-by";
  private static final String V2_SECONDS_LEFT_FIELD = "seconds-left";

  private static final Map<String, String> FIELD_MIGRATION_MAP =
      ImmutableMap.of(
          V1_LASTLOCATION_FIELD, V2_LAST_LOCATION_FIELD,
          V1_JAILEDBY_FIELD, V2_JAILED_BY_FIELD,
          V1_SECONDSLEFT_FIELD, V2_SECONDS_LEFT_FIELD
      );

  @Override
  public void upgrade(final ConfigurationSection config, final BetterJailsPlugin plugin) {
    for (final Map.Entry<String, String> entry : FIELD_MIGRATION_MAP.entrySet()) {
      final String oldKey = entry.getKey();
      final String newKey = entry.getValue();
      if (config.contains(oldKey)) {
        if (!config.contains(newKey)) {
          config.set(newKey, config.get(oldKey));
        }

        config.set(oldKey, null);
      }
    }

    if (config.contains(V2_LAST_LOCATION_FIELD)) {
      final Location location = (Location) config.get(V2_LAST_LOCATION_FIELD);
      final Location backup = plugin.configuration().backupLocation().mutable();
      if (backup.equals(location)) {
        config.set(V2_LAST_LOCATION_FIELD, null);
      } else {
        config.set(V2_LAST_LOCATION_FIELD, ImmutableLocation.copyOf(location));
      }
    } else {
      final String uuid = config.getString(V2_UUID_FIELD);
      final String name = config.getString(V2_NAME_FIELD);
      LOGGER.warn("Failed to load last known location of prisoner {} ({}). The world they were previously in might have been removed.", uuid, name);
    }

    if (config.getBoolean(V1_UNJAILED_FIELD)) {
      config.set(V2_SECONDS_LEFT_FIELD, 0);
    }

    config.set(V1_UNJAILED_FIELD, null);
  }
}
