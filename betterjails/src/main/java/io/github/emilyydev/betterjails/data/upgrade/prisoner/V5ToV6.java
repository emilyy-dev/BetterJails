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
import io.github.emilyydev.betterjails.BetterJailsPlugin;
import io.github.emilyydev.betterjails.data.upgrade.DataUpgrader;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

public class V5ToV6 implements DataUpgrader {

  private static final String LAST_LOCATION_FIELD = "last-location";
  private static final String V5_UNKNOWN_LOCATION_FIELD = "unknown-location";

  @Override
  public void upgrade(final ConfigurationSection config, final BetterJailsPlugin plugin) {
    if (config.getBoolean(V5_UNKNOWN_LOCATION_FIELD, false)) {
      config.set(LAST_LOCATION_FIELD, null);
    } else {
      config.set(LAST_LOCATION_FIELD, ImmutableLocation.copyOf((Location) config.get(LAST_LOCATION_FIELD)));
    }

    config.set(V5_UNKNOWN_LOCATION_FIELD, null);
  }
}
