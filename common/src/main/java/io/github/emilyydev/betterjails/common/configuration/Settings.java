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

package io.github.emilyydev.betterjails.common.configuration;

import com.google.common.collect.ImmutableList;
import io.github.emilyydev.betterjails.common.configuration.type.BooleanSetting;
import io.github.emilyydev.betterjails.common.configuration.type.StringSetting;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public final class Settings {

  public static final BooleanSetting OFFLINE_TIME = new BooleanSetting("offline-time", false, false);

  public static final BooleanSetting CHANGE_GROUP = new BooleanSetting("change-group", false, false);

  public static final StringSetting PRISONER_GROUP = new StringSetting("prisoner-group", "prisoner", false);

  public static final Setting<GroupChangingBehavior> GROUP_CHANGING_BEHAVIOR =
      new Setting<GroupChangingBehavior>("group-changing-behavior", GroupChangingBehavior.ADD, false) {

        @Override
        public @NotNull GroupChangingBehavior get(final @NotNull ConfigurationAdapter configurationAdapter) {
          final String value = configurationAdapter.getString(key());
          if (value == null) {
            return fallback();
          }

          final GroupChangingBehavior behavior = GroupChangingBehavior.find(value);
          return behavior == null ? fallback() : behavior;
        }
      };

  public static final BooleanSetting CELL_SPECIFIC_PERMISSION = new BooleanSetting("cell-specific-permissions", false, false);

  public static final Setting<TextColor> JAIL_LIST_COLOR =
      new Setting<TextColor>("jail-list-color", NamedTextColor.GREEN, true) {

        @Override
        public @NotNull TextColor get(final @NotNull ConfigurationAdapter configurationAdapter) {
          final String value = configurationAdapter.getString(key());
          if (value == null) {
            return fallback();
          }

          TextColor color = NamedTextColor.NAMES.value(value);
          if (color != null) {
            return color;
          }

          color = TextColor.fromHexString(value);
          if (color != null) {
            return color;
          }

          return fallback();
        }
      };

  public static final Setting<ListingStyle> JAIL_LIST_STYLE =
      new Setting<ListingStyle>("jail-list-style", ListingStyle.LINE, true) {

        @Override
        public @NotNull ListingStyle get(final @NotNull ConfigurationAdapter configurationAdapter) {
          final String value = configurationAdapter.getString(key());
          if (value == null) {
            return fallback();
          }

          final ListingStyle style = ListingStyle.find(value);
          return style == null ? fallback() : style;
        }
      };

  public static final StringSetting LIST_SEPARATOR =
      new StringSetting("jail-list-separators", "", true) { // fallback unused

        @Override
        public @NotNull String get(final @NotNull ConfigurationAdapter configurationAdapter) {
          final ListingStyle style = configurationAdapter.get(JAIL_LIST_STYLE);

          final Map<String, String> listSeparators = configurationAdapter.getSection(key());
          if (listSeparators == null) {
            return style.defaultSeparator();
          }

          return listSeparators.getOrDefault(style.toString(), style.defaultSeparator());
        }
      };

  // TODO add other settings

  public static final ImmutableList<Setting<?>> SETTINGS =
      ImmutableList.of(OFFLINE_TIME,
                       CHANGE_GROUP,
                       PRISONER_GROUP,
                       GROUP_CHANGING_BEHAVIOR,
                       CELL_SPECIFIC_PERMISSION,
                       JAIL_LIST_STYLE,
                       JAIL_LIST_COLOR,
                       LIST_SEPARATOR);
}
