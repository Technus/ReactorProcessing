package com.github.technus.processingReactor.reactive.draw;

import com.github.technus.processingReactor.MainProcessing;
import processing.opengl.PGraphics2D;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.github.technus.processingReactor.Utility.noOperation;
import static com.github.technus.processingReactor.Utility.noAction;

public class Handler2DSharp extends Handler2D{
    public Handler2DSharp(MainProcessing processing) {
        this(noOperation(),processing,processing.getSizeFlux().blockFirst());
    }

    public Handler2DSharp(Function<Flux<ScreenLayerHandler<PGraphics2D>>, Flux<ScreenLayerHandler<PGraphics2D>>> chain,
                          MainProcessing processing,
                          List<Integer> list) {
        this(chain, noAction(),processing, list);
    }

    public Handler2DSharp(Function<Flux<ScreenLayerHandler<PGraphics2D>>, Flux<ScreenLayerHandler<PGraphics2D>>> chain,
                          Consumer<PGraphics2D> graphicsPreparer,
                          MainProcessing processing,
                          List<Integer> list) {
        super(chain, pGraphics2D -> {
            pGraphics2D.noSmooth();
            pGraphics2D.textureSampling(3);//linear
            graphicsPreparer.accept(pGraphics2D);
        }, processing, list);
    }
}