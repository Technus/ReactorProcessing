package com.github.technus.processingReactor.objects;

import com.github.technus.processingReactor.MainProcessing;
import reactor.core.Disposable;

import java.util.ArrayList;
import java.util.List;

public abstract class ProcessingObjectBase implements IProcessingObject {
    private final List<Disposable> disposables =new ArrayList<>();

    public List<Disposable> getDisposables() {
        return disposables;
    }

    @Override
    public void destroy(MainProcessing app) {
        disposables.forEach(Disposable::dispose);
    }
}
