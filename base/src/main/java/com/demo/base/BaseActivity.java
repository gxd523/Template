package com.demo.base;

import android.os.Bundle;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.AndroidViewModel;

/**
 * Created by guoxiaodong on 2020/9/9 09:29
 */
public abstract class BaseActivity<VDB extends ViewDataBinding, VM extends AndroidViewModel> extends FragmentActivity {
    protected VDB binding;
    protected VM viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getContentViewId() != 0) {
            binding = DataBindingUtil.setContentView(this, getContentViewId());
            binding.setLifecycleOwner(this);
        }
        createViewModel();
        bindViewModel();
    }

    public void createViewModel() {
        if (viewModel == null) {
            Class<? extends AndroidViewModel> modelClass;
            Type type = getClass().getGenericSuperclass();
            if (type instanceof ParameterizedType) {
                Type genericType = ((ParameterizedType) type).getActualTypeArguments()[1];
                modelClass = (Class<? extends AndroidViewModel>) genericType;
            } else {
                modelClass = BaseViewModel.class;
            }
            viewModel = (VM) BaseApplication.viewModelFactory.create(modelClass);
//            mViewModel.setObjectLifecycleTransformer(bindToLifecycle());
        }
    }

    @LayoutRes
    public int getContentViewId() {
        return 0;
    }

    public void bindViewModel() {
    }
}