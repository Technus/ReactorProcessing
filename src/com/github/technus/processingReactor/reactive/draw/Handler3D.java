package com.github.technus.processingReactor.reactive.draw;

import com.github.technus.processingReactor.MainProcessing;
import processing.opengl.PGraphics3D;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.function.Function;

import static com.github.technus.processingReactor.Utility.noOperation;
import static processing.core.PConstants.P3D;

public class Handler3D extends ScreenLayerHandler<PGraphics3D>{
    public Handler3D(MainProcessing processing) {
        this(noOperation(), processing);
    }

    public Handler3D(Function<Flux<ScreenLayerHandler<PGraphics3D>>, Flux<ScreenLayerHandler<PGraphics3D>>> chain,
                     MainProcessing processing) {
        this(chain, size->(PGraphics3D)processing.createGraphics(size.get(0),size.get(1), P3D));
    }

    protected Handler3D(Function<Flux<ScreenLayerHandler<PGraphics3D>>, Flux<ScreenLayerHandler<PGraphics3D>>> chain,
                        Function<List<Integer>, PGraphics3D> graphicsSupplier) {
        super(chain, graphicsSupplier);
    }


}
