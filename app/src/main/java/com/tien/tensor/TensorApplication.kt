package com.tien.tensor

import android.app.Application
import com.tien.tensor.di.AppModule

class TensorApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppModule.init(this)
    }
}
