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

package io.github.emilyydev.betterjails.test;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import com.github.fefo.betterjails.api.event.prisoner.PlayerImprisonEvent;
import com.github.fefo.betterjails.api.event.prisoner.PrisonerReleaseEvent;
import io.github.emilyydev.betterjails.BetterJailsPlugin;
import io.github.emilyydev.betterjails.util.Util;
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
    server.addSimpleWorld("world");
    try (final InputStream pluginDescriptorStream = BetterJailsPlugin.class.getResourceAsStream("/plugin.yml")) {
      plugin = MockBukkit.loadWith(BetterJailsPlugin.class, Objects.requireNonNull(pluginDescriptorStream, "descriptor stream"));
    }

    plugin.eventBus().subscribe(plugin, PlayerImprisonEvent.class, EventBusTest::playerImprison);
    plugin.eventBus().subscribe(plugin, PrisonerReleaseEvent.class, EventBusTest::prisonerRelease);
    server.getScheduler().performOneTick();
  }

  @AfterAll
  public static void teardown() {
    server = null;
    plugin = null;
    MockBukkit.unmock();
  }

  private static void playerImprison(final PlayerImprisonEvent event) {
    plugin.getLogger().info("event = " + event);
    plugin.getLogger().info("event.prisoner() = " + event.prisoner());
  }

  private static void prisonerRelease(final PrisonerReleaseEvent event) {
    plugin.getLogger().info("event = " + event);
    plugin.getLogger().info("event.prisoner() = " + event.prisoner());
  }

  @Test
  public void test() throws IOException {
    plugin.dataHandler().addJail("jail0", Vector.getRandom().toLocation(server.addSimpleWorld("world0")));

    final PlayerMock player = server.addPlayer();
    plugin.dataHandler().addJailedPlayer(player, "jail0", Util.NIL_UUID, "test", 3600L);
    plugin.dataHandler().releaseJailedPlayer(player.getUniqueId(), Util.NIL_UUID, "test", true);
  }
}
