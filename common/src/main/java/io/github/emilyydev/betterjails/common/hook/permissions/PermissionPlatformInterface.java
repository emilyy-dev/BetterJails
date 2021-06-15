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

package io.github.emilyydev.betterjails.common.hook.permissions;

import io.github.emilyydev.betterjails.common.plugin.abstraction.Player;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface PermissionPlatformInterface {

  CompletableFuture<Collection<String>> getParentGroups(final UUID uuid);
  CompletableFuture<Void> setParentGroup(final UUID uuid, final String group);
  CompletableFuture<Void> addParentGroup(final UUID uuid, final String group);
  CompletableFuture<Void> removeParentGroup(final UUID uuid, final String group);

  default CompletableFuture<Collection<String>> getParentGroups(final Player<?> player) {
    return getParentGroups(player.uuid());
  }

  default CompletableFuture<Void> setParentGroup(final Player<?> player, final String groupName) {
    return setParentGroup(player.uuid(), groupName);
  }

  default CompletableFuture<Void> addParentGroup(final Player<?> player, final String groupName) {
    return addParentGroup(player.uuid(), groupName);
  }

  default CompletableFuture<Void> removeParentGroup(final Player<?> player, final String groupName) {
    return removeParentGroup(player.uuid(), groupName);
  }
}
