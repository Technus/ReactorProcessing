package com.github.technus.processingReactor.objects;

import com.github.technus.processingReactor.MainProcessing;

public interface IProcessingObject {
    void initialize(MainProcessing app);
    void destroy(MainProcessing app);
}
