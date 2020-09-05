package me.fefo.betterjails;

import java.util.Random;

import me.fefo.facilites.ColorFormat;
import me.fefo.facilites.TaskUtil;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import org.jetbrains.annotations.Nullable;

public final class BetterJails extends JavaPlugin implements Listener {
  private static BetterJails instance = null;
  private static final Random RANDOM = new Random();
  private static final String STARS_AND_SPRINKLES = ".·'•*";
  public static final String SHORT_PREFIX = ColorFormat.format("&7[&3&lB&b&lJ&7] &r");
  public static final String LONG_PREFIX = ColorFormat.format("&7[&3Better&bJails&7] &r");

  @Nullable
  public static BetterJails getInstance() {
    return instance;
  }

  public BetterJails() {
    super();
    instance = this;
    TaskUtil.setPlugin(this);
  }

  @Override
  public void onEnable() {
    Bukkit.getConsoleSender().sendMessage(getBanner());

    saveDefaultConfig();
  }

  @Override
  public void onDisable() {
    instance = null;
  }

  public String getFullName() {
    return getDescription().getFullName();
  }

  public String getAuthor() {
    return getDescription().getAuthors().get(0);
  }

  public static String[] getBanner() {
    final String[] banner = new String[8];

    banner[0] = ColorFormat.format("=================================================");
    banner[1] = ColorFormat.format("&3                             &b                    ");
    banner[2] = ColorFormat.format("&3  _                          &b                    ");
    banner[3] = ColorFormat.format("&3 |_)   _   _|_  _|_   _   ._ &b   |   _.  o  |   _ ");
    banner[4] = ColorFormat.format("&3 |_)  (/_   |_   |_  (/_  |  &b \\_|  (_|  |  |  _> ");
    banner[5] = ColorFormat.format("&3                             &b                    ");
    banner[6] = ColorFormat.format("&3" + instance.getFullName() + " by " + instance.getAuthor());
    banner[7] = ColorFormat.format("=================================================");

    if (banner[6].length() >= banner[5].lastIndexOf('b') - 1) {
      banner[6] += ColorFormat.format("&b");
    }

    while (banner[6].length() != banner[5].length()) {
      if (banner[6].length() == banner[5].lastIndexOf('b') - 1) {
        banner[6] += ColorFormat.format("&b");
      }
      banner[6] += ' ';
    }

    // Give it sprinkles :D
    for (int i = 0; i < banner.length; ++i) {
      final StringBuilder builder = new StringBuilder(banner[i]);

      for (int j = 0; j < 3; ++j) {
        final int k = RANDOM.nextInt(builder.length());

        if (builder.charAt(k) == ' ') {
          builder.setCharAt(k, STARS_AND_SPRINKLES.charAt(RANDOM.nextInt(STARS_AND_SPRINKLES.length())));
        }
      }
      banner[i] = builder.toString();
    }

    return banner;
  }
}
