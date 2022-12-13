package com.gxd.template.base.binding

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.viewbinding.ViewBinding

abstract class ViewBindingActivity<V : ViewBinding> : ComponentActivity() {
    private var _binding: V? = null
    protected val binding: V by lazy {
        val viewBindingClass = getViewBindingClass<V>()
        _binding = viewBindingClass?.callInflate(layoutInflater, attachToParent = false)

        if (_binding == null) {
            throw RuntimeException("Something wrong with ViewBinding init!")
        }
        _binding!!
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }
}