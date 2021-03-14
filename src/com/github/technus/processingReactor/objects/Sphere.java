package com.github.technus.processingReactor.objects;

import com.github.technus.processingReactor.MainProcessing;
import com.github.technus.processingReactor.reactive.draw.ScreenLayerHandler;
import processing.opengl.PGraphics3D;

public class Sphere extends ProcessingObjectBase {
    private final ScreenLayerHandler<PGraphics3D> layer;
    private static final int EXTENT=20;
    int x,y,ox,oy;

    public Sphere(ScreenLayerHandler<PGraphics3D> layer){
        this(layer,0,0);
    }

    public Sphere(ScreenLayerHandler<PGraphics3D> layer, int ox, int oy) {
        this.layer = layer;
        this.ox = ox;
        this.oy = oy;
    }

    @Override
    public void initialize(MainProcessing app) {
        getDisposables().add(layer.getFluxDraw().subscribe(layer -> {
            app.getMousePositionFlux().doOnNext(System.out::println).blockFirst().ifPresent(ints -> {
                x = ints.get(0) + ox;
                y = ints.get(1) + oy;
            });
            if (x < app.width + EXTENT && x > -EXTENT && y > -EXTENT && y < app.height + EXTENT) {
                layer.pushMatrix();
                layer.translate(x, y);
                layer.sphere(EXTENT);
                layer.popMatrix();
            }
        }));
    }
}
