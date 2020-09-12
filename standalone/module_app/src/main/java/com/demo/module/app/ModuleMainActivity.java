package com.demo.module.app;

import android.os.Bundle;

import com.demo.annotation.Layout;
import com.demo.base.BaseActivity;
import com.demo.module.app.databinding.ActivityModuleMainBinding;

@Layout(value = R.layout.activity_module_main)
public class ModuleMainActivity extends BaseActivity<ActivityModuleMainBinding, ModuleMainViewModel> {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding.setViewModel(viewModel);

        viewModel.title.postValue(getClass().getSimpleName());
    }
}
