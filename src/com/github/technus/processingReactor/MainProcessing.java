package com.github.technus.processingReactor;

import com.github.technus.processingReactor.objects.Circle;
import com.github.technus.processingReactor.objects.Sphere;
import com.github.technus.processingReactor.reactive.ReactiveListener;
import com.github.technus.processingReactor.reactive.SynchronousExecutor;
import com.github.technus.processingReactor.reactive.draw.Handler2DSharp;
import com.github.technus.processingReactor.reactive.draw.Handler3D;
import com.github.technus.processingReactor.reactive.draw.ScreenLayerHandler;
import processing.core.PApplet;
import processing.event.KeyEvent;
import processing.event.MouseEvent;
import processing.opengl.PGraphicsOpenGL;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadLocalRandom;

import static com.github.technus.processingReactor.Utility.isDebug;
import static com.github.technus.processingReactor.Utility.noOperation;

/**
 * Created by Tec on 10.04.2017.
 */
public class MainProcessing extends PApplet {
    private final SynchronousExecutor executor=new SynchronousExecutor("sync",new LinkedBlockingQueue<>());
    private final Scheduler synchronous =Schedulers.fromExecutor(executor);
    private final Scheduler asynchronous = Schedulers.newElastic("async");
    private final Scheduler renderInvalidator =Schedulers.newParallel("renderInvalidator");
    private final Scheduler ticker=Schedulers.newSingle("ticker");

    private final List<ScreenLayerHandler<?>> layers=new ArrayList<>();

    private final ReactiveListener<Long> ticking=new ReactiveListener<>(this::asyncCached);

    private final List<Integer> sizeList = new ArrayList<>();
    private final List<Integer> sizeListU = Collections.unmodifiableList(sizeList);
    private final ReactiveListener<List<Integer>> size = new ReactiveListener<>(this::asyncCached);

    private final Set<Integer> keysHeldSet = new HashSet<>();
    private final Set<Integer> keysHeldSetU = Collections.unmodifiableSet(keysHeldSet);
    private final ReactiveListener<Set<Integer>> keysHeld = new ReactiveListener<>(this::asyncCached);
    private final ReactiveListener<KeyEvent> keyboard = new ReactiveListener<>(this::async);

    private final List<Integer> mouseList = new ArrayList<>();
    private final List<Integer> mouseListU = Collections.unmodifiableList(mouseList);
    private final ReactiveListener<Optional<List<Integer>>> mousePosition = new ReactiveListener<>(this::asyncCached);

    private final Set<Integer> mouseHeldSet = new HashSet<>();
    private final Set<Integer> mouseHeldSetU = Collections.unmodifiableSet(mouseHeldSet);
    private final ReactiveListener<Set<Integer>> mouseHeld = new ReactiveListener<>(this::asyncCached);
    private final ReactiveListener<MouseEvent> mouse = new ReactiveListener<>(this::async);

    public static void main(String[] args) {
        PApplet.main(MainProcessing.class.getCanonicalName());
    }

    public <T> Flux<T> asyncCached(Flux<T> f) {
        return f.subscribeOn(getAsynchronous()).share().cache(1);
    }

    public <T> Flux<T> async(Flux<T> f) {
        return f.subscribeOn(getAsynchronous()).share();
    }

    public <T> Flux<T> sync(Flux<T> f) {
        return f.subscribeOn(getSynchronous()).share();
    }

    public List<ScreenLayerHandler<?>> getLayers() {
        return layers;
    }

    public Scheduler getAsynchronous() {
        return asynchronous;
    }

    public Scheduler getSynchronous() {
        return synchronous;
    }

    @Override
    public void settings() {
        mouseList.add(0);
        mouseList.add(0);
        sizeList.add(640);
        sizeList.add(480);
        size(sizeList.get(0), sizeList.get(1), P3D);
        size.getSink().next(sizeListU);

        mousePosition.getSink().next(Optional.empty());
    }

    @Override
    public void setup() {
        getSurface().setResizable(true);

        background(0);

        Flux.interval(Duration.ofMillis(100),ticker).subscribe(t->ticking.getSink().next(t));
    }

    @Override
    public void draw() {
        if (sizeList.get(0) != width || sizeList.get(1) != height) {
            sizeList.set(0, width);
            sizeList.set(1, height);
            size.getSink().next(sizeListU);
        }

        executor.runSynchronousExecutor();

        background(0);

        List<Integer> resizeList = Optional.ofNullable(getSizeFlux().blockFirst()).orElse(sizeListU);
        Flux.merge(Flux.fromIterable(layers))
                .parallel()
                .runOn(renderInvalidator)
                .doOnNext(screenLayerHandler -> screenLayerHandler.nextInvalidate(resizeList,this))
                .then()
                .block();
        Flux.concat(Flux.fromIterable(layers))
                .subscribe(screenLayerHandler -> screenLayerHandler.nextDraw(resizeList, this));
    }

    @Override
    protected void handleMouseEvent(MouseEvent event) {
        //debug event mutator not needed
        super.handleMouseEvent(event);
        mouse.getSink().next(event);
        if (MouseEvent.EXIT == event.getAction()) {
            mousePosition.getSink().next(Optional.empty());
        } else {
            mouseList.set(0, event.getX());
            mouseList.set(1, event.getY());
            mousePosition.getSink().next(Optional.of(mouseListU));
        }
        if (MouseEvent.PRESS == event.getAction() && mouseHeldSet.add(event.getButton())) {
            mouseHeld.getSink().next(mouseHeldSetU);
        } else if (MouseEvent.RELEASE == event.getAction() && mouseHeldSet.remove(event.getButton())) {
            mouseHeld.getSink().next(mouseHeldSetU);
        }
    }

    @Override
    protected void handleKeyEvent(KeyEvent event) {
        if (isDebug()) {
            event = new KeyEvent(event.getNative(), event.getMillis(), event.getAction(),
                    event.getModifiers(), event.getKey(), event.getKeyCode(), event.isAutoRepeat()) {

                private String actionString() {
                    switch (this.action) {
                        case 1:
                            return "PRESS";
                        case 2:
                            return "RELEASE";
                        case 3:
                            return "TYPE";
                        default:
                            return "UNKNOWN";
                    }
                }

                @Override
                public String toString() {
                    return String.format("<KeyEvent %s@%s,%d modifiers:%d repeating:%s>", this.actionString(), this.getKey(), this.getKeyCode(), this.getModifiers(), this.isAutoRepeat());
                }
            };
        }
        super.handleKeyEvent(event);
        keyboard.getSink().next(event);
        if (!event.isAutoRepeat()) {
            if (KeyEvent.PRESS == event.getAction() && keysHeldSet.add(event.getKeyCode())) {
                keysHeld.getSink().next(keysHeldSetU);
            } else if (KeyEvent.RELEASE == event.getAction() && keysHeldSet.remove(event.getKeyCode())) {
                keysHeld.getSink().next(keysHeldSetU);
            }
        }
    }

    public Flux<List<Integer>> getSizeFlux() {
        return size.getFlux();
    }

    public Flux<Set<Integer>> getKeysHeldFlux() {
        return keysHeld.getFlux();
    }

    public Flux<KeyEvent> getKeyboardFlux() {
        return keyboard.getFlux();
    }

    public Flux<Optional<List<Integer>>> getMousePositionFlux() {
        return mousePosition.getFlux();
    }

    public Flux<Set<Integer>> getMouseHeldFlux() {
        return mouseHeld.getFlux();
    }

    public Flux<MouseEvent> getMouseFlux() {
        return mouse.getFlux();
    }

    public Flux<Long> getTickerFlux(){
        return ticking.getFlux();
    }
}
