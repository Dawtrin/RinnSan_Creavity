package com.rinnsan.creavity.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rinnsan.creavity.domain.models.IdentityProfile
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// ─── DataStore extension (file-level, không đặt trong class) ──────────────────
private val Context.identityDataStore: DataStore<Preferences>
        by preferencesDataStore(name = "identity_profile")

/**
 * ═══════════════════════════════════════════════════════════════════
 * IDENTITY REPOSITORY
 * ═══════════════════════════════════════════════════════════════════
 *
 * Single source of truth cho IdentityProfile.
 * Responsibilities:
 *  1. Persist profile xuống DataStore (Preferences) dưới dạng JSON
 *  2. Load profile khi app khởi động
 *  3. Phát sự kiện qua SharedFlow để UplinkViewModel observe
 *     mà không cần biết IdentityScannerViewModel tồn tại
 *
 * Data flow:
 *   IdentityScannerViewModel
 *       └─ saveProfile(profile)
 *               ├─ ghi vào DataStore
 *               └─ emit vào _profileEvents (SharedFlow)
 *                       └─ UplinkViewModel.collect() → setIdentityProfile()
 */
@Singleton
class IdentityRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson
) {

    // ──────────────────────────────────────────────────────────────
    // KEYS
    // ──────────────────────────────────────────────────────────────

    private object Keys {
        val PROFILE_JSON = stringPreferencesKey("identity_profile_json")
    }

    // ──────────────────────────────────────────────────────────────
    // SHARED FLOW — Bridge giữa Scanner và Uplink
    // replay = 1: UplinkViewModel collect được event ngay cả khi
    // subscribe sau khi Scanner đã emit (ví dụ: rotation)
    // ──────────────────────────────────────────────────────────────

    private val _profileEvents = MutableSharedFlow<IdentityProfile>(replay = 1)
    val profileEvents: SharedFlow<IdentityProfile> = _profileEvents.asSharedFlow()

    // ──────────────────────────────────────────────────────────────
    // SAVE
    // ──────────────────────────────────────────────────────────────

    /**
     * Lưu profile xuống DataStore VÀ phát sự kiện cho các observer.
     *
     * Được gọi bởi: IdentityScannerViewModel sau khi computeIdentity()
     * hoàn tất.
     *
     * Throws: KHÔNG throw — lỗi được log và bỏ qua để không crash UI.
     */
    suspend fun saveProfile(profile: IdentityProfile) {
        try {
            // Serialize: IdentityProfile → Map → JSON string
            val profileMap = profile.toMap()
            val json = gson.toJson(profileMap)

            context.identityDataStore.edit { prefs ->
                prefs[Keys.PROFILE_JSON] = json
            }

            // Notify observers (UplinkViewModel)
            _profileEvents.emit(profile)

        } catch (e: Exception) {
            android.util.Log.e("IdentityRepository", "saveProfile failed: ${e.message}", e)
            // Vẫn emit event để UI không bị treo, dù persistence thất bại
            _profileEvents.emit(profile)
        }
    }

    // ──────────────────────────────────────────────────────────────
    // LOAD
    // ──────────────────────────────────────────────────────────────

    /**
     * Nạp profile từ DataStore.
     *
     * Return: IdentityProfile nếu tìm thấy và parse thành công,
     *         null nếu chưa có dữ liệu hoặc dữ liệu bị corrupt.
     *
     * Được gọi bởi: UplinkViewModel.init{}
     */
    suspend fun loadProfile(): IdentityProfile? {
        return try {
            val json = context.identityDataStore.data
                .map { prefs -> prefs[Keys.PROFILE_JSON] }
                .firstOrNull()
                ?: return null // Chưa có dữ liệu → null hợp lệ

            // Deserialize: JSON string → Map → IdentityProfile
            // Dùng TypeToken vì Gson cần type hint cho Map<String, Any?>
            val mapType = object : TypeToken<Map<String, Any?>>() {}.type
            val profileMap: Map<String, Any?> = gson.fromJson(json, mapType)

            IdentityProfile.fromMap(profileMap)
                ?: run {
                    android.util.Log.w(
                        "IdentityRepository",
                        "fromMap() returned null — profile corrupt, clearing storage"
                    )
                    clearProfile() // Xóa data corrupt để tránh loop
                    null
                }

        } catch (e: Exception) {
            android.util.Log.e("IdentityRepository", "loadProfile failed: ${e.message}", e)
            null
        }
    }

    /**
     * Flow liên tục của profile từ DataStore.
     * Dùng khi cần observe thay đổi real-time (tuỳ chọn).
     */
    val profileFlow: Flow<IdentityProfile?> = context.identityDataStore.data
        .map { prefs ->
            val json = prefs[Keys.PROFILE_JSON] ?: return@map null
            try {
                val mapType = object : TypeToken<Map<String, Any?>>() {}.type
                val profileMap: Map<String, Any?> = gson.fromJson(json, mapType)
                IdentityProfile.fromMap(profileMap)
            } catch (e: Exception) {
                android.util.Log.e("IdentityRepository", "profileFlow parse error", e)
                null
            }
        }

    // ──────────────────────────────────────────────────────────────
    // CLEAR
    // ──────────────────────────────────────────────────────────────

    /**
     * Xóa profile khỏi DataStore.
     * Được gọi bởi: UplinkViewModel.clearIdentity()
     */
    suspend fun clearProfile() {
        try {
            context.identityDataStore.edit { prefs ->
                prefs.remove(Keys.PROFILE_JSON)
            }
        } catch (e: Exception) {
            android.util.Log.e("IdentityRepository", "clearProfile failed: ${e.message}", e)
        }
    }

    // ──────────────────────────────────────────────────────────────
    // UTILITIES
    // ──────────────────────────────────────────────────────────────

    /**
     * Kiểm tra có profile đã lưu không (không deserialize đầy đủ).
     */
    suspend fun hasProfile(): Boolean {
        return try {
            context.identityDataStore.data
                .map { prefs -> prefs[Keys.PROFILE_JSON] != null }
                .firstOrNull() ?: false
        } catch (e: Exception) {
            false
        }
    }
}