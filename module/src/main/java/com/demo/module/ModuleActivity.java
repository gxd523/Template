package com.demo.module;

import android.os.Bundle;

import com.demo.base.BaseActivity;
import com.demo.module.databinding.ActivityModuleBinding;

//@Layout(value = R.layout.activity_module)
public class ModuleActivity extends BaseActivity<ActivityModuleBinding, ModuleViewModel> {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel.title.postValue(getClass().getSimpleName());
    }

    @Override
    public int getContentViewId() {
        return R.layout.activity_module;
    }
}