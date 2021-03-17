//
// This file is part of BetterJails, licensed under the MIT License.
//
// Copyright (c) 2021 Fefo6644 <federico.lopez.1999@outlook.com>
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

package com.github.fefo.betterjails.util;

public class Triplet<T, U, V> {

  private static final Triplet<?, ?, ?> NULL_TRIPLET = new Triplet<>(null, null, null);

  @SuppressWarnings("unchecked")
  public static <T, U, V> Triplet<T, U, V> of(final T first, final U second, final V third) {
    if (first == null && second == null && third == null) {
      return (Triplet<T, U, V>) NULL_TRIPLET;
    }
    return new Triplet<>(first, second, third);
  }

  private final T first;
  private final U second;
  private final V third;

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
}
