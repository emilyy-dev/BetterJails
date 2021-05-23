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

package io.github.emilyydev.betterjails.common.plugin.abstraction;

import io.github.emilyydev.betterjails.common.message.Subject;
import org.jetbrains.annotations.NotNull;

/**
 * Main abstraction layer between the platform types and BetterJail's representation/adaptation of them.
 * <p>
 * Allows for translating and transforming objects of the types listed in the type parameters.
 *
 * @param <S> "sender" - the platform command sender/source type. Adapts to a messaging {@link Subject}.
 * @param <P> "player" - the platform player type. Adapts to an abstracted {@link Player} which also holds the platform player instance.
 * @param <L> "location" - the platform location type. Adapts to an abstracted, immutable {@link Location}.
 * @param <W> "world" - the platform world type. Adapts to a {@link World} identifying object.
 */
public interface PlatformAdapter<S, P, L, W> {

  S adaptSubject(@NotNull Subject subject);
  P adaptPlayer(@NotNull Player<P> player);
  L adaptLocation(@NotNull Location location);
  W adaptWorld(@NotNull World world);

  Subject adaptSubject(@NotNull S subject);
  Player<P> adaptPlayer(@NotNull P player);
  Location adaptLocation(@NotNull L location);
  World adaptWorld(@NotNull W world);
}
