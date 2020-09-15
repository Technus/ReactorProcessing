package com.github.technus.processingReactor.reactive.draw;

import com.github.technus.processingReactor.MainProcessing;
import processing.core.PGraphics;
import reactor.core.publisher.*;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.github.technus.processingReactor.Utility.noAction;

public abstract class ScreenLayerHandler<Graphics extends PGraphics> {
    private final Function<List<Integer>, Graphics> graphicsSupplier;
    private Consumer<Graphics>
            graphicsPreBegin= noAction(),
            graphicsPostBeginInitialize=noAction(),
            graphicsPostBegin= noAction(),
            graphicsPreEnd= noAction(),
            graphicsPostEnd= noAction();
    private Graphics graphics;

    private final Flux<ScreenLayerHandler<Graphics>> fluxInvalidate;
    private final FluxSink<ScreenLayerHandler<Graphics>> sinkInvalidate;
    private volatile boolean invalidated=true;

    private final Flux<Graphics> fluxDraw;
    private final FluxSink<Graphics> sinkDraw;

    protected ScreenLayerHandler(Function<Flux<ScreenLayerHandler<Graphics>>, Flux<ScreenLayerHandler<Graphics>>> chain,
                                 Function<List<Integer>, Graphics> graphicsSupplier) {
        this.graphicsSupplier = graphicsSupplier;

        FluxProcessor<Graphics, Graphics> processor= DirectProcessor.create();
        this.fluxDraw = processor.share();
        this.sinkDraw =processor.sink(FluxSink.OverflowStrategy.DROP);

        FluxProcessor<ScreenLayerHandler<Graphics>,ScreenLayerHandler<Graphics>> processorInvalidate=EmitterProcessor.create(1);
        this.fluxInvalidate = processorInvalidate.filter(t->!t.invalidated).transform(chain);
        this.sinkInvalidate=processorInvalidate.sink(FluxSink.OverflowStrategy.DROP);
    }

    public Consumer<Graphics> getGraphicsPreBegin() {
        return graphicsPreBegin;
    }

    public void setGraphicsPreBegin(Consumer<Graphics> graphicsPreBegin) {
        this.graphicsPreBegin = graphicsPreBegin;
        if(graphics!=null){
            initializeGraphics(Arrays.asList(graphics.width,graphics.height));
        }
    }

    public Consumer<Graphics> getGraphicsPostBegin() {
        return graphicsPostBegin;
    }

    public void setGraphicsPostBegin(Consumer<Graphics> graphicsPostBegin) {
        this.graphicsPostBegin = graphicsPostBegin;
        if(graphics!=null){
            initializeGraphics(Arrays.asList(graphics.width,graphics.height));
        }
    }

    public Consumer<Graphics> getGraphicsPreEnd() {
        return graphicsPreEnd;
    }

    public void setGraphicsPreEnd(Consumer<Graphics> graphicsPreEnd) {
        this.graphicsPreEnd = graphicsPreEnd;
        if(graphics!=null){
            initializeGraphics(Arrays.asList(graphics.width,graphics.height));
        }
    }

    public Consumer<Graphics> getGraphicsPostEnd() {
        return graphicsPostEnd;
    }

    public void setGraphicsPostEnd(Consumer<Graphics> graphicsPostEnd) {
        this.graphicsPostEnd = graphicsPostEnd;
        if(graphics!=null){
            initializeGraphics(Arrays.asList(graphics.width,graphics.height));
        }
    }

    public Consumer<Graphics> getGraphicsPostBeginInitialize() {
        return graphicsPostBeginInitialize;
    }

    public void setGraphicsPostBeginInitialize(Consumer<Graphics> graphicsPostBeginInitialize) {
        this.graphicsPostBeginInitialize = graphicsPostBeginInitialize;
        if(graphics!=null){
            initializeGraphics(Arrays.asList(graphics.width,graphics.height));
        }
    }

    public void setInvalidated() {
        invalidated = true;
    }

    public Flux<Graphics> getFluxDraw() {
        return fluxDraw;
    }

    public Flux<ScreenLayerHandler<Graphics>> getFluxInvalidate() {
        return fluxInvalidate;
    }

    public void initializeGraphics(List<Integer> initialSize){
        if(graphics!=null){
            graphics.dispose();
        }
        graphics = graphicsSupplier.apply(initialSize);
        graphicsPreBegin.accept(graphics);
        graphics.beginDraw();
        graphicsPostBeginInitialize.accept(graphics);
        graphicsPostBegin.accept(graphics);
        sinkDraw.next(graphics);
        graphicsPreEnd.accept(graphics);
        graphics.endDraw();
        graphicsPostEnd.accept(graphics);
        setInvalidated();
    }

    public void nextInvalidate(List<Integer> resizeList, MainProcessing mainProcessing) {
        if (resizeList.get(0) == graphics.width && resizeList.get(1) == graphics.height) {
            sinkInvalidate.next(this);
        }
    }

    public void nextDraw(List<Integer> resizeList, MainProcessing mainProcessing) {
        if(resizeList.get(0)!=graphics.width || resizeList.get(1)!=graphics.height){
            setInvalidated();
            graphics.dispose();
            graphics = graphicsSupplier.apply(resizeList);
            graphicsPreBegin.accept(graphics);
            graphics.beginDraw();
            graphicsPostBeginInitialize.accept(graphics);
            graphicsPostBegin.accept(graphics);
            sinkDraw.next(graphics);
            graphicsPreEnd.accept(graphics);
            graphics.endDraw();
            graphicsPostEnd.accept(graphics);
            invalidated=false;
        }else if(invalidated){
            graphicsPreBegin.accept(graphics);
            graphics.beginDraw();
            graphicsPostBegin.accept(graphics);
            graphics.clear();
            sinkDraw.next(graphics);
            graphicsPreEnd.accept(graphics);
            graphics.endDraw();
            graphicsPostEnd.accept(graphics);
            invalidated=false;
        }
        mainProcessing.image(graphics,0,0);
    }
}
