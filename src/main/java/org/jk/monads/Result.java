package org.jk.monads;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public abstract class Result<T> {

    protected Result() {}

    public static <U> Result<U> from(ResultSupplier<U> f) {
        Objects.requireNonNull(f);
        try {
            return success(f.get());
        } catch (Throwable t) {
            return failure(t);
        }
    }

    public abstract <U> Result<U> transform(ResultFunction<? super T, ? extends U> f);

    public abstract <U> Result<U> chain(ResultFunction<? super T, Result<U>> f);

    public abstract T recoverValue(Function<? super Throwable, T> f);

    public abstract Result<T> recoverResult(ResultFunction<? super Throwable, Result<T>> f);

    public abstract T getOrElse(T value);

    public abstract Result<T> getOrElseFrom(ResultSupplier<T> f);

    public abstract <X extends Throwable> T orElseThrow(Supplier<? extends X> exceptionSupplier) throws X;

    public abstract T getValue() throws Throwable;

    public abstract T getUncheckedValue();

    public abstract boolean isSuccessful();

    public abstract <E extends Throwable> Result<T> ifSuccess(ResultConsumer<T, E> action) throws E;

    public abstract <E extends Throwable> Result<T> ifFailure(ResultConsumer<Throwable, E> action) throws E;

    public abstract Result<T> filterValue(Predicate<T> pred);

    public abstract Optional<T> toOptional();

    public static <U> Result<U> failure(Throwable e) {
        return new FailureResult<>(e);
    }

    public static <U> Result<U> success(U x) {
        return new SuccessResult<>(x);
    }
}

class SuccessResult<T> extends Result<T> {
    private final T value;

    SuccessResult(T value) {
        this.value = value;
    }

    @Override
    public <U> Result<U> chain(ResultFunction<? super T, Result<U>> f) {
        Objects.requireNonNull(f);
        try {
            return f.apply(value);
        } catch (Throwable t) {
            return Result.failure(t);
        }
    }

    @Override
    public T recoverValue(Function<? super Throwable, T> f) {
        return value;
    }

    @Override
    public Result<T> recoverResult(ResultFunction<? super Throwable, Result<T>> f) {
        return this;
    }

    @Override
    public T getOrElse(T value) {
        return this.value;
    }

    @Override
    public Result<T> getOrElseFrom(ResultSupplier<T> f) {
        return this;
    }

    @Override
    public <X extends Throwable> T orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
        return value;
    }

    @Override
    public T getValue() {
        return value;
    }

    @Override
    public T getUncheckedValue() {
        return value;
    }

    @Override
    public <U> Result<U> transform(ResultFunction<? super T, ? extends U> f) {
        Objects.requireNonNull(f);
        try {
            return new SuccessResult<>(f.apply(value));
        } catch (Throwable t) {
            return Result.failure(t);
        }
    }

    @Override
    public boolean isSuccessful() {
        return true;
    }

    @Override
    public <E extends Throwable> Result<T> ifSuccess(ResultConsumer<T, E> action) throws E {
        action.accept(value);
        return this;
    }

    @Override
    public Result<T> filterValue(Predicate<T> p) {
        if (p.test(value)) {
            return this;
        } else {
            return Result.failure(new NoSuchElementException("Predicate does not match for " + value));
        }
    }

    @Override
    public Optional<T> toOptional() {
        return Optional.ofNullable(value);
    }

    @Override
    public <E extends Throwable> Result<T> ifFailure(ResultConsumer<Throwable, E> action) {
        return this;
    }
}

class FailureResult<T> extends Result<T> {
    private final Throwable e;

    FailureResult(Throwable e) {
        this.e = e;
    }

    @Override
    public <U> Result<U> transform(ResultFunction<? super T, ? extends U> f) {
        return Result.failure(e);
    }

    @Override
    public <U> Result<U> chain(ResultFunction<? super T, Result<U>> f) {
        return Result.failure(e);
    }

    @Override
    public T recoverValue(Function<? super Throwable, T> f) {
        return f.apply(e);
    }

    @Override
    public Result<T> recoverResult(ResultFunction<? super Throwable, Result<T>> f) {
        try {
            return f.apply(e);
        } catch (Throwable t) {
            return Result.failure(t);
        }
    }

    @Override
    public T getOrElse(T value) {
        return value;
    }

    @Override
    public Result<T> getOrElseFrom(ResultSupplier<T> f) {
        return Result.from(f);
    }

    @Override
    public <X extends Throwable> T orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
        throw exceptionSupplier.get();
    }

    @Override
    public T getValue() throws Throwable {
        throw e;
    }

    @Override
    public T getUncheckedValue() {
        throw new RuntimeException(e);
    }

    @Override
    public boolean isSuccessful() {
        return false;
    }

    @Override
    public <E extends Throwable> Result<T> ifSuccess(ResultConsumer<T, E> action) {
        return this;
    }

    @Override
    public Result<T> filterValue(Predicate<T> pred) {
        return this;
    }

    @Override
    public Optional<T> toOptional() {
        return Optional.empty();
    }

    @Override
    public <E extends Throwable> Result<T> ifFailure(ResultConsumer<Throwable, E> action) throws E {
        action.accept(e);
        return this;
    }
}
