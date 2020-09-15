package com.github.technus.processingReactor.reactive;

import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxProcessor;
import reactor.core.publisher.FluxSink;

import java.util.function.Function;

public class ReactiveListener<Anything> {
    private final Flux<Anything> flux;
    private final FluxSink<Anything> sink;

    public ReactiveListener(Function<Flux<Anything>,Flux<Anything>> chain) {
        FluxProcessor<Anything, Anything> processor= EmitterProcessor.create(1);
        sink=processor.sink(FluxSink.OverflowStrategy.DROP);
        flux = processor.transform(chain);
    }

    public Flux<Anything> getFlux() {
        return flux;
    }

    public FluxSink<Anything> getSink() {
        return sink;
    }
}
