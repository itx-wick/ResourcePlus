package com.mr_w.resourceplus.callbacks;

public abstract class GenericCallbacks<T> {

    public void onJsonSuccess(T response) {

    }

    public void onStringSuccess(T response) {

    }

    public void onMultiPartSuccess(T response) {

    }

    public abstract void onFailure(String message);

}
