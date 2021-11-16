package com.kroger.desp.consumer;

import lombok.SneakyThrows;

import java.util.function.Consumer;

@FunctionalInterface
public interface ThrowingConsumer<T> extends Consumer<T> {

    @SneakyThrows
    @Override
    default void accept(final T elem) {
        acceptThrows(elem);
    }

    void acceptThrows(T elem) throws Exception;

}