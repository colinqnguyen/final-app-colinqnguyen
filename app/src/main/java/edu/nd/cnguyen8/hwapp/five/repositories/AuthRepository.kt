package edu.nd.cnguyen8.hwapp.five.repositories

interface AuthRepository {
    suspend fun signUp(email: String, password: String): Result<Unit>
    suspend fun signIn(email: String, password: String): Result<Unit>
    fun signOut()
    fun getCurrentUserId(): String?
}