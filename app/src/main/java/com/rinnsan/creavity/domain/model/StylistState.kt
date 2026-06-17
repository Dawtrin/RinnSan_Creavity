package com.rinnsan.creavity.domain.models

/**
 * ═══════════════════════════════════════════════════════════════════
 * STYLIST STATE MACHINE - BOX B SYSTEM STATES
 * ═══════════════════════════════════════════════════════════════════
 *
 * This is NOT an AI state machine.
 * This is a SYSTEM READINESS state machine.
 *
 * Philosophy:
 * Box B (The Stylist) is a SYSTEM that needs to be activated.
 * Each state represents a different level of system engagement.
 *
 * States control:
 * - UI visibility
 * - Feature availability
 * - User permissions
 * - System behavior
 *
 * States do NOT control:
 * - AI model execution (no OpenAI here)
 * - Data computation (that's IdentityEngine)
 * - Content generation (that's content layer)
 *
 * This is PURELY about system readiness and UI orchestration.
 *
 * STATE TRANSITION FLOW:
 * ┌──────────┐
 * │ OFFLINE  │ → User has no IdentityProfile
 * └────┬─────┘
 *      │ [Profile acquired]
 *      ▼
 * ┌──────────┐
 * │ ONLINE   │ → System ready, waiting for user action
 * └────┬─────┘
 *      │ [User requests analysis]
 *      ▼
 * ┌──────────┐
 * │ ANALYZE  │ → System processing request
 * └────┬─────┘
 *      │ [Analysis complete]
 *      ▼
 * ┌──────────┐
 * │ SUGGEST  │ → System presenting results
 * └────┬─────┘
 *      │ [User archives conversation]
 *      ▼
 * ┌──────────┐
 * │ ARCHIVE  │ → Conversation saved, system idle
 * └────┬─────┘
 *      │ [User starts new session]
 *      ▼
 *   [Back to ONLINE]
 */

/**
 * StylistState
 *
 * Represents the current operational state of Box B (The Stylist system).
 *
 * IMPORTANT: This is NOT AI state. This is SYSTEM state.
 *
 * Each state defines:
 * - What UI elements are visible
 * - What actions are available
 * - What system behaviors are active
 * - How the user should feel
 *
 * @property displayName Human-readable state name for debugging
 * @property isInteractive Whether user can interact with system
 * @property canReceiveInput Whether user can send new requests
 * @property showLoadingIndicator Whether loading UI should be shown
 */
sealed class StylistState(
    val displayName: String,
    val isInteractive: Boolean,
    val canReceiveInput: Boolean,
    val showLoadingIndicator: Boolean
) {

    /**
     * ═══════════════════════════════════════════════════════════════
     * STATE: OFFLINE
     * ═══════════════════════════════════════════════════════════════
     *
     * TECHNICAL MEANING:
     * - User has not completed Identity Scanner (Box A)
     * - No IdentityProfile exists
     * - Box B cannot operate without this data
     *
     * UI BEHAVIOR:
     * - Stylist interface is LOCKED
     * - Show "Identity Scan Required" message
     * - Display CTA to complete Scanner
     * - All features disabled
     *
     * EMOTIONAL DESIGN:
     *
     * User should feel: EXCLUDED but CURIOUS
     *
     * Visual Language:
     * - Locked icon (cannot access)
     * - Red/GlitchRed color (system offline)
     * - "ACCESS DENIED" messaging
     * - Brutalist locked gate aesthetic
     *
     * Copy Examples:
     * - "STYLIST OFFLINE"
     * - "IDENTITY SCAN REQUIRED FOR ACCESS"
     * - "COMPLETE SCANNER TO UNLOCK SYSTEM"
     *
     * Psychology:
     * User feels like they're OUTSIDE a secure facility.
     * They can see the system exists, but cannot enter.
     * This creates DESIRE to complete the Scanner.
     *
     * Not frustration - ANTICIPATION.
     * The lock implies something valuable is inside.
     *
     * Interaction Rules:
     * - Cannot send messages
     * - Cannot view suggestions
     * - Can only navigate to Scanner
     * - Can view preview of what's locked
     */
    data object OFFLINE : StylistState(
        displayName = "OFFLINE",
        isInteractive = false,
        canReceiveInput = false,
        showLoadingIndicator = false
    )

    /**
     * ═══════════════════════════════════════════════════════════════
     * STATE: ONLINE
     * ═══════════════════════════════════════════════════════════════
     *
     * TECHNICAL MEANING:
     * - User has completed Identity Scanner
     * - IdentityProfile exists and is valid
     * - System is ready to receive requests
     * - Waiting for user to initiate interaction
     *
     * UI BEHAVIOR:
     * - Input field is active
     * - "Send" button enabled
     * - Show user's archetype badge
     * - Display conversation history (if any)
     * - All features unlocked
     *
     * EMOTIONAL DESIGN:
     *
     * User should feel: EMPOWERED and READY
     *
     * Visual Language:
     * - CyberAcid accents (system active)
     * - Cursor blinking in input field
     * - "SYSTEM ONLINE" status
     * - Clean, empty canvas ready for input
     *
     * Copy Examples:
     * - "STYLIST ONLINE"
     * - "READY FOR INPUT"
     * - "ASK ANYTHING ABOUT YOUR STYLE"
     *
     * Psychology:
     * User feels like they're at a COMMAND PROMPT.
     * The system is LISTENING, waiting for their command.
     * Empty canvas = possibility.
     *
     * This is the feeling of sitting at a terminal,
     * cursor blinking, infinite potential.
     *
     * Not overwhelming - INVITING.
     * The blank state invites exploration.
     *
     * Interaction Rules:
     * - Can send messages
     * - Can view past conversations
     * - Can start new analysis
     * - Cannot see results yet (none exist)
     */
    data object ONLINE : StylistState(
        displayName = "ONLINE",
        isInteractive = true,
        canReceiveInput = true,
        showLoadingIndicator = false
    )

    /**
     * ═══════════════════════════════════════════════════════════════
     * STATE: ANALYZE
     * ═══════════════════════════════════════════════════════════════
     *
     * TECHNICAL MEANING:
     * - User has sent a request
     * - System is processing (could be AI call, database query, etc.)
     * - Response is being generated
     * - User must wait
     *
     * UI BEHAVIOR:
     * - Input field DISABLED
     * - Show loading animation (scanning line, not spinner)
     * - Display "ANALYZING..." status
     * - User's message visible
     * - Response area shows loading state
     *
     * EMOTIONAL DESIGN:
     *
     * User should feel: ANTICIPATION and TRUST
     *
     * Visual Language:
     * - CyberAcid scanning line (active processing)
     * - "ANALYZING..." text (system working)
     * - Monospace font (technical process)
     * - No playful spinners (clinical)
     *
     * Copy Examples:
     * - "ANALYZING REQUEST..."
     * - "PROCESSING QUERY..."
     * - "SYSTEM COMPUTING..."
     *
     * Psychology:
     * User feels like their request is being TAKEN SERIOUSLY.
     * The system is WORKING for them.
     *
     * This is the feeling of watching a diagnostic tool run,
     * knowing that accurate results take time.
     *
     * Not impatient - ENGAGED.
     * The loading state creates anticipation for the result.
     *
     * Timing Matters:
     * - 0-2 seconds: User feels system is fast
     * - 2-5 seconds: User expects quality result
     * - 5+ seconds: Show more detailed status
     *
     * Interaction Rules:
     * - Cannot send new messages
     * - Cannot interrupt processing
     * - Can view conversation history
     * - Cannot navigate away (optional: allow with warning)
     */
    data class ANALYZE(
        val requestId: String,
        val userMessage: String,
        val startTime: Long = System.currentTimeMillis()
    ) : StylistState(
        displayName = "ANALYZE",
        isInteractive = false,
        canReceiveInput = false,
        showLoadingIndicator = true
    ) {
        /**
         * Get processing duration in milliseconds
         */
        fun getDuration(): Long {
            return System.currentTimeMillis() - startTime
        }

        /**
         * Check if processing is taking too long
         * Returns true if > 10 seconds
         */
        fun isTimeout(): Boolean {
            return getDuration() > 10000
        }
    }

    /**
     * ═══════════════════════════════════════════════════════════════
     * STATE: SUGGEST
     * ═══════════════════════════════════════════════════════════════
     *
     * TECHNICAL MEANING:
     * - Processing complete
     * - Response has been generated
     * - Results are ready to display
     * - User can view and interact with suggestions
     *
     * UI BEHAVIOR:
     * - Display system response
     * - Show suggestions/recommendations
     * - Input field re-enabled (can ask follow-up)
     * - Show "New Analysis" button
     * - Show "Archive Conversation" button
     *
     * EMOTIONAL DESIGN:
     *
     * User should feel: INFORMED and EMPOWERED
     *
     * Visual Language:
     * - Results appear with subtle animation
     * - CyberAcid highlights on key points
     * - Clear typography hierarchy
     * - Actionable buttons for next steps
     *
     * Copy Examples:
     * - "ANALYSIS COMPLETE"
     * - "RECOMMENDATIONS READY"
     * - "BASED ON YOUR GHOST IDENTITY..."
     *
     * Psychology:
     * User feels like they've received VALUABLE INTELLIGENCE.
     * The suggestions are PERSONALIZED and ACTIONABLE.
     *
     * This is the feeling of getting results from a diagnostic scan,
     * seeing clear, specific, useful information.
     *
     * Not generic - SPECIFIC.
     * Not vague - ACTIONABLE.
     *
     * The response should feel like it was MADE FOR THEM
     * because it was based on their IdentityProfile.
     *
     * Interaction Rules:
     * - Can send follow-up questions
     * - Can archive this conversation
     * - Can start new analysis
     * - Can share results (optional)
     */
    data class SUGGEST(
        val requestId: String,
        val userMessage: String,
        val systemResponse: String,
        val suggestions: List<Suggestion> = emptyList(),
        val timestamp: Long = System.currentTimeMillis()
    ) : StylistState(
        displayName = "SUGGEST",
        isInteractive = true,
        canReceiveInput = true,
        showLoadingIndicator = false
    )

    /**
     * ═══════════════════════════════════════════════════════════════
     * STATE: ARCHIVE
     * ═══════════════════════════════════════════════════════════════
     *
     * TECHNICAL MEANING:
     * - User has archived the conversation
     * - Session is saved for later reference
     * - System is idle, ready for new session
     * - Previous results remain accessible
     *
     * UI BEHAVIOR:
     * - Show "Conversation Archived" confirmation
     * - Clear active conversation view
     * - Show "Start New Session" button
     * - Display archive access link
     * - Return to clean slate
     *
     * EMOTIONAL DESIGN:
     *
     * User should feel: ACCOMPLISHED and ORGANIZED
     *
     * Visual Language:
     * - Green checkmark or "ARCHIVED" badge
     * - Fade out current conversation
     * - Show archive icon/indicator
     * - Clean transition to empty state
     *
     * Copy Examples:
     * - "CONVERSATION ARCHIVED"
     * - "SESSION SAVED"
     * - "READY FOR NEW ANALYSIS"
     *
     * Psychology:
     * User feels like they've COMPLETED A TASK.
     * Information is SAVED and RETRIEVABLE.
     *
     * This is the feeling of closing a notebook,
     * knowing your notes are preserved and organized.
     *
     * Not lost - STORED.
     * Not ended - PAUSED.
     *
     * The archive creates CLOSURE without FINALITY.
     * User can always come back.
     *
     * Interaction Rules:
     * - Can view archived conversation
     * - Can start new session (returns to ONLINE)
     * - Can browse archive
     * - Cannot continue archived conversation directly
     */
    data class ARCHIVE(
        val conversationId: String,
        val archivedAt: Long = System.currentTimeMillis(),
        val messageCount: Int
    ) : StylistState(
        displayName = "ARCHIVE",
        isInteractive = true,
        canReceiveInput = false,
        showLoadingIndicator = false
    ) {
        /**
         * Get human-readable archive time
         */
        fun getArchiveTimeAgo(): String {
            val now = System.currentTimeMillis()
            val diff = now - archivedAt

            return when {
                diff < 60000 -> "Just now"
                diff < 3600000 -> "${diff / 60000} minutes ago"
                diff < 86400000 -> "${diff / 3600000} hours ago"
                else -> "${diff / 86400000} days ago"
            }
        }
    }
}

/**
 * Suggestion
 *
 * A single actionable suggestion from the system.
 * Used in SUGGEST state to display recommendations.
 *
 * @property id Unique identifier
 * @property title Short title (e.g., "Try layered look")
 * @property description Detailed explanation
 * @property actionUrl Optional link to product/article
 * @property confidence How confident the system is (0.0-1.0)
 */
data class Suggestion(
    val id: String,
    val title: String,
    val description: String,
    val actionUrl: String? = null,
    val confidence: Float = 1.0f
)

/**
 * ═══════════════════════════════════════════════════════════════════
 * STATE TRANSITION RULES
 * ═══════════════════════════════════════════════════════════════════
 *
 * Valid transitions:
 *
 * OFFLINE → ONLINE
 *   Trigger: User completes Identity Scanner
 *
 * ONLINE → ANALYZE
 *   Trigger: User sends request
 *
 * ANALYZE → SUGGEST
 *   Trigger: Processing complete
 *
 * ANALYZE → ONLINE
 *   Trigger: Processing error (fallback)
 *
 * SUGGEST → ANALYZE
 *   Trigger: User sends follow-up question
 *
 * SUGGEST → ARCHIVE
 *   Trigger: User archives conversation
 *
 * ARCHIVE → ONLINE
 *   Trigger: User starts new session
 *
 * ANY → OFFLINE
 *   Trigger: IdentityProfile expires or is deleted
 *
 * Invalid transitions:
 * - OFFLINE → ANALYZE (missing profile)
 * - ARCHIVE → SUGGEST (cannot resume directly)
 * - ANALYZE → ARCHIVE (must finish or error first)
 */

/**
 * ═══════════════════════════════════════════════════════════════════
 * EMOTIONAL DESIGN SUMMARY
 * ═══════════════════════════════════════════════════════════════════
 *
 * OFFLINE: "I want in, but I need to complete something first"
 * ────────────────────────────────────────────────────────────────
 * Emotion: EXCLUSION + CURIOSITY
 * Color: GlitchRed (locked)
 * Icon: Lock
 * Action: Complete Identity Scanner
 *
 * Psychological Effect:
 * Creates desire through exclusivity. User sees the locked system
 * and wants access. The barrier is clear and achievable.
 *
 *
 * ONLINE: "The system is ready, what should I ask?"
 * ────────────────────────────────────────────────────────────────
 * Emotion: EMPOWERMENT + POSSIBILITY
 * Color: CyberAcid (active)
 * Icon: Blinking cursor
 * Action: Type and send message
 *
 * Psychological Effect:
 * Blank canvas invites exploration. User feels in control.
 * The empty state is inviting, not intimidating.
 *
 *
 * ANALYZE: "Something is happening, I should wait"
 * ────────────────────────────────────────────────────────────────
 * Emotion: ANTICIPATION + TRUST
 * Color: CyberAcid (processing)
 * Icon: Scanning line
 * Action: Wait and watch
 *
 * Psychological Effect:
 * Loading state creates anticipation. User trusts the process
 * because it looks serious and technical, not frivolous.
 *
 *
 * SUGGEST: "This is for me, I should act on this"
 * ────────────────────────────────────────────────────────────────
 * Emotion: INFORMED + MOTIVATED
 * Color: CyberAcid (highlights)
 * Icon: Checkmark or arrow
 * Action: Read and respond
 *
 * Psychological Effect:
 * Personalized results feel valuable. User feels understood.
 * Actionable suggestions motivate next steps.
 *
 *
 * ARCHIVE: "Done and saved, I can move on"
 * ────────────────────────────────────────────────────────────────
 * Emotion: ACCOMPLISHMENT + ORGANIZATION
 * Color: TeslaWhite/TechSilver (neutral)
 * Icon: Archive box
 * Action: Start new or view archive
 *
 * Psychological Effect:
 * Closure without loss. User feels organized and productive.
 * The conversation is preserved but no longer active.
 */

/**
 * ═══════════════════════════════════════════════════════════════════
 * USAGE EXAMPLE
 * ═══════════════════════════════════════════════════════════════════
 *
 * ```kotlin
 * // In ViewModel
 * class StylistViewModel : ViewModel() {
 *     private val _state = MutableStateFlow<StylistState>(StylistState.OFFLINE)
 *     val state: StateFlow<StylistState> = _state.asStateFlow()
 *
 *     fun onProfileAcquired(profile: IdentityProfile) {
 *         _state.value = StylistState.ONLINE
 *     }
 *
 *     fun onUserMessage(message: String) {
 *         val requestId = UUID.randomUUID().toString()
 *         _state.value = StylistState.ANALYZE(requestId, message)
 *
 *         // Process request (AI call, database, etc.)
 *         processRequest(message) { response ->
 *             _state.value = StylistState.SUGGEST(
 *                 requestId = requestId,
 *                 userMessage = message,
 *                 systemResponse = response
 *             )
 *         }
 *     }
 *
 *     fun onArchive(conversationId: String, messageCount: Int) {
 *         _state.value = StylistState.ARCHIVE(conversationId, messageCount)
 *     }
 * }
 *
 * // In UI
 * @Composable
 * fun StylistScreen(viewModel: StylistViewModel) {
 *     val state by viewModel.state.collectAsState()
 *
 *     when (state) {
 *         is StylistState.OFFLINE -> OfflineView()
 *         is StylistState.ONLINE -> OnlineView()
 *         is StylistState.ANALYZE -> AnalyzeView(state as StylistState.ANALYZE)
 *         is StylistState.SUGGEST -> SuggestView(state as StylistState.SUGGEST)
 *         is StylistState.ARCHIVE -> ArchiveView(state as StylistState.ARCHIVE)
 *     }
 * }
 * ```
 */