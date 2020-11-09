package me.fefo.betterjails.common.util;

import org.jetbrains.annotations.Nullable;

public class Pair<T, U> {

  public static <T, U> Pair<T, U> of(@Nullable final T first, @Nullable final U second) {
    return new Pair<>(first, second);
  }

  public static <T, U> Pair<T, U> immutable(@Nullable final T first, @Nullable final U second) {
    return new ImmutablePair<>(first, second);
  }

  private T first;
  private U second;

  private Pair(final T first, final U second) {
    this.first = first;
    this.second = second;
  }

  public @Nullable T getFirst() {
    return first;
  }

  public @Nullable U getSecond() {
    return second;
  }

  public void setFirst(@Nullable final T first) {
    this.first = first;
  }

  public void setSecond(@Nullable final U second) {
    this.second = second;
  }

  private static class ImmutablePair<T, U> extends Pair<T, U> {

    private ImmutablePair(final T first, final U second) {
      super(first, second);
    }

    @Override
    public void setFirst(@Nullable final T first) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void setSecond(@Nullable final U second) {
      throw new UnsupportedOperationException();
    }
  }
}
