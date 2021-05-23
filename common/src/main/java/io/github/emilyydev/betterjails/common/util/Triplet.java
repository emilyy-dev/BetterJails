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

public class Triplet<T, U, V> {

  public static <T, U, V> Triplet<T, U, V> of(final T first, final U second, final V third) {
    return new Triplet<>(first, second, third);
  }

  public static <T, U, V> Triplet<T, U, V> immutable(final T first, final U second, final V third) {
    return new ImmutableTriplet<>(first, second, third);
  }

  private T first;
  private U second;
  private V third;

  private Triplet(final T first, final U second, final V third) {
    this.first = first;
    this.second = second;
    this.third = third;
  }

  public T getFirst() {
    return this.first;
  }

  public U getSecond() {
    return this.second;
  }

  public V getThird() {
    return this.third;
  }

  public void setFirst(final T first) {
    this.first = first;
  }

  public void setSecond(final U second) {
    this.second = second;
  }

  public void setThird(final V third) {
    this.third = third;
  }

  private static class ImmutableTriplet<T, U, V> extends Triplet<T, U, V> {

    private ImmutableTriplet(final T first, final U second, final V third) {
      super(first, second, third);
    }

    @Override
    public void setFirst(final T first) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void setSecond(final U second) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void setThird(final V third) {
      throw new UnsupportedOperationException();
    }
  }
}
