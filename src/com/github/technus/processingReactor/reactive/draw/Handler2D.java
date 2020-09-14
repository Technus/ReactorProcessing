package com.github.technus.processingReactor.reactive.draw;

import com.github.technus.processingReactor.MainProcessing;
import processing.opengl.PGraphics2D;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static com.github.technus.processingReactor.Utility.noActionOnBoth;
import static com.github.technus.processingReactor.Utility.noOperation;
import static processing.core.PConstants.P2D;

public class Handler2D extends ScreenLayerHandler<PGraphics2D>{
    public Handler2D(MainProcessing processing) {
        this(noOperation(), processing, processing.getSizeFlux().blockFirst());
    }

    public Handler2D(Function<Flux<ScreenLayerHandler<PGraphics2D>>, Flux<ScreenLayerHandler<PGraphics2D>>> chain,
                     MainProcessing processing,
                     List<Integer> list) {
        this(chain, noActionOnBoth(),processing,list);
    }

    public Handler2D(Function<Flux<ScreenLayerHandler<PGraphics2D>>, Flux<ScreenLayerHandler<PGraphics2D>>> chain,
                     BiConsumer<PGraphics2D,DrawingStage> graphicsPreparer,
                     MainProcessing processing,
                     List<Integer> list) {
        super(chain, size-> (PGraphics2D) processing.createGraphics(size.get(0), size.get(1), P2D),graphicsPreparer, list);
    }

    protected Handler2D(Function<Flux<ScreenLayerHandler<PGraphics2D>>, Flux<ScreenLayerHandler<PGraphics2D>>> chain,
                     Function<List<Integer>, PGraphics2D> graphicsSupplier,
                     BiConsumer<PGraphics2D,DrawingStage> graphicsPreparer,
                     List<Integer> initialSize) {
        super(chain, graphicsSupplier, graphicsPreparer, initialSize);
    }
}