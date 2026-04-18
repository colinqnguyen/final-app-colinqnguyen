package edu.nd.cnguyen8.hwapp.five.data

data class UserProfile(
    val uid: String = "",
    val email: String = "",
    val nickname: String = "",
    val greeting: String = "",
    val shirtColor: String = "Blue",
    val profileImageUrl: String = "",
    val tokens: Int = 0,
    val dailyGoal: Int = 10000,
    val streakCount: Int = 0,
    val lastRewardStepMilestone: Int = 0,
    val lastStreakDate: String = ""
)