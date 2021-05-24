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

import io.github.emilyydev.betterjails.common.message.Subject;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.function.Predicate;

import static java.util.Objects.requireNonNull;

public class Permission implements Predicate<Subject> {

  public static Permission has(final String permission) {
    requireNonNull(permission, "permission");
    final String lowercase = permission.toLowerCase(Locale.ROOT);
    return new Permission(subject -> subject.hasPermission(lowercase));
  }

  public static Permission lacks(final String permission) {
    return has(permission).negate();
  }

  private final Predicate<Subject> delegate;

  private Permission(Predicate<Subject> delegate) {
    // we only need the root delegate predicate
    while (delegate instanceof Permission) {
      delegate = ((Permission) delegate).delegate;
    }

    this.delegate = delegate;
  }

  @Override
  public boolean test(final Subject subject) {
    return this.delegate.test(subject);
  }

  @Override
  public @NotNull Permission and(final @NotNull Predicate<? super Subject> other) {
    return new Permission(this.delegate.and(requireNonNull(other, "other")));
  }

  public @NotNull Permission and(final @NotNull String other) {
    return and(has(other));
  }

  @Override
  public @NotNull Permission or(final @NotNull Predicate<? super Subject> other) {
    return new Permission(this.delegate.or(requireNonNull(other, "other")));
  }

  public @NotNull Permission or(final @NotNull String other) {
    return new Permission(or(has(other)));
  }

  @Override
  public @NotNull Permission negate() {
    return new Permission(this.delegate.negate());
  }
}
