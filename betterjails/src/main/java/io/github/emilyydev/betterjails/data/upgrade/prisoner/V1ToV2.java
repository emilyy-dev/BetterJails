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

import com.google.common.collect.ImmutableMap;
import io.github.emilyydev.betterjails.BetterJailsPlugin;
import io.github.emilyydev.betterjails.data.upgrade.DataUpgrader;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Map;

/**
 * Simple upgrading for renaming fields to a more proper and conventional naming
 */
public final class V1ToV2 implements DataUpgrader {

  private static final String V1_UNJAILED_FIELD = "unjailed";
  private static final String V1_LASTLOCATION_FIELD = "lastlocation";
  private static final String V1_JAILEDBY_FIELD = "jailedby";
  private static final String V1_SECONDSLEFT_FIELD = "secondsleft";

  private static final String V2_IS_RELEASED_FIELD = "released";
  private static final String V2_LAST_LOCATION_FIELD = "last-location";
  private static final String V2_JAILED_BY_FIELD = "jailed-by";
  private static final String V2_SECONDS_LEFT_FIELD = "seconds-left";

  private static final Map<String, String> FIELD_MIGRATION_MAP =
      ImmutableMap.of(
          V1_UNJAILED_FIELD, V2_IS_RELEASED_FIELD,
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
  }
}
