package edu.nd.cnguyen8.hwapp.five.screens.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.nd.cnguyen8.hwapp.five.repositories.AuthRepository
import javax.inject.Inject
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    var email by mutableStateOf("")
        private set

    var password by mutableStateOf("")
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var isLoggedIn by mutableStateOf(false)
        private set

    fun onEmailChange(newEmail: String) {
        email = newEmail
    }

    fun onPasswordChange(newPassword: String) {
        password = newPassword
    }

    fun signUp() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            val result = authRepository.signUp(email, password)

            isLoading = false
            result
                .onSuccess {
                    isLoggedIn = true
                }
                .onFailure {
                    errorMessage = it.message
                }
        }
    }

    fun signIn() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            val result = authRepository.signIn(email, password)

            isLoading = false
            result
                .onSuccess {
                    isLoggedIn = true
                }
                .onFailure {
                    errorMessage = it.message
                }
        }
    }
}