package com.tien.tensor.presentation.security

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tien.tensor.domain.usecase.GeneratePasswordUseCase
import com.tien.tensor.domain.usecase.GetSecurityReportUseCase
import com.tien.tensor.domain.usecase.HashTextUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SecurityViewModel(
    private val getSecurityReportUseCase: GetSecurityReportUseCase,
    private val generatePasswordUseCase: GeneratePasswordUseCase,
    private val hashTextUseCase: HashTextUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(SecurityUiState())
    val uiState: StateFlow<SecurityUiState> = _state.asStateFlow()

    init {
        refreshAudit()
    }

    fun refreshAudit() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val report = getSecurityReportUseCase()
            _state.update { it.copy(isLoading = false, report = report) }
        }
    }

    fun onPasswordLengthSelected(length: Int) {
        _state.update { it.copy(passwordLength = length) }
    }

    fun onGeneratePassword() {
        _state.update { it.copy(generatedPassword = generatePasswordUseCase(it.passwordLength)) }
    }

    fun onHashInputChanged(input: String) {
        _state.update { it.copy(hashInput = input, hashOutput = hashTextUseCase(input)) }
    }
}
