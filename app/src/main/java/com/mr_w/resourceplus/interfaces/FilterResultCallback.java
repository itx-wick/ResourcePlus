package com.mr_w.resourceplus.interfaces;

import com.mr_w.resourceplus.utils.BaseFile;
import com.mr_w.resourceplus.utils.Directory;

import java.util.List;

public interface FilterResultCallback<T extends BaseFile> {
    void onResult(List<Directory<T>> directories);
}