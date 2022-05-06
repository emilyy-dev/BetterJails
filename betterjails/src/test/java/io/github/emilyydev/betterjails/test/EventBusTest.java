package io.github.emilyydev.betterjails.test;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import com.github.fefo.betterjails.api.event.prisoner.PlayerImprisonEvent;
import com.github.fefo.betterjails.api.event.prisoner.PrisonerReleaseEvent;
import io.github.emilyydev.betterjails.BetterJailsPlugin;
import org.bukkit.util.Vector;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class EventBusTest {

  private static ServerMock server = null;
  private static BetterJailsPlugin plugin = null;

  @BeforeAll
  public static void prepare() throws IOException {
    server = MockBukkit.mock();
    try (final InputStream pluginDescriptorStream = BetterJailsPlugin.class.getResourceAsStream("/plugin.yml")) {
      plugin = MockBukkit.loadWith(BetterJailsPlugin.class, Objects.requireNonNull(pluginDescriptorStream, "descriptor stream"));
    }

    plugin.getEventBus().subscribe(plugin, PlayerImprisonEvent.class, EventBusTest::playerImprison);
    plugin.getEventBus().subscribe(plugin, PrisonerReleaseEvent.class, EventBusTest::prisonerRelease);
  }

  @AfterAll
  public static void teardown() {
    server = null;
    plugin = null;
    MockBukkit.unmock();
  }

  private static void playerImprison(final PlayerImprisonEvent event) {
    System.out.println("event = " + event);
    System.out.println("event.prisoner() = " + event.prisoner());
  }

  private static void prisonerRelease(final PrisonerReleaseEvent event) {
    System.out.println("event = " + event);
    System.out.println("event.prisoner() = " + event.prisoner());
  }

  @Test
  public void test() throws IOException {
    plugin.dataHandler.addJail("jail0", Vector.getRandom().toLocation(server.addSimpleWorld("world0")));

    final PlayerMock player = server.addPlayer();
    plugin.dataHandler.addJailedPlayer(player, "jail0", null, 3600L);
    plugin.dataHandler.removeJailedPlayer(player.getUniqueId());
  }
}
