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

import org.bukkit.entity.Player;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import static java.lang.invoke.MethodHandles.*;
import static java.lang.invoke.MethodType.methodType;

public final class WorldGuardFacade {

    private static final MethodHandle RESET_STATE;

    static {
        try {
            final MethodHandles.Lookup lookup = MethodHandles.lookup();
            MethodHandle resetState = lookup.findStatic(WorldGuardFacade.class, "empty", methodType(void.class)); // ()void
            resetState = dropArguments(resetState, 0, Player.class);                                              // (Player)void
            try {
                final Class<?> SessionManager = Class.forName("com.sk89q.worldguard.session.SessionManager");
                if (SessionManager.isInterface()) { // 7.x
                    final Class<?> WorldGuardPlugin = Class.forName("com.sk89q.worldguard.bukkit.WorldGuardPlugin");
                    final Class<?> WorldGuard = Class.forName("com.sk89q.worldguard.WorldGuard");
                    final Class<?> WorldGuardPlatform = Class.forName("com.sk89q.worldguard.internal.platform.WorldGuardPlatform");
                    final Class<?> LocalPlayer = Class.forName("com.sk89q.worldguard.LocalPlayer");

                    // WorldGuard.getInstance().getPlatform().getSessionManager().resetState(WorldGuardPlugin.inst().wrapPlayer(player));

                    final MethodHandle WorldGuardPlugin_inst = lookup.findStatic(WorldGuardPlugin, "inst", methodType(WorldGuardPlugin));
                    final MethodHandle WorldGuardPlugin_wrapPlayer = lookup.findVirtual(WorldGuardPlugin, "wrapPlayer", methodType(LocalPlayer, Player.class));

                    final MethodHandle WorldGuard_getInstance = lookup.findStatic(WorldGuard, "getInstance", methodType(WorldGuard));
                    final MethodHandle WorldGuard_getPlatform = lookup.findVirtual(WorldGuard, "getPlatform", methodType(WorldGuardPlatform));
                    final MethodHandle WorldGuardPlatform_getSessionManager = lookup.findVirtual(WorldGuardPlatform, "getSessionManager", methodType(SessionManager));
                    final MethodHandle SessionManager_resetState = lookup.findVirtual(SessionManager, "resetState", methodType(void.class, LocalPlayer));

                    MethodHandle wrapPlayer = WorldGuardPlugin_wrapPlayer;          // (WorldGuardPlugin,Player)LocalPlayer
                    wrapPlayer = foldArguments(wrapPlayer, WorldGuardPlugin_inst);  // (Player)LocalPlayer

                    resetState = SessionManager_resetState;                                                         // (SessionManager,LocalPlayer)void
                    resetState = filterArguments(resetState, 0, WorldGuardPlatform_getSessionManager, wrapPlayer);  // (WorldGuardPlatform,Player)void
                    resetState = filterArguments(resetState, 0, WorldGuard_getPlatform);                            // (WorldGuard,Player)void
                    resetState = foldArguments(resetState, WorldGuard_getInstance);                                 // (Player)void
                } // TODO: <=1.12
            } catch (final ClassNotFoundException ignored) {
            }

            RESET_STATE = resetState;
        } catch (final NoSuchMethodException | IllegalAccessException ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static void resetState(final Player player) {
        try {
            RESET_STATE.invokeExact(player);
        } catch (final RuntimeException | Error ex) {
            throw ex;
        } catch (final Throwable ex) {
            throw new RuntimeException(ex);
        }
    }

    private static void empty() {
    }
}
