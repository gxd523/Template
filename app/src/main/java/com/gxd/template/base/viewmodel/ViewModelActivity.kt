package com.gxd.template.base.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.gxd.template.base.binding.ViewBindingActivity
import java.lang.reflect.ParameterizedType

abstract class ViewModelActivity<V : ViewBinding, M : ViewModel> : ViewBindingActivity<V>() {
    protected val viewModel by lazy {
        val viewModelClass = getViewModelClass<M>(this) ?: throw Exception("can not get ViewModel Class")
        val viewModelFactory = ViewModelFactory(application)
        ViewModelProvider(this, viewModelFactory)[viewModelClass]
    }

    private fun <T : ViewModel> getViewModelClass(any: Any): Class<T>? {
        val genericSuperclass = any::class.java.genericSuperclass ?: return null
        if (genericSuperclass !is ParameterizedType) return null
        for (typeArgument in genericSuperclass.actualTypeArguments) {
            val genericTypeClass = typeArgument as? Class<*> ?: break
            if (ViewModel::class.java.isAssignableFrom(genericTypeClass)) {
                @Suppress("UNCHECKED_CAST")
                return typeArgument as? Class<T>
            }
        }
        return null
    }

    private class ViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val constructor = modelClass.getConstructor(Application::class.java)
            return constructor.newInstance(application)
        }
    }
}