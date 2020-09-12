package com.demo.bridge;

import android.content.Context;

import java.util.HashSet;
import java.util.Set;

public enum ApplicationLifecycleOwner {
    INSTANCE;
    private Set<ApplicationLifecycleObserver> observerSet = new HashSet<>();

    public void addObserver(ApplicationLifecycleObserver observer) {
        observerSet.add(observer);
    }

    public void notifyOnCreate() {
        for (ApplicationLifecycleObserver observer : observerSet) {
            observer.onCreate();
        }
    }

    public void notifyOnAttachBaseContext(Context context) {
        for (ApplicationLifecycleObserver observer : observerSet) {
            observer.attachBaseContext(context);
        }
    }
}
