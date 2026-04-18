package edu.nd.cnguyen8.hwapp.five.di

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import edu.nd.cnguyen8.hwapp.five.data.health.HealthConnectManager
import edu.nd.cnguyen8.hwapp.five.repositories.AuthRepository
import edu.nd.cnguyen8.hwapp.five.repositories.FirebaseAuthRepository
import edu.nd.cnguyen8.hwapp.five.repositories.FirebaseProfileRepository
import edu.nd.cnguyen8.hwapp.five.repositories.ProfileRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage = FirebaseStorage.getInstance()

    @Provides
    @Singleton
    fun provideHealthConnectManager(
        @ApplicationContext context: Context
    ): HealthConnectManager = HealthConnectManager(context)

    @Provides
    @Singleton
    fun provideAuthRepository(
        auth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): AuthRepository = FirebaseAuthRepository(auth, firestore)

    @Provides
    @Singleton
    fun provideProfileRepository(
        firestore: FirebaseFirestore,
        storage: FirebaseStorage
    ): ProfileRepository = FirebaseProfileRepository(firestore, storage)
}