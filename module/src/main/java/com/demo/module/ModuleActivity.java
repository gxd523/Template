package com.demo.module;

import android.os.Bundle;

import com.demo.annotation.Layout;
import com.demo.base.BaseActivity;
import com.demo.module1.R;
import com.demo.module1.databinding.ActivityModuleBinding;

@Layout(value = R.layout.activity_module)
public class ModuleActivity extends BaseActivity<ActivityModuleBinding, ModuleViewModel> {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding.setViewModel(viewModel);

        viewModel.title.postValue(getClass().getSimpleName());
    }
}