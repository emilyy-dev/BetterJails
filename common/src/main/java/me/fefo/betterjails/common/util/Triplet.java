package me.fefo.betterjails.common.util;

import org.jetbrains.annotations.Nullable;

public class Triplet<T, U, V> {

  public static <T, U, V> Triplet<T, U, V> of(@Nullable final T first, @Nullable final U second, @Nullable final V third) {
    return new Triplet<>(first, second, third);
  }

  public static <T, U, V> Triplet<T, U, V> immutable(@Nullable final T first, @Nullable final U second, @Nullable final V third) {
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

  public @Nullable T getFirst() {
    return first;
  }

  public @Nullable U getSecond() {
    return second;
  }

  public @Nullable V getThird() {
    return third;
  }

  public void setFirst(@Nullable final T first) {
    this.first = first;
  }

  public void setSecond(@Nullable final U second) {
    this.second = second;
  }

  public void setThird(@Nullable final V third) {
    this.third = third;
  }

  private static class ImmutableTriplet<T, U, V> extends Triplet<T, U, V> {

    private ImmutableTriplet(final T first, final U second, final V third) {
      super(first, second, third);
    }

    @Override
    public void setFirst(@Nullable final T first) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void setSecond(@Nullable final U second) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void setThird(@Nullable final V third) {
      throw new UnsupportedOperationException();
    }
  }
}
