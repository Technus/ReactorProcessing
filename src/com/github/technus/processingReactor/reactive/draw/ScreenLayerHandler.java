package com.github.technus.processingReactor.reactive.draw;

import com.github.technus.processingReactor.MainProcessing;
import processing.core.PGraphics;
import reactor.core.publisher.*;

import java.util.List;
import java.util.function.Function;

public abstract class ScreenLayerHandler<T extends PGraphics> {
    private final Flux<T> flux;
    private final Flux<ScreenLayerHandler<T>> fluxInvalidate;
    private final FluxSink<T> sink;
    private final FluxSink<ScreenLayerHandler<T>> sinkInvalidate;
    private final Function<List<Integer>,T> supplier;
    private T graphics;
    private volatile boolean invalidated=true;

    public ScreenLayerHandler(Function<Flux<ScreenLayerHandler<T>>, Flux<ScreenLayerHandler<T>>> chain, Function<List<Integer>, T> supplier, List<Integer> initialSize) {
        this.supplier = supplier;
        graphics=this.supplier.apply(initialSize);
        FluxProcessor<T,T> processor= DirectProcessor.create();
        FluxProcessor<ScreenLayerHandler<T>,ScreenLayerHandler<T>> processorInvalidate=EmitterProcessor.create(1);
        sink=processor.sink(FluxSink.OverflowStrategy.DROP);
        sinkInvalidate=processorInvalidate.sink(FluxSink.OverflowStrategy.DROP);
        flux = processor.share();
        fluxInvalidate = processorInvalidate.filter(t->!t.invalidated).transform(chain);
    }

    public final void setInvalidated() {
        this.invalidated = true;
    }

    public final boolean isInvalidated() {
        return invalidated;
    }

    public final Flux<T> getFlux() {
        return flux;
    }

    public final Flux<ScreenLayerHandler<T>> getFluxInvalidate() {
        return fluxInvalidate;
    }

    public void nextInvalidate(List<Integer> resizeList, MainProcessing mainProcessing) {
        if (resizeList.get(0) == graphics.width && resizeList.get(1) == graphics.height) {
            sinkInvalidate.next(this);
        }
    }

    public void next(List<Integer> resizeList, MainProcessing mainProcessing) {
        if(resizeList.get(0)!=graphics.width || resizeList.get(1)!=graphics.height){
            invalidated=true;
            graphics.dispose();
            graphics = supplier.apply(resizeList);
            graphics.beginDraw();
            sink.next(graphics);
            graphics.endDraw();
            invalidated=false;
        }else if(invalidated){
            graphics.beginDraw();
            graphics.clear();
            sink.next(graphics);
            graphics.endDraw();
            invalidated=false;
        }
        mainProcessing.image(graphics,0,0);
    }
}
