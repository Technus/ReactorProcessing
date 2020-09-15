package com.github.technus.processingReactor.reactive.draw;

import com.github.technus.processingReactor.MainProcessing;
import processing.opengl.PGraphics2D;
import reactor.core.publisher.Flux;

import java.util.function.Function;

import static com.github.technus.processingReactor.Utility.noOperation;
import static processing.core.PConstants.P2D;

public class Handler2DSharp extends Handler2D{
    public Handler2DSharp(MainProcessing processing) {
        this(noOperation(),processing);
    }

    public Handler2DSharp(Function<Flux<ScreenLayerHandler<PGraphics2D>>, Flux<ScreenLayerHandler<PGraphics2D>>> chain,
                          MainProcessing processing) {
        super(chain, size-> {
            PGraphics2D graphics = (PGraphics2D) processing.createGraphics(size.get(0), size.get(1), P2D);
            graphics.noSmooth();
            graphics.textureSampling(3);//linear
            return graphics;
        });
    }
}