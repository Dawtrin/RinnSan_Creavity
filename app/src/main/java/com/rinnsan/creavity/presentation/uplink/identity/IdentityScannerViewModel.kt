package com.rinnsan.creavity.presentation.uplink.identity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rinnsan.creavity.data.IDENTITY_QUESTIONS
import com.rinnsan.creavity.data.repository.IdentityRepository
import com.rinnsan.creavity.domain.engine.IdentityEngine
import com.rinnsan.creavity.domain.models.IdentityProfile
import com.rinnsan.creavity.domain.models.Question
import com.rinnsan.creavity.domain.models.QuestionResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ═══════════════════════════════════════════════════════════════════
 * IDENTITY SCANNER VIEWMODEL — v2.0 WITH PERSISTENCE
 * ═══════════════════════════════════════════════════════════════════
 *
 * Thay đổi so với v1:
 *  [+] Inject IdentityRepository
 *  [+] processIdentity() → gọi repository.saveProfile() sau khi tính xong
 *      → Repository tự emit profileEvents → UplinkViewModel nhận
 *
 * Data flow hoàn chỉnh:
 *   User chọn answer (x15)
 *     └─ onAnswerSelected()
 *           └─ processIdentity()
 *                 ├─ IdentityEngine.computeIdentity()
 *                 ├─ repository.saveProfile(profile)   ← [+] NEW
 *                 │       ├─ ghi DataStore
 *                 │       └─ emit profileEvents (SharedFlow)
 *                 │               └─ UplinkViewModel.observeScannerEvents()
 *                 │                       └─ applyProfile() → UI online
 *                 └─ _uiState = Completed(profile)     ← local UI
 */

sealed class ScannerUiState {
    object Initial : ScannerUiState()
    data class Scanning(
        val currentQuestionIndex: Int,
        val totalQuestions: Int,
        val responses: List<QuestionResponse>
    ) : ScannerUiState()

    object Processing : ScannerUiState()

    data class Completed(
        val profile: IdentityProfile
    ) : ScannerUiState()

    data class Error(val message: String) : ScannerUiState()
}

@HiltViewModel
class IdentityScannerViewModel @Inject constructor(
    private val identityRepository: IdentityRepository  // [+] NEW
) : ViewModel() {

    private val _uiState = MutableStateFlow<ScannerUiState>(ScannerUiState.Initial)
    val uiState: StateFlow<ScannerUiState> = _uiState.asStateFlow()

    private val _questions = MutableStateFlow<List<Question>>(emptyList())
    val questions: StateFlow<List<Question>> = _questions.asStateFlow()

    private val responses = mutableListOf<QuestionResponse>()

    init {
        loadQuestions()
    }

    // ──────────────────────────────────────────────────────────────
    // LOAD QUESTIONS
    // ──────────────────────────────────────────────────────────────

    private fun loadQuestions() {
        _questions.value = IDENTITY_QUESTIONS

        _uiState.value = if (IDENTITY_QUESTIONS.isNotEmpty()) {
            ScannerUiState.Scanning(
                currentQuestionIndex = 0,
                totalQuestions = IDENTITY_QUESTIONS.size,
                responses = emptyList()
            )
        } else {
            ScannerUiState.Error("No questions available")
        }
    }

    // ──────────────────────────────────────────────────────────────
    // ANSWER HANDLING
    // ──────────────────────────────────────────────────────────────

    fun onAnswerSelected(questionResponse: QuestionResponse) {
        val currentState = _uiState.value
        if (currentState !is ScannerUiState.Scanning) return

        responses.add(questionResponse)

        val nextIndex = currentState.currentQuestionIndex + 1

        if (nextIndex < currentState.totalQuestions) {
            _uiState.value = ScannerUiState.Scanning(
                currentQuestionIndex = nextIndex,
                totalQuestions = currentState.totalQuestions,
                responses = responses.toList()
            )
        } else {
            processIdentity()
        }
    }

    // ──────────────────────────────────────────────────────────────
    // COMPUTE + PERSIST  [UPDATED]
    // ──────────────────────────────────────────────────────────────

    /**
     * Tính toán profile từ responses, sau đó:
     *  1. Lưu xuống DataStore qua Repository
     *  2. Repository emit SharedFlow → UplinkViewModel tự động nhận
     *  3. Cập nhật local UI state → Completed
     */
    private fun processIdentity() {
        viewModelScope.launch {
            try {
                _uiState.value = ScannerUiState.Processing

                // Delay "scanning" effect
                delay(1500)

                // Tính toán
                val profile = IdentityEngine.computeIdentity(
                    responses = responses,
                    userId = null
                )

                // [+] Persist & notify UplinkViewModel via SharedFlow
                // Không cần biết UplinkViewModel tồn tại — Repository làm cầu nối
                identityRepository.saveProfile(profile)

                // Cập nhật local UI
                _uiState.value = ScannerUiState.Completed(profile)

            } catch (e: Exception) {
                android.util.Log.e(
                    "IdentityScannerViewModel",
                    "processIdentity failed: ${e.message}",
                    e
                )
                _uiState.value = ScannerUiState.Error(
                    message = e.message ?: "Unknown error during identity computation"
                )
            }
        }
    }

    // ──────────────────────────────────────────────────────────────
    // RESET
    // ──────────────────────────────────────────────────────────────

    fun resetScanner() {
        responses.clear()
        _uiState.value = ScannerUiState.Scanning(
            currentQuestionIndex = 0,
            totalQuestions = _questions.value.size,
            responses = emptyList()
        )
    }
}