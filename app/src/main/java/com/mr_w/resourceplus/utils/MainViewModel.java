package com.mr_w.resourceplus.utils;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MainViewModel extends ViewModel {
    //initialize variable
    MutableLiveData<String> mutableLiveData = new MutableLiveData<>();

    //Create Set Text Method
    public void setText(String s){
        mutableLiveData.setValue(s);
    }

    //create get text method
    public MutableLiveData<String> getText(){
        return mutableLiveData;
    }
}
