package edu.nd.cnguyen8.hwapp.five.repositories

import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import edu.nd.cnguyen8.hwapp.five.data.UserProfile
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseProfileRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : ProfileRepository {

    override suspend fun getUserProfile(uid: String): Result<UserProfile> {
        return try {
            val snapshot = firestore.collection("users")
                .document(uid)
                .get()
                .await()

            val userProfile = snapshot.toObject(UserProfile::class.java)
                ?: return Result.failure(Exception("User profile not found"))

            Result.success(userProfile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateUserProfile(userProfile: UserProfile): Result<Unit> {
        return try {
            firestore.collection("users")
                .document(userProfile.uid)
                .set(userProfile)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun uploadProfileImage(uid: String, imageUri: Uri): Result<String> {
        return try {
            val ref = storage.reference.child("profile_images/$uid.jpg")
            ref.putFile(imageUri).await()
            val downloadUrl = ref.downloadUrl.await().toString()
            Result.success(downloadUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}