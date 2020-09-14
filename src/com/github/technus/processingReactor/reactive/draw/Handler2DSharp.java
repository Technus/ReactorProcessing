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

public final class Handler2DSharp extends Handler2D{
    public Handler2DSharp(MainProcessing processing) {
        this(noOperation(),processing,processing.getSizeFlux().blockFirst());
    }

    public Handler2DSharp(Function<Flux<ScreenLayerHandler<PGraphics2D>>, Flux<ScreenLayerHandler<PGraphics2D>>> chain,
                          MainProcessing processing,
                          List<Integer> list) {
        this(chain, noActionOnBoth(),processing, list);
    }

    public Handler2DSharp(Function<Flux<ScreenLayerHandler<PGraphics2D>>, Flux<ScreenLayerHandler<PGraphics2D>>> chain,
                          BiConsumer<PGraphics2D,DrawingStage> graphicsPreparer,
                          MainProcessing processing,
                          List<Integer> list) {
        super(chain, size-> {
            PGraphics2D graphics = (PGraphics2D) processing.createGraphics(size.get(0), size.get(1), P2D);
            graphics.noSmooth();
            return graphics;
        },graphicsPreparer, list);
    }
}