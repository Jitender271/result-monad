package org.jk.monads;

public interface ResultConsumer<T, E extends Throwable> {
    void accept(T t) throws E;
}
