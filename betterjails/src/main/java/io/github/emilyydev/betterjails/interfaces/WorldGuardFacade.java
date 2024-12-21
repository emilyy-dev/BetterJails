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

package io.github.emilyydev.betterjails.interfaces;

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import org.bukkit.entity.Player;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public final class WorldGuardFacade {

  private static final MethodHandle RESET_STATE_INTERNAL_MH;

  static {
    try {
      final MethodHandles.Lookup lookup = MethodHandles.lookup();
      MethodHandle resetStateMh = lookup.findStatic(WorldGuardFacade.class, "resetStateNop", MethodType.methodType(void.class, Player.class));
      try {
        final Class<?> sessionManagerClass = Class.forName("com.sk89q.worldguard.session.SessionManager");
        if (sessionManagerClass.isInterface()) { // 7.x
          resetStateMh = lookup.findStatic(WorldGuardFacade.class, "resetState0", MethodType.methodType(void.class, Player.class));
        } // TODO: <=1.12
      } catch (final ClassNotFoundException ignored) {
      }

      RESET_STATE_INTERNAL_MH = resetStateMh;
    } catch (final NoSuchMethodException | IllegalAccessException ex) {
      throw new ExceptionInInitializerError(ex);
    }
  }

  public static void resetState(final Player player) {
    try {
      RESET_STATE_INTERNAL_MH.invokeExact(player);
    } catch (final RuntimeException | Error ex) {
      throw ex;
    } catch (final Throwable ex) {
      throw new RuntimeException(ex);
    }
  }

  private static void resetState0(final Player player) {
    final LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
    WorldGuard.getInstance().getPlatform().getSessionManager().resetState(localPlayer);
  }

  private static void resetStateNop(final Player player) {
  }
}
