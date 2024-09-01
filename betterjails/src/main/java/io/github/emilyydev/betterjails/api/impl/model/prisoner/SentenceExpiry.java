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

package io.github.emilyydev.betterjails.api.impl.model.prisoner;

import java.time.Duration;
import java.time.Instant;

public interface SentenceExpiry {

  static SentenceExpiry of(final Duration timeLeft) {
    return new OfTimeLeft(timeLeft);
  }

  static SentenceExpiry of(final Instant expiryDate) {
    return new OfExpiryDate(expiryDate);
  }

  Duration timeLeft();
  Instant expiryDate();

  final class OfTimeLeft implements SentenceExpiry {

    private final Duration timeLeft;

    public OfTimeLeft(final Duration timeLeft) {
      this.timeLeft = timeLeft;
    }

    @Override
    public Duration timeLeft() {
      return this.timeLeft;
    }

    @Override
    public Instant expiryDate() {
      return Instant.now().plus(this.timeLeft);
    }

    @Override
    public String toString() {
      return "SentenceExpiry.OfTimeLeft(" + this.timeLeft + ')';
    }
  }

  final class OfExpiryDate implements SentenceExpiry {

    private final Instant expiryDate;

    public OfExpiryDate(final Instant expiryDate) {
      this.expiryDate = expiryDate;
    }

    @Override
    public Duration timeLeft() {
      return Duration.between(Instant.now(), this.expiryDate);
    }

    @Override
    public Instant expiryDate() {
      return this.expiryDate;
    }

    @Override
    public String toString() {
      return "SentenceExpiry.OfExpiryDate[" + this.expiryDate + ']';
    }
  }
}
