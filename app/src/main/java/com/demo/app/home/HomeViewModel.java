package com.demo.app.home;

import android.app.Application;
import android.view.View;

import com.demo.base.BaseViewModel;
import com.demo.bean.ModuleBean;
import com.demo.bridge.ModuleBridge;
import com.demo.bridge.BridgeManager;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

/**
 * 相当于Presenter
 */
public final class HomeViewModel extends BaseViewModel {
    //    public MutableLiveData<ModuleBean> moduleBean = new MutableLiveData<>();
//    public ModuleBean moduleBean = new ModuleBean("ccc", 99);
    public ObservableField<ModuleBean> moduleBean = new ObservableField<>();
    private ModuleBridge moduleBridge;

    public HomeViewModel(@NonNull Application application) {
        super(application);
    }

    public void onJumpClick(View view) {
        if (moduleBridge == null) {
            moduleBridge = BridgeManager.getBridge(ModuleBridge.class);
        }
        if (moduleBridge != null) {
            moduleBridge.launchModuleActivity(view.getContext());
        }
    }

    public void onGetDataClick(View view) {
        if (moduleBridge == null) {
            moduleBridge = BridgeManager.getBridge(ModuleBridge.class);
        }
        if (moduleBridge == null) {
            return;
        }
        moduleBridge.requestData()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<ModuleBean>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(ModuleBean moduleBean) {
//                        HomeViewModel.this.moduleBean = moduleBean;
                        HomeViewModel.this.moduleBean.set(moduleBean);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                });
    }

    public CharSequence getTitle() {
//        return view.getContext().getClass().getSimpleName();
        return "xxxx";
    }
}