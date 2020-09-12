package com.demo.module;

import android.content.Context;
import android.content.Intent;

import com.demo.annotation.Bridge;
import com.demo.bean.ModuleBean;
import com.demo.bridge.ModuleBridge;

import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.schedulers.Schedulers;

@Bridge
public class ModuleBridgeImpl implements ModuleBridge {
    private boolean flag;
    private int n;

    @Override
    public Single<ModuleBean> requestData() {
        return Single.create((SingleOnSubscribe<ModuleBean>) emitter -> {
            flag = !flag;
            ModuleBean moduleBean = new ModuleBean(flag ? "aaa" : "bbb", n++);
            emitter.onSuccess(moduleBean);
        }).subscribeOn(Schedulers.newThread());
    }

    @Override
    public void launchModuleActivity(Context context) {
        Intent intent = new Intent(context, ModuleActivity.class);
        context.startActivity(intent);
    }
}
