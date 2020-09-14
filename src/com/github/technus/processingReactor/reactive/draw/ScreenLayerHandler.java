package com.github.technus.processingReactor.reactive.draw;

import com.github.technus.processingReactor.MainProcessing;
import processing.core.PGraphics;
import reactor.core.publisher.*;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static com.github.technus.processingReactor.reactive.draw.DrawingStage.*;

public abstract class ScreenLayerHandler<Graphics extends PGraphics> {
    private final Flux<Graphics> flux;
    private final Flux<ScreenLayerHandler<Graphics>> fluxInvalidate;
    private final FluxSink<Graphics> sink;
    private final FluxSink<ScreenLayerHandler<Graphics>> sinkInvalidate;
    private final Function<List<Integer>, Graphics> graphicsSupplier;
    private final BiConsumer<Graphics,DrawingStage> graphicsPreparer;
    private Graphics graphics;
    private volatile boolean invalidated=true;

    protected ScreenLayerHandler(Function<Flux<ScreenLayerHandler<Graphics>>, Flux<ScreenLayerHandler<Graphics>>> chain,
                                 Function<List<Integer>, Graphics> graphicsSupplier,
                                 BiConsumer<Graphics,DrawingStage> graphicsPreparer,
                                 List<Integer> initialSize) {
        this.graphicsSupplier = graphicsSupplier;
        this.graphicsPreparer = graphicsPreparer;
        graphics=this.graphicsSupplier.apply(initialSize);
        FluxProcessor<Graphics, Graphics> processor= DirectProcessor.create();
        FluxProcessor<ScreenLayerHandler<Graphics>,ScreenLayerHandler<Graphics>> processorInvalidate=EmitterProcessor.create(1);
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

    public final Flux<Graphics> getFlux() {
        return flux;
    }

    public final Flux<ScreenLayerHandler<Graphics>> getFluxInvalidate() {
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
            graphics = graphicsSupplier.apply(resizeList);
            graphicsPreparer.accept(graphics, PreBegin);
            graphics.beginDraw();
            graphicsPreparer.accept(graphics, PostBegin);
            sink.next(graphics);
            graphicsPreparer.accept(graphics, PreEnd);
            graphics.endDraw();
            graphicsPreparer.accept(graphics, PostEnd);
            invalidated=false;
        }else if(invalidated){
            graphicsPreparer.accept(graphics, PreBegin);
            graphics.beginDraw();
            graphicsPreparer.accept(graphics, PostBegin);
            graphics.clear();
            sink.next(graphics);
            graphicsPreparer.accept(graphics, PreEnd);
            graphics.endDraw();
            graphicsPreparer.accept(graphics, PostEnd);
            invalidated=false;
        }
        mainProcessing.image(graphics,0,0);
    }
}
