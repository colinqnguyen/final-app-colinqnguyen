package edu.nd.cnguyen8.hwapp.five.screens.profile

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.nd.cnguyen8.hwapp.five.data.UserProfile
import edu.nd.cnguyen8.hwapp.five.repositories.ProfileRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository
) : ViewModel() {

    var nickname by mutableStateOf("")
        private set

    var greeting by mutableStateOf("")
        private set

    var shirtColor by mutableStateOf("Blue")
        private set

    var profileImageUrl by mutableStateOf("")
        private set

    var selectedImageUri by mutableStateOf<Uri?>(null)
        private set

    val shirtColorOptions = listOf(
        "Blue", "Red", "Green", "Yellow",
        "Black", "White", "Purple", "Orange"
    )

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var saveMessage by mutableStateOf<String?>(null)
        private set

    private var currentProfile: UserProfile? = null
    private var uid: String = ""
    private var email: String = ""

    init {
        loadProfile()
    }

    fun onNicknameChange(newNickname: String) {
        nickname = newNickname
    }

    fun onGreetingChange(newGreeting: String) {
        greeting = newGreeting
    }

    fun onShirtColorChange(newColor: String) {
        shirtColor = newColor
    }

    fun onImageSelected(uri: Uri?) {
        selectedImageUri = uri
    }

    fun loadProfile() {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return

        uid = currentUser.uid
        email = currentUser.email ?: ""

        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            val result = profileRepository.getUserProfile(uid)

            isLoading = false
            result
                .onSuccess { profile ->
                    currentProfile = profile
                    nickname = profile.nickname
                    greeting = profile.greeting
                    shirtColor = profile.shirtColor
                    profileImageUrl = profile.profileImageUrl
                }
                .onFailure {
                    errorMessage = it.message
                }
        }
    }

    fun saveProfile() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            saveMessage = null

            val previousProfile = currentProfile
            var finalImageUrl = profileImageUrl

            selectedImageUri?.let { uri ->
                val uploadResult = profileRepository.uploadProfileImage(uid, uri)
                uploadResult
                    .onSuccess { uploadedUrl ->
                        finalImageUrl = uploadedUrl
                    }
                    .onFailure {
                        isLoading = false
                        errorMessage = it.message
                        return@launch
                    }
            }

            val updatedProfile = UserProfile(
                uid = uid,
                email = email,
                nickname = nickname,
                greeting = greeting,
                shirtColor = shirtColor,
                profileImageUrl = finalImageUrl,
                tokens = previousProfile?.tokens ?: 0,
                dailyGoal = previousProfile?.dailyGoal ?: 10000,
                streakCount = previousProfile?.streakCount ?: 0,
                lastRewardStepMilestone = previousProfile?.lastRewardStepMilestone ?: 0,
                lastStreakDate = previousProfile?.lastStreakDate ?: ""
            )

            val result = profileRepository.updateUserProfile(updatedProfile)

            isLoading = false
            result
                .onSuccess {
                    currentProfile = updatedProfile
                    profileImageUrl = updatedProfile.profileImageUrl
                    selectedImageUri = null
                    saveMessage = "Profile saved"
                }
                .onFailure {
                    errorMessage = it.message
                }
        }
    }
}