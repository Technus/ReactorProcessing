package com.github.technus.processingReactor.objects;

import com.github.technus.processingReactor.MainProcessing;
import com.github.technus.processingReactor.reactive.draw.ScreenLayerHandler;
import processing.opengl.PGraphics3D;
import reactor.core.Disposable;

import java.util.Optional;

public class Sphere extends ProcessingObjectBase {
    private final ScreenLayerHandler<PGraphics3D> layer;
    private static final int EXTENT=20;
    int x,y;
    int ox,oy;
    Disposable disposable;

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
        getDisposables().add(app.getMousePositionFlux()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .subscribe(ints -> {
                    x = ints.get(0) + ox;
                    y = ints.get(1) + oy;
                    if (x < app.width + EXTENT && x > -EXTENT && y > -EXTENT && y < app.height + EXTENT) {
                        if (disposable == null) {
                            disposable = layer.getFlux().subscribe(layer -> {
                                layer.pushMatrix();
                                layer.translate(x, y);
                                layer.sphere(EXTENT);
                                layer.popMatrix();
                            });
                        }
                    } else if (disposable != null) {
                        disposable.dispose();
                        disposable = null;
                    }
                }));
    }

    @Override
    public void destroy(MainProcessing app) {
        super.destroy(app);
        if(disposable!=null){
            disposable.dispose();
            disposable=null;
        }
    }
}
