package edu.nd.cnguyen8.hwapp.five.screens.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.health.connect.client.HealthConnectClient
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.nd.cnguyen8.hwapp.five.data.UserProfile
import edu.nd.cnguyen8.hwapp.five.data.health.HealthConnectManager
import edu.nd.cnguyen8.hwapp.five.repositories.AuthRepository
import edu.nd.cnguyen8.hwapp.five.repositories.ProfileRepository
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.time.LocalDate
import kotlin.math.max

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository,
    private val healthConnectManager: HealthConnectManager
) : ViewModel() {

    var nickname by mutableStateOf("")
        private set

    var tokens by mutableStateOf(0)
        private set

    var dailyGoal by mutableStateOf(10000)
        private set

    var streakCount by mutableStateOf(0)
        private set

    var lastRewardStepMilestone by mutableStateOf(0)
        private set

    var lastStreakDate by mutableStateOf("")
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var showInfoDialog by mutableStateOf(false)
        private set

    var showGoalDialog by mutableStateOf(false)
        private set

    var goalInput by mutableStateOf("")
        private set

    var hasHealthPermission by mutableStateOf(false)
        private set

    var healthConnectAvailable by mutableStateOf(true)
        private set

    private var currentProfile: UserProfile? = null

    var currentSteps by mutableStateOf(0)
        private set

    var debugStepOffset by mutableStateOf(0)
        private set

    val caloriesBurnedToday: Int
        get() = (displayedSteps * 0.04).toInt()

    val averageSteps: Int
        get() = maxOf(6000, dailyGoal - 1000)

    var showAddStepsDialog by mutableStateOf(false)
        private set

    var testStepsInput by mutableStateOf("")
        private set

    val displayedSteps: Int
        get() = currentSteps + debugStepOffset

    val stepsUntilNextCoin: Int
        get() {
            val remainder = displayedSteps % 2000
            return if (remainder == 0) 0 else 2000 - remainder
        }

    val goalCompletedToday: Boolean
        get() = displayedSteps >= dailyGoal

    val goalProgress: Float
        get() = (displayedSteps.toFloat() / dailyGoal).coerceIn(0f, 1f)

    init {
        loadProfile()
        checkHealthConnectAvailability()
    }

    fun loadProfile() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            val result = profileRepository.getUserProfile(uid)

            isLoading = false
            result
                .onSuccess { profile ->
                    currentProfile = profile
                    nickname = profile.nickname
                    tokens = profile.tokens
                    dailyGoal = profile.dailyGoal
                    streakCount = profile.streakCount
                    lastRewardStepMilestone = profile.lastRewardStepMilestone
                    lastStreakDate = profile.lastStreakDate
                    goalInput = profile.dailyGoal.toString()
                }
                .onFailure {
                    errorMessage = it.message
                }
        }
    }

    private fun checkHealthConnectAvailability() {
        val status = HealthConnectManager.isAvailable(healthConnectManager.context)
        healthConnectAvailable =
            status == HealthConnectClient.SDK_AVAILABLE
    }

    fun refreshHealthPermissionState() {
        viewModelScope.launch {
            try {
                hasHealthPermission = healthConnectManager.hasAllPermissions()
                if (hasHealthPermission) {
                    loadTodaySteps()
                }
            } catch (e: Exception) {
                errorMessage = e.message
            }
        }
    }

    fun loadTodaySteps() {
        viewModelScope.launch {
            try {
                currentSteps = healthConnectManager.readTodaySteps()
                checkForStepRewards()
                checkForDailyGoalCompletion()
            } catch (e: Exception) {
                errorMessage = e.message
            }
        }
    }

    fun logout() {
        authRepository.signOut()
    }

    fun openInfoDialog() {
        showInfoDialog = true
    }

    fun closeInfoDialog() {
        showInfoDialog = false
    }

    fun openGoalDialog() {
        goalInput = dailyGoal.toString()
        showGoalDialog = true
    }

    fun closeGoalDialog() {
        showGoalDialog = false
    }

    fun onGoalInputChange(newValue: String) {
        goalInput = newValue
    }

    fun saveDailyGoal() {
        val parsedGoal = goalInput.toIntOrNull()

        if (parsedGoal == null || parsedGoal < 2000) {
            errorMessage = "Daily goal must be at least 2000"
            return
        }

        val profile = currentProfile ?: return
        val updatedProfile = profile.copy(dailyGoal = parsedGoal)

        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            val result = profileRepository.updateUserProfile(updatedProfile)

            isLoading = false
            result
                .onSuccess {
                    currentProfile = updatedProfile
                    dailyGoal = updatedProfile.dailyGoal
                    showGoalDialog = false
                }
                .onFailure {
                    errorMessage = it.message
                }
        }
    }

    fun requiredHealthPermissions(): Set<String> = healthConnectManager.permissions

    fun openAddStepsDialog() {
        testStepsInput = ""
        showAddStepsDialog = true
    }

    fun closeAddStepsDialog() {
        showAddStepsDialog = false
    }

    fun onTestStepsInputChange(newValue: String) {
        testStepsInput = newValue
    }

    fun addCustomTestSteps() {
        val stepCount = testStepsInput.toIntOrNull()

        if (stepCount == null || stepCount <= 0) {
            errorMessage = "Enter a valid number of steps"
            return
        }

        debugStepOffset += stepCount
        showAddStepsDialog = false
        testStepsInput = ""
        checkForStepRewards()
        checkForDailyGoalCompletion()
    }

    fun resetDebugSteps() {
        debugStepOffset = 0
    }

    fun checkForStepRewards() {
        val profile = currentProfile ?: return

        val completedMilestone = (displayedSteps / 2000) * 2000

        if (completedMilestone <= lastRewardStepMilestone) {
            return
        }

        val newCoins = (completedMilestone - lastRewardStepMilestone) / 2000

        val updatedProfile = profile.copy(
            tokens = profile.tokens + newCoins,
            lastRewardStepMilestone = completedMilestone
        )

        viewModelScope.launch {
            val result = profileRepository.updateUserProfile(updatedProfile)

            result
                .onSuccess {
                    currentProfile = updatedProfile
                    tokens = updatedProfile.tokens
                    lastRewardStepMilestone = updatedProfile.lastRewardStepMilestone
                }
                .onFailure {
                    errorMessage = it.message
                }
        }
    }

    fun checkForDailyGoalCompletion() {
        val profile = currentProfile ?: return

        if (displayedSteps < dailyGoal) {
            return
        }

        val today = LocalDate.now()
        val todayString = today.toString()

        if (lastStreakDate == todayString) {
            return
        }

        val yesterdayString = today.minusDays(1).toString()

        val newStreakCount = if (lastStreakDate == yesterdayString) {
            profile.streakCount + 1
        } else {
            1
        }

        val updatedProfile = profile.copy(
            streakCount = newStreakCount,
            lastStreakDate = todayString
        )

        viewModelScope.launch {
            val result = profileRepository.updateUserProfile(updatedProfile)

            result
                .onSuccess {
                    currentProfile = updatedProfile
                    streakCount = updatedProfile.streakCount
                    lastStreakDate = updatedProfile.lastStreakDate
                }
                .onFailure {
                    errorMessage = it.message
                }
        }
    }
}