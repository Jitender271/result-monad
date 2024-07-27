package org.jk.monads;

public interface ResultFunction<T, R> {
    R apply(T t) throws Throwable;
}