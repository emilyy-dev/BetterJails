package io.github.emilyydev.betterjails.dataupgrade;

import com.google.common.collect.ImmutableMap;
import io.github.emilyydev.betterjails.BetterJailsPlugin;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.lang.invoke.MethodHandle;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.lang.invoke.MethodHandles.Lookup;
import static java.lang.invoke.MethodHandles.lookup;
import static java.lang.invoke.MethodType.methodType;

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

  private static final MethodHandle SET_INLINE_COMMENTS_MH;

  static {
    final Lookup lookup = lookup();
    MethodHandle setInlineCommentsMh;
    try {
      setInlineCommentsMh = lookup.findVirtual(ConfigurationSection.class, "setInlineComments", methodType(void.class, String.class, List.class));
    } catch (final NoSuchMethodException | IllegalAccessException ex) {
      // no warning for you
      try {
        setInlineCommentsMh = lookup.findStatic(V1ToV2.class, "setInlineCommentsNoop", methodType(void.class, ConfigurationSection.class, String.class, List.class));
      } catch (final NoSuchMethodException | IllegalAccessException ex2) {
        throw new ExceptionInInitializerError(ex2);
      }
    }

    SET_INLINE_COMMENTS_MH = setInlineCommentsMh;
  }

  private static void setInlineCommentsNoop(final ConfigurationSection section, final String path, final List<String> comments) {
  }

  public static void setVersionWarning(final ConfigurationSection section) {
    setInlineComments(section, "version", Collections.singletonList("DO NOT CHANGE OR REMOVE THIS VALUE UNDER ANY CIRCUMSTANCES"));
  }

  // shouldn't really be here if it's public but bleh
  private static void setInlineComments(final ConfigurationSection section, final String path, final List<String> comments) {
    try {
      SET_INLINE_COMMENTS_MH.invokeExact(section, path, comments);
    } catch (final RuntimeException | Error ex) {
      throw ex;
    } catch (final Throwable ex) {
      throw new RuntimeException(ex);
    }
  }

  @Override
  public boolean upgrade(final YamlConfiguration config, final BetterJailsPlugin plugin) {
    boolean changed = false;
    for (final Map.Entry<String, String> entry : FIELD_MIGRATION_MAP.entrySet()) {
      final String oldKey = entry.getKey();
      final String newKey = entry.getValue();
      if (config.contains(oldKey)) {
        if (!config.contains(newKey)) {
          config.set(newKey, config.get(oldKey));
        }

        config.set(oldKey, null);
        changed = true;
      }
    }

    if (!config.contains("version")) {
      config.set("version", 2);
      setVersionWarning(config);
      changed = true;
    }

    return changed;
  }
}
