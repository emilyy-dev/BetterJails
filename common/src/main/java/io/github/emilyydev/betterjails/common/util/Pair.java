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

package io.github.emilyydev.betterjails.common.util;

public class Pair<T, U> {

  public static <T, U> Pair<T, U> of(final T first, final U second) {
    return new Pair<>(first, second);
  }

  public static <T, U> Pair<T, U> immutable(final T first, final U second) {
    return new ImmutablePair<>(first, second);
  }

  private T first;
  private U second;

  private Pair(final T first, final U second) {
    this.first = first;
    this.second = second;
  }

  public T getFirst() {
    return this.first;
  }

  public U getSecond() {
    return this.second;
  }

  public void setFirst(final T first) {
    this.first = first;
  }

  public void setSecond(final U second) {
    this.second = second;
  }

  private static class ImmutablePair<T, U> extends Pair<T, U> {

    private ImmutablePair(final T first, final U second) {
      super(first, second);
    }

    @Override
    public void setFirst(final T first) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void setSecond(final U second) {
      throw new UnsupportedOperationException();
    }
  }
}
