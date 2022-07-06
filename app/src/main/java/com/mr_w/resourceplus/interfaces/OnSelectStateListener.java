package com.mr_w.resourceplus.interfaces;

public interface OnSelectStateListener<T> {
    void OnSelectStateChanged(boolean state, T file);
}