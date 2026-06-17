package com.rinnsan.creavity.core.di

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.rinnsan.creavity.core.constants.Constants
import com.rinnsan.creavity.data.remote.CloudinaryApi
import com.rinnsan.creavity.data.repository.SignalRepository
import com.rinnsan.creavity.data.repository.AdminRepositoryImpl
import com.rinnsan.creavity.data.repository.VaultRepositoryImpl
import com.rinnsan.creavity.data.repository.WishlistRepositoryImpl
import com.rinnsan.creavity.domain.repository.AdminRepository
import com.rinnsan.creavity.domain.repository.VaultRepository
import com.rinnsan.creavity.domain.repository.WishlistRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * ═══════════════════════════════════════════════════════════════════
 * NETWORK MODULE - DEPENDENCY INJECTION
 * ═══════════════════════════════════════════════════════════════════
 *
 * Updated: Added Firebase + Cloudinary for Signal V2.0
 */

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // EXISTING - RETROFIT (Keep for other APIs)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://rinnsan-api.firebaseapp.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // NEW - FIREBASE (For Signal V2.0)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance().apply {
            firestoreSettings = com.google.firebase.firestore.FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)  // Offline support
                .build()
        }
    }

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // NEW - CLOUDINARY (Image Upload)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    @Provides
    @Singleton
    fun provideCloudinaryApi(
        @ApplicationContext context: Context
    ): CloudinaryApi {
        return CloudinaryApi(context)
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // NEW - REPOSITORIES
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    @Provides
    @Singleton
    fun provideSignalRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth,
        cloudinaryApi: CloudinaryApi
    ): SignalRepository {
        return SignalRepository(firestore, auth, cloudinaryApi)
    }

    @Provides
    @Singleton
    fun provideAdminRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth,
        cloudinaryApi: CloudinaryApi
    ): AdminRepository {
        return AdminRepositoryImpl(firestore, auth, cloudinaryApi)
    }

    @Provides
    @Singleton
    fun provideVaultRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth
    ): VaultRepository {
        return VaultRepositoryImpl(firestore, auth)
    }

    @Provides
    @Singleton
    fun provideWishlistRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth
    ): WishlistRepository {
        return WishlistRepositoryImpl(firestore, auth)
    }
}