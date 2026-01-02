//
// This file is part of BetterJails, licensed under the MIT License.
//
// Copyright (c) 2025 emilyy-dev
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

package io.github.emilyydev.betterjails.interfaces.storage;

import com.github.fefo.betterjails.api.util.ImmutableLocation;
import com.google.common.collect.ImmutableMap;
import io.github.emilyydev.betterjails.BetterJailsPlugin;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.*;

import static java.lang.invoke.MethodHandles.lookup;
import static java.lang.invoke.MethodType.methodType;

/**
 * Data upgrader implementation for Bukkit configuration storage.
 * Handles migration of prisoner and jail data between schema versions.
 */
public final class BukkitConfigurationDataUpgrader implements StorageDataUpgrader {

    private static final Logger LOGGER = LoggerFactory.getLogger("BetterJails");
    private static final int PRISONER_VERSION = 2;
    private static final int JAILS_VERSION = 2;

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

    private static final String JAILS_FIELD = "jails";
    private static final String NAME_FIELD = "name";
    private static final String LOCATION_FIELD = "location";

    private final BetterJailsPlugin plugin;

    public BukkitConfigurationDataUpgrader(final BetterJailsPlugin plugin) {
        this.plugin = plugin;
    }

    private static void markPrisonerVersion(final @NotNull ConfigurationSection config) {
        config.set("version", PRISONER_VERSION);
        SetInlineCommentsHelper.setVersionWarning(config);
    }

    private static void markJailVersion(final @NotNull ConfigurationSection config) {
        config.set("version", JAILS_VERSION);
        SetInlineCommentsHelper.setVersionWarning(config);
    }

    @Override
    public int getCurrentPrisonerVersion() {
        return PRISONER_VERSION;
    }

    @Override
    public int getCurrentJailsVersion() {
        return JAILS_VERSION;
    }

    @Override
    public void upgradePrisonerData(final Object data, final int fromVersion, final int toVersion) throws Exception {
        if (!(data instanceof ConfigurationSection)) {
            throw new IllegalArgumentException("Data must be a ConfigurationSection");
        }

        final ConfigurationSection config = (ConfigurationSection) data;

        if (fromVersion < 2 && toVersion >= 2) {
            upgradePrisonerV1ToV2(config);
        }

        markPrisonerVersion(config);
    }

    @Override
    public void upgradeJailsData(final Object data, final int fromVersion, final int toVersion) throws Exception {
        if (!(data instanceof ConfigurationSection)) {
            throw new IllegalArgumentException("Data must be a ConfigurationSection");
        }

        final ConfigurationSection config = (ConfigurationSection) data;

        if (fromVersion < 2 && toVersion >= 2) {
            upgradeJailsV1ToV2(config);
        }

        markJailVersion(config);
    }

    private void upgradePrisonerV1ToV2(final ConfigurationSection config) {
        // Migrate field names
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

        // Handle location migration
        if (config.contains(V2_LAST_LOCATION_FIELD)) {
            final Location location = (Location) config.get(V2_LAST_LOCATION_FIELD);
            final Location backup = this.plugin.configuration().backupLocation().mutable();
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

        // Handle unjailed flag
        if (config.getBoolean(V1_UNJAILED_FIELD)) {
            config.set(V2_SECONDS_LEFT_FIELD, 0);
        }
        config.set(V1_UNJAILED_FIELD, null);
    }

    private void upgradeJailsV1ToV2(final @NotNull ConfigurationSection config) {
        final Set<String> keys = config.getKeys(false);
        final List<Map<String, Object>> jails = new ArrayList<>();

        for (final String name : keys) {
            final Map<String, Object> jail = new HashMap<>();
            jail.put(NAME_FIELD, name);
            jail.put(LOCATION_FIELD, ImmutableLocation.copyOf((Location) config.get(name)));
            config.set(name, null);
            jails.add(jail);
        }

        config.set(JAILS_FIELD, jails);
    }

    private static final class SetInlineCommentsHelper {

        private static final MethodHandle SET_INLINE_COMMENTS_MH;

        static {
            final MethodHandles.Lookup lookup = lookup();
            MethodHandle setInlineCommentsMh;
            try {
                setInlineCommentsMh = lookup.findVirtual(ConfigurationSection.class, "setInlineComments", methodType(void.class, String.class, List.class));
            } catch (final NoSuchMethodException | IllegalAccessException ex) {
                try {
                    setInlineCommentsMh = lookup.findStatic(SetInlineCommentsHelper.class, "setInlineCommentsNoop", methodType(void.class, ConfigurationSection.class, String.class, List.class));
                } catch (final NoSuchMethodException | IllegalAccessException ex2) {
                    throw new ExceptionInInitializerError(ex2);
                }
            }

            SET_INLINE_COMMENTS_MH = setInlineCommentsMh;
        }

        static void setVersionWarning(final ConfigurationSection config) {
            setInlineComments(config, "version", Collections.singletonList("DO NOT CHANGE OR REMOVE THIS VALUE UNDER ANY CIRCUMSTANCES"));
        }

        private static void setInlineCommentsNoop(final ConfigurationSection config, final String path, final List<String> comments) {
        }

        private static void setInlineComments(final ConfigurationSection config, final String path, final List<String> comments) {
            try {
                SET_INLINE_COMMENTS_MH.invokeExact(config, path, comments);
            } catch (final RuntimeException | Error ex) {
                throw ex;
            } catch (final Throwable ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}