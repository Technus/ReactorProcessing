package com.github.technus.processingReactor.reactive;

import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxProcessor;
import reactor.core.publisher.FluxSink;

import java.util.function.Function;

public class ReactiveListener<T> {
    private final Flux<T> flux;
    private final FluxSink<T> sink;

    public ReactiveListener(Function<Flux<T>,Flux<T>> chain) {
        FluxProcessor<T,T> processor= EmitterProcessor.create(1);
        sink=processor.sink(FluxSink.OverflowStrategy.DROP);
        flux = processor.transform(chain);
    }

    public Flux<T> getFlux() {
        return flux;
    }

    public FluxSink<T> getSink() {
        return sink;
    }
}
