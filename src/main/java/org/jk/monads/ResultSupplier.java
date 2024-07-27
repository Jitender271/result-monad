package org.jk.monads;

public interface ResultSupplier<T> {
    T get() throws Throwable;
}