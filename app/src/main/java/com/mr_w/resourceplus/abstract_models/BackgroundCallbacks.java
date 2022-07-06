package com.mr_w.resourceplus.abstract_models;

public abstract class BackgroundCallbacks<T> {

    public void onPreProcessing() {
    }

    public abstract void onFailure();

    public abstract void onCompleted(T data);

}
