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

package io.github.emilyydev.betterjails.common.plugin;

import io.github.emilyydev.betterjails.common.configuration.ConfigurationAdapter;
import io.github.emilyydev.betterjails.common.message.Subject;
import io.github.emilyydev.betterjails.common.plugin.abstraction.PlatformAdapter;
import io.github.emilyydev.betterjails.common.plugin.abstraction.Player;
import io.github.emilyydev.betterjails.common.plugin.abstraction.TaskScheduler;
import net.kyori.adventure.platform.AudienceProvider;

import java.nio.file.Path;

public interface BetterJailsBootstrap {

  AudienceProvider getAudienceProvider();
  Subject getConsoleSubject();
  TaskScheduler getTaskScheduler();
  <S, P, L, W> PlatformAdapter<S, P, L, W> getPlatformAdapter();
  Path getPluginFolder();
  ConfigurationAdapter getConfigurationAdapter();
  String getVersion();
  <P> Iterable<Player<P>> getOnlinePlayers();
  BetterJailsPlugin getPlugin();
}