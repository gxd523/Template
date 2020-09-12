package com.demo.app.home;

import android.os.Bundle;

import com.demo.annotation.Layout;
import com.demo.app.R;
import com.demo.app.databinding.ActivityHomeBinding;
import com.demo.base.BaseActivity;

import androidx.annotation.Nullable;

@Layout(value = R.layout.activity_home)
public class HomeActivity extends BaseActivity<ActivityHomeBinding, HomeViewModel> {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding.setViewModel(viewModel);
    }
}