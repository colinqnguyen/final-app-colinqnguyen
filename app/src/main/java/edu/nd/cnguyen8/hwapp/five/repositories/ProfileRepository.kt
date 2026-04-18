package edu.nd.cnguyen8.hwapp.five.repositories

import android.net.Uri
import edu.nd.cnguyen8.hwapp.five.data.UserProfile

interface ProfileRepository {
    suspend fun getUserProfile(uid: String): Result<UserProfile>
    suspend fun updateUserProfile(userProfile: UserProfile): Result<Unit>
    suspend fun uploadProfileImage(uid: String, imageUri: Uri): Result<String>
}