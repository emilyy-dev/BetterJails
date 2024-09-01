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
