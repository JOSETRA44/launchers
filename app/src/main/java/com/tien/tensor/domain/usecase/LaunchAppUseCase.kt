package com.tien.tensor.domain.usecase

import com.tien.tensor.domain.port.AppLauncher

class LaunchAppUseCase(private val appLauncher: AppLauncher) {
    operator fun invoke(packageName: String) = appLauncher.launch(packageName)
}
