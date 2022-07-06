package com.mr_w.resourceplus.injections.di.component;


import com.mr_w.resourceplus.injections.di.module.DialogModule;
import com.mr_w.resourceplus.injections.di.scope.DialogScope;

import dagger.Component;

@DialogScope
@Component(modules = DialogModule.class, dependencies = AppComponent.class)
public interface DialogComponent {

//    void inject(RateUsDialog dialog);

}
