package com.rinnsan.creavity.presentation.signal

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.rinnsan.creavity.core.theme.*
import com.rinnsan.creavity.domain.model.SignalPost as DomainPost
import com.rinnsan.creavity.domain.model.getFormattedLikes
import com.rinnsan.creavity.domain.model.getFormattedTimestamp
import com.rinnsan.creavity.domain.model.getFormattedComments
import com.rinnsan.creavity.domain.model.isLikedBy
import com.rinnsan.creavity.presentation.signal.viewmodel.SignalViewModel
import com.rinnsan.creavity.presentation.signal.viewmodel.UserFeedPost
import com.rinnsan.creavity.domain.model.Comment as SignalComment
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.res.painterResource
import com.rinnsan.creavity.R
import androidx.hilt.navigation.compose.hiltViewModel
import com.rinnsan.creavity.presentation.signal.components.CommentBottomSheet





// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// DATA MODELS - THE SIGNAL (LOCAL MOCK)
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// DATA MODELS - THE SIGNAL
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
sealed class SignalPost {
    // User submitted outfit photos
    data class OutfitPost(
        val username: String,
        val location: String,
        val timestamp: String,
        val description: String,
        val imageRes: Int,
        val likes: Int,
        val tags: List<String>
    ) : SignalPost()

    // Style guide articles (text + images)
    data class StyleGuide(
        val title: String,
        val subtitle: String,
        val author: String,
        val readTime: String,
        val content: List<GuideSection>,
        val coverImageRes: Int?
    ) : SignalPost()

    // Pure text thoughts/manifestos
    data class Manifesto(
        val title: String,
        val body: String,
        val author: String,
        val date: String
    ) : SignalPost()

    // Community trend carousel
    data class TrendCarousel(
        val title: String,
        val description: String,
        val images: List<TrendImage>
    ) : SignalPost()

    // Breaking signal (like ticker)
    data class BreakingSignal(val message: String) : SignalPost()

    // Featured collection
    data class FeaturedMoment(
        val title: String,
        val description: String,
        val imageRes: Int,
        val timestamp: String

    ) : SignalPost()
}

data class GuideSection(
    val heading: String,
    val text: String,
    val imageRes: Int? = null   // 🔥 FIX
)


data class TrendImage(
    val imageRes: Int,
    val caption: String
)

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// HELPER: Load image from assets
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

@Composable
fun TheSignalScreen(
    navController: NavController,
    viewModel: SignalViewModel = hiltViewModel()
) {
    // Firestore feed state
    val uiState by viewModel.uiState.collectAsState()
    val userCreatedPosts by viewModel.userCreatedPosts.collectAsState()
    val currentUserId = remember { FirebaseAuth.getInstance().currentUser?.uid ?: "" }

    // Comment dialog state — XÓA, dùng CommentBottomSheet thay thế
    // var commentTargetPost by remember { mutableStateOf<DomainPost?>(null) }
    // var commentText by remember { mutableStateOf("") }

    // Delete dialog state
    var deleteTargetPost by remember { mutableStateOf<DomainPost?>(null) }

    // View comments dialog state — XÓA, dùng CommentBottomSheet thay thế
    // var viewCommentsPost by remember { mutableStateOf<DomainPost?>(null) }

    var showFilterMenu by remember { mutableStateOf(false) }
    var filterByUser by remember { mutableStateOf<String?>(null) }
    var selectedPostForComments by remember { mutableStateOf<DomainPost?>(null) }

    val signalFeed = remember {
        listOf(
            // 1. Featured Hero Moment
            SignalPost.FeaturedMoment(
                title = "SHADOW ARCHITECT",
                description = "When geometry meets darkness. Featured submission by @ghost_in_fabric",
                imageRes = R.drawable.news_s1,
                timestamp = "2H AGO"
            ),

            // 2. Breaking Signal
            SignalPost.BreakingSignal(
                message = "NEW SIGNAL DETECTED // 247 OUTFIT SUBMISSIONS THIS WEEK // TRENDING: MONOCHROME LAYERS // COMMUNITY VOTE: BEST WINTER FIT //"
            ),

            // 3. User Outfit Post
            SignalPost.OutfitPost(
                username = "NEON_WALKER",
                location = "SHIBUYA // TOKYO",
                timestamp = "4H AGO",
                description = "Rainy night patrol fit. Y-3 jacket + vintage cargo pants. The reflections make everything feel cinematic.",
                imageRes= R.drawable.news_s2,
                likes = 1847,
                tags = listOf("TECHWEAR", "NIGHT", "URBAN")
            ),

            // 4. Style Guide Article
            SignalPost.StyleGuide(
                title = "CYBERPUNK STARTER KIT",
                subtitle = "5 Essential Pieces for the Modern Urban Warrior",
                author = "SIGNAL_EDITOR",
                readTime = "8 MIN READ",
                coverImageRes = R.drawable.news_s3,
                content = listOf(
                    GuideSection(
                        heading = "01 // THE FOUNDATION",
                        text = "Start with a solid black base. Not just any black - we're talking about fabric that absorbs light, that creates negative space in the urban landscape. Your foundation is your armor.",
                        imageRes = null,
                    ),
                    GuideSection(
                        heading = "02 // LAYERING IS POWER",
                        text = "The cyberpunk aesthetic is built on complexity. Think: technical vest over hoodie over long-sleeve base. Each layer tells a story. Each layer serves a function - even if that function is pure style.",
                        imageRes = R.drawable.news_s4,
                    ),
                    GuideSection(
                        heading = "03 // ASYMMETRY BREAKS THE GRID",
                        text = "One glove. One strap loose. Zipper half-done. Perfect symmetry is corporate. Controlled chaos is rebellion. Let your outfit feel like it's evolved, not designed.",
                        imageRes = R.drawable.news_s5,
                    ),
                    GuideSection(
                        heading = "04 // TECH DETAILS",
                        text = "Straps. Buckles. Velcro. Modular pockets. These aren't decorations - they're statements. They say: I'm ready. For what? That's the mystery.",
                        imageRes = R.drawable.news_s6,
                    ),
                    GuideSection(
                        heading = "05 // FOOTWEAR = FOUNDATION",
                        text = "Chunky soles. High-tops. Combat boots that have seen things. Your shoes should look like they could kick through a door or run from the law. Choose accordingly.",
                        imageRes = null,
                    )
                )
            ),

            // 5. Manifesto Post
            SignalPost.Manifesto(
                title = "ON WEARING DARKNESS",
                body = "Black is not the absence of color. It's the presence of everything, compressed. When you wear black in the city, you become the negative space between neon signs. You become the shadow that the advertisements can't touch.\n\nThis is not fashion. This is camouflage for the soul in a world that demands visibility. Every zipper is a declaration. Every strap is a refusal.\n\nWe dress in layers because identity itself is layered. We dress in darkness because the future is unwritten and we refuse to be illuminated by someone else's vision.\n\nThe street is the runway. The city is the gallery. Your outfit is your manifesto.\n\nWear it like you mean it.",
                author = "VOID_PROPHET",
                date = "TRANSMISSION_826"
            ),

            // 6. Featured Moment
            SignalPost.FeaturedMoment(
                title = "MOTION BLUR AESTHETIC",
                description = "Captured between seconds. This is what urban speed looks like.",
                imageRes = R.drawable.news_s7,
                timestamp = "TODAY"
            ),

            // 7. Trend Carousel
            SignalPost.TrendCarousel(
                title = "THIS WEEK IN THE SIGNAL",
                description = "Top looks from the community",
                images = listOf(
                    TrendImage(R.drawable.news_s8, "Subway Nomad"),
                    TrendImage(R.drawable.news_s9, "Graffiti Soul"),
                    TrendImage(R.drawable.news_s10, "Industrial Poetry")
                )
            ),

            // 8. User Outfit Post
            SignalPost.OutfitPost(
                username = "CONCRETE_ANGEL",
                location = "UNDERGROUND // BERLIN",
                timestamp = "1D AGO",
                description = "Sometimes you find the perfect wall. Sometimes the wall finds you. Oversized Yohji coat, thrifted pants, DIY chains. Everything is a remix.",
                imageRes = R.drawable.news_s11,
                likes = 2341,
                tags = listOf("AVANTGARDE", "DIY", "MONOCHROME")
            ),

            // 9. Breaking Signal
            SignalPost.BreakingSignal(
                message = "COMMUNITY ALERT // STREET STYLE MEETUP: ROPPONGI SAT 8PM // BRING YOUR FIT // BRING YOUR CAMERA // #SIGNALCREW //"
            ),

            // 10. Featured Moment
            SignalPost.FeaturedMoment(
                title = "AFTER DARK",
                description = "The city reveals its true colors when the sun goes down.",
                imageRes = R.drawable.news_s12,
                timestamp = "LAST NIGHT"
            )
        )
    }

    // Bài post mới do user vừa tạo (nằm trên cùng)

    // Tính displayPosts NGOÀI LazyColumn (không được khai báo val bên trong LazyListScope)
    val displayPosts = if (filterByUser != null) {
        uiState.posts.filter { it.userId == filterByUser }
    } else {
        uiState.posts
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VoidBlack)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            // Header
            item(key = "header") {
                SignalHeaderUpdated(
                    onCreatePost = { navController.navigate("create_post") },
                    onFilterClick = { showFilterMenu = true },
                    currentFilter = filterByUser
                )
            }

            // Loading indicator
            if (uiState.isLoading && uiState.posts.isEmpty()) {
                item(key = "loading") {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = CyberAcid, modifier = Modifier.size(32.dp))
                    }
                }
            }

            // Firestore posts
            items(items = displayPosts, key = { it.id }) { post ->
                FirestorePostCard(
                    post           = post,
                    currentUserId  = currentUserId,
                    onLike         = { viewModel.toggleLike(post.id) },
                    onCommentClick = {
                        selectedPostForComments = post
                        viewModel.loadComments(post.id)
                    },
                    onViewComments = {
                        selectedPostForComments = post
                        viewModel.loadComments(post.id)
                    },
                    onDelete = { deleteTargetPost = post }
                )
                Spacer(modifier = Modifier.height(32.dp))
            }

            // Optimistic posts
            items(items = userCreatedPosts, key = { it.id }) { post ->
                UserCreatedPostCard(post = post)
                Spacer(modifier = Modifier.height(32.dp))
            }

            // Mock community data — chỉ hiện khi chưa có Firestore posts
            if (uiState.posts.isEmpty() && !uiState.isLoading) {
                itemsIndexed(items = signalFeed, key = { index, _ -> "mock_$index" }) { index, post ->
                    when (post) {
                        is SignalPost.FeaturedMoment -> FeaturedMomentCard(post)
                        is SignalPost.OutfitPost     -> OutfitPostCard(post)
                        is SignalPost.StyleGuide     -> StyleGuideCard(post)
                        is SignalPost.Manifesto      -> ManifestoCard(post)
                        is SignalPost.TrendCarousel  -> TrendCarouselCard(post)
                        is SignalPost.BreakingSignal -> BreakingSignalTicker(post)
                    }
                    if (index < signalFeed.size - 1) Spacer(modifier = Modifier.height(32.dp))
                }
            } else if (uiState.posts.isNotEmpty()) {
                item(key = "community_header") {
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .background(Color.White.copy(alpha = 0.04f))
                            .border(1.dp, Color.White.copy(alpha = 0.08f))
                            .padding(12.dp)
                    ) {
                        Text(
                            "// COMMUNITY FEED //",
                            fontFamily = AppFonts.spaceMono,
                            fontSize = 9.sp,
                            color = CyberAcid.copy(alpha = 0.6f),
                            letterSpacing = 2.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
                itemsIndexed(items = signalFeed, key = { index, _ -> "mock2_$index" }) { index, post ->
                    when (post) {
                        is SignalPost.FeaturedMoment -> FeaturedMomentCard(post)
                        is SignalPost.OutfitPost     -> OutfitPostCard(post)
                        is SignalPost.StyleGuide     -> StyleGuideCard(post)
                        is SignalPost.Manifesto      -> ManifestoCard(post)
                        is SignalPost.TrendCarousel  -> TrendCarouselCard(post)
                        is SignalPost.BreakingSignal -> BreakingSignalTicker(post)
                    }
                    if (index < signalFeed.size - 1) Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }

        // FAB
        FloatingActionButton(
            onClick = { navController.navigate("create_post") },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 100.dp, end = 20.dp),
            containerColor = CyberAcid,
            contentColor = Color.Black
        ) {
            Icon(Icons.Default.Add, contentDescription = "Create Post", modifier = Modifier.size(24.dp))
        }

        // ── Delete dialog ─────────────────────────────────────────────
        deleteTargetPost?.let { post ->
            DeleteConfirmDialog(
                post      = post,
                onDismiss = { deleteTargetPost = null },
                onConfirm = {
                    viewModel.deletePost(post.id)
                    deleteTargetPost = null
                }
            )
        }

        // ── Comment Bottom Sheet ──────────────────────────────────────
        selectedPostForComments?.let { post ->
            CommentBottomSheet(
                postId = post.id,
                comments = uiState.currentPostComments,
                currentUserId = currentUserId,
                onAddComment = { text -> viewModel.addComment(post.id, text) },
                onDeleteComment = { commentId -> viewModel.deleteComment(post.id, commentId) },
                onDismiss = {
                    selectedPostForComments = null
                    viewModel.clearComments()
                }
            )
        }

        // ── Filter dialog ─────────────────────────────────────────────
        if (showFilterMenu) {
            FilterMenuDialog(
                currentFilter = filterByUser,
                allPosts = uiState.posts,
                onFilterByUser = { userId ->
                    filterByUser = userId
                    showFilterMenu = false
                },
                onClearFilter = {
                    filterByUser = null
                    showFilterMenu = false
                },
                onDismiss = { showFilterMenu = false }
            )
        }
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// HEADER COMPONENT
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
@Composable
fun SignalHeaderUpdated(
    onCreatePost: () -> Unit,
    onFilterClick: () -> Unit,
    currentFilter: String?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "THE SIGNAL",
                    fontFamily = AppFonts.oswald,
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Text(
                    text = if (currentFilter != null) "FILTERED BY USER" else "COMMUNITY // STYLE // SIGNAL",
                    fontFamily = AppFonts.spaceMono,
                    color = if (currentFilter != null) Color(0xFF00D9FF) else CyberAcid,
                    fontSize = 9.sp,
                    letterSpacing = 2.sp
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ✅ UPDATED: Filter button với visual feedback
                IconButton(
                    onClick = onFilterClick,
                    modifier = Modifier
                        .size(40.dp)
                        .border(
                            width = 1.dp,
                            color = if (currentFilter != null) Color(0xFF00D9FF) else Color.White.copy(alpha = 0.15f),
                            shape = CircleShape
                        )
                        .background(
                            color = if (currentFilter != null) Color(0xFF00D9FF).copy(alpha = 0.1f) else Color.Transparent,
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Filter",
                        tint = if (currentFilter != null) Color(0xFF00D9FF) else CyberAcid,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // ✅ UPDATED: Create post button (chuyển từ FAB)
                IconButton(
                    onClick = onCreatePost,
                    modifier = Modifier
                        .size(40.dp)
                        .background(CyberAcid, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Đăng bài",
                        tint = Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Status bar - GIỮ NGUYÊN phần còn lại của SignalHeader
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.03f))
                .border(1.dp, Color.White.copy(alpha = 0.08f))
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatusItem("ONLINE", "1,247", CyberAcid)
            StatusItem("THIS WEEK", "89 POSTS", CyberAcid)
            StatusItem("TRENDING", "#MONO", CyberAcid)
        }
    }
}

@Composable
fun StatusItem(label: String, value: String, CyberAcid1: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontFamily = AppFonts.spaceMono,
            fontSize = 8.sp,
            color = Color.White.copy(alpha = 0.4f),
            letterSpacing = 1.sp
        )
        Text(
            text = value,
            fontFamily = AppFonts.oswald,
            fontSize = 14.sp,
            color = CyberAcid,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun VerticalDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(30.dp)
            .background(Color.White.copy(alpha = 0.1f))
    )
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// 1. FEATURED MOMENT CARD
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
@Composable
fun FeaturedMomentCard(post: SignalPost.FeaturedMoment) {


    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }

    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(600),
        label = "fade"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(480.dp)
            .padding(horizontal = 20.dp)
            .alpha(alpha)
            .clip(RoundedCornerShape(2.dp))
    ) {
        Image(
            painter = painterResource(id = post.imageRes),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )


        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color.Transparent,
                        0.6f to Color.Black.copy(alpha = 0.3f),
                        1f to Color.Black.copy(alpha = 0.95f)
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .background(CyberAcid)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "FEATURED",
                        fontFamily = AppFonts.spaceMono,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        letterSpacing = 1.sp
                    )
                }

                Text(
                    text = post.timestamp,
                    fontFamily = AppFonts.spaceMono,
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }

            Column {
                Text(
                    text = post.title,
                    fontFamily = AppFonts.oswald,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    lineHeight = 40.sp,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = post.description,
                    fontFamily = AppFonts.spaceMono,
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    lineHeight = 18.sp
                )
            }
        }
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// 2. OUTFIT POST CARD
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
@Composable
fun OutfitPostCard(post: SignalPost.OutfitPost) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        // User header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(CyberAcid, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Text(
                        text = post.username,
                        fontFamily = AppFonts.oswald,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "${post.location} • ${post.timestamp}",
                        fontFamily = AppFonts.spaceMono,
                        fontSize = 9.sp,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Image
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .clip(RoundedCornerShape(2.dp))
        ) {
            Image(
                painter = painterResource(id = post.imageRes),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

        }

        Spacer(modifier = Modifier.height(16.dp))

        // Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                ActionButton(Icons.Default.FavoriteBorder, post.likes.toString())
                ActionButton(Icons.Default.ChatBubbleOutline, "47")
                ActionButton(Icons.Default.Share, null)
            }

            Icon(
                imageVector = Icons.Default.BookmarkBorder,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Description
        Text(
            text = post.description,
            fontFamily = AppFonts.spaceMono,
            fontSize = 13.sp,
            color = Color.White.copy(alpha = 0.9f),
            lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Tags
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.horizontalScroll(rememberScrollState())
        ) {
            post.tags.forEach { tag ->
                Box(
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.08f))
                        .border(1.dp, CyberAcid.copy(alpha = 0.3f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "#$tag",
                        fontFamily = AppFonts.spaceMono,
                        fontSize = 10.sp,
                        color = CyberAcid,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun ActionButton(icon: androidx.compose.ui.graphics.vector.ImageVector, count: String?) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.7f),
            modifier = Modifier.size(22.dp)
        )
        if (count != null) {
            Text(
                text = count,
                fontFamily = AppFonts.spaceMono,
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// 3. STYLE GUIDE CARD
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
@Composable
fun StyleGuideCard(post: SignalPost.StyleGuide) {
    var isExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .background(Color.White.copy(alpha = 0.02f))
            .border(1.dp, Color.White.copy(alpha = 0.1f))
            .clickable { isExpanded = !isExpanded }
    ) {
        // Header
        if (post.coverImageRes != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
            ) {
                Image(
                    painter = painterResource(id = post.coverImageRes), // Dùng R.drawable
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                0.5f to Color.Transparent,
                                1f to Color.Black.copy(alpha = 0.8f)
                            )
                        )
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                        .background(CyberAcid)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "GUIDE",
                        fontFamily = AppFonts.spaceMono,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }
        }

        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = post.title,
                fontFamily = AppFonts.oswald,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                lineHeight = 30.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = post.subtitle,
                fontFamily = AppFonts.spaceMono,
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.7f),
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "BY ${post.author}",
                    fontFamily = AppFonts.spaceMono,
                    fontSize = 10.sp,
                    color = CyberAcid,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = post.readTime,
                    fontFamily = AppFonts.spaceMono,
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(24.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color.White.copy(alpha = 0.1f))
                )

                Spacer(modifier = Modifier.height(24.dp))

                post.content.forEach { section ->
                    Text(
                        text = section.heading,
                        fontFamily = AppFonts.oswald,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberAcid,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = section.text,
                        fontFamily = AppFonts.spaceMono,
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.85f),
                        lineHeight = 19.sp
                    )

                    if (section.imageRes != null) {
                        Spacer(modifier = Modifier.height(16.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(2.dp))
                        ) {
                            Image(
                                painter = painterResource(id = section.imageRes), // Load từ R.drawable
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = CyberAcid,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isExpanded) "COLLAPSE" else "READ FULL GUIDE",
                    fontFamily = AppFonts.spaceMono,
                    fontSize = 10.sp,
                    color = CyberAcid,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// 4. MANIFESTO CARD (Pure Text)
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
@Composable
fun ManifestoCard(post: SignalPost.Manifesto) {
    var isExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .background(CyberAcid)
            .clickable { isExpanded = !isExpanded }
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = "MANIFESTO",
                fontFamily = AppFonts.spaceMono,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black.copy(alpha = 0.6f),
                letterSpacing = 2.sp
            )

            Text(
                text = post.date,
                fontFamily = AppFonts.spaceMono,
                fontSize = 9.sp,
                color = Color.Black.copy(alpha = 0.5f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = post.title,
            fontFamily = AppFonts.oswald,
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            color = Color.Black,
            lineHeight = 34.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (isExpanded) post.body else post.body.take(180) + "...",
            fontFamily = AppFonts.spaceMono,
            fontSize = 13.sp,
            color = Color.Black.copy(alpha = 0.85f),
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "— ${post.author}",
                fontFamily = AppFonts.spaceMono,
                fontSize = 11.sp,
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Italic
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = if (isExpanded) "LESS" else "MORE",
                    fontFamily = AppFonts.spaceMono,
                    fontSize = 10.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// 5. TREND CAROUSEL CARD
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
@Composable
fun TrendCarouselCard(post: SignalPost.TrendCarousel) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = post.title,
                        fontFamily = AppFonts.oswald,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = post.description,
                        fontFamily = AppFonts.spaceMono,
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }

                Text(
                    text = "(${post.images.size})",
                    fontFamily = AppFonts.spaceMono,
                    fontSize = 14.sp,
                    color = CyberAcid,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            itemsIndexed(post.images) { index, trendImage ->
                Box(
                    modifier = Modifier
                        .width(180.dp)
                        .height(240.dp)
                ) {
                    // ✅ Chỉ cần Image với painterResource
                    Image(
                        painter = painterResource(id = trendImage.imageRes),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(2.dp))
                    )

                    // Gradient overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    0.6f to Color.Transparent,
                                    1f to Color.Black.copy(alpha = 0.8f)
                                )
                            )
                    )

                    // Caption
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(CyberAcid)
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "${index + 1}",
                                fontFamily = AppFonts.oswald,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = trendImage.caption,
                            fontFamily = AppFonts.oswald,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// 6. BREAKING SIGNAL TICKER
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
@Composable
fun BreakingSignalTicker(post: SignalPost.BreakingSignal) {
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        delay(500)

        scope.launch {
            while (true) {
                val maxScroll = scrollState.maxValue
                val distance = maxScroll - scrollState.value

                if (distance > 0) {
                    val speedPixelsPerSecond = 25f
                    val durationMillis = ((distance / speedPixelsPerSecond) * 1000).toInt()

                    scrollState.animateScrollTo(
                        value = maxScroll,
                        animationSpec = tween(
                            durationMillis = durationMillis,
                            easing = LinearEasing
                        )
                    )

                    scrollState.scrollTo(0)
                } else {
                    break
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(Color.White.copy(alpha = 0.05f))
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    0f to CyberAcid.copy(alpha = 0.3f),
                    0.5f to CyberAcid.copy(alpha = 0.1f),
                    1f to CyberAcid.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier
                .horizontalScroll(scrollState, enabled = false),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(100) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(CyberAcid, CircleShape)
                    )

                    Text(
                        text = post.message,
                        fontFamily = AppFonts.spaceMono,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberAcid,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.width(40.dp))
                }
            }
        }
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// USER CREATED POST CARD — hiển thị bài mới đăng của chính user
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
@Composable
fun UserCreatedPostCard(post: UserFeedPost) {
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(500),
        label = "fade_in"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .alpha(alpha)
    ) {
        // ─── Header ───
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(CyberAcid, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = post.username,
                        fontFamily = AppFonts.oswald,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = if (post.location.isNotBlank())
                            "${post.location} • ${post.timestamp}"
                        else
                            post.timestamp,
                        fontFamily = AppFonts.spaceMono,
                        fontSize = 9.sp,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
            }

            // Badge "NEW"
            Box(
                modifier = Modifier
                    .background(CyberAcid)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "NEW",
                    fontFamily = AppFonts.spaceMono,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    letterSpacing = 1.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ─── Image (nếu có) ───
        if (post.imageUris.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .clip(RoundedCornerShape(2.dp))
            ) {
                AsyncImage(
                    model = post.imageUris.first(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // ─── Actions ───
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                ActionButton(Icons.Default.FavoriteBorder, "0")
                ActionButton(Icons.Default.ChatBubbleOutline, "0")
                ActionButton(Icons.Default.Share, null)
            }
            Icon(
                imageVector = Icons.Default.BookmarkBorder,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ─── Description ───
        if (post.description.isNotBlank()) {
            Text(
                text = post.description,
                fontFamily = AppFonts.spaceMono,
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.9f),
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // ─── Tags ───
        if (post.tags.isNotEmpty()) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.horizontalScroll(rememberScrollState())
            ) {
                post.tags.forEach { tag ->
                    Box(
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.08f))
                            .border(1.dp, CyberAcid.copy(alpha = 0.3f))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "#$tag",
                            fontFamily = AppFonts.spaceMono,
                            fontSize = 10.sp,
                            color = CyberAcid,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// FIRESTORE POST CARD — render domain SignalPost từ Firestore
// Like/Comment hoạt động thật sự, bài không mất khi restart app
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
@Composable
fun FirestorePostCard(
    post: DomainPost,
    currentUserId: String,
    onLike: () -> Unit,
    onCommentClick: () -> Unit,
    onViewComments: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    val isLiked = remember(post.interactions.likes.userIds, currentUserId) {
        post.isLikedBy(currentUserId)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        // ─── Header ───
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Avatar chữ cái
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(CyberAcid, androidx.compose.foundation.shape.CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = post.username.take(2).ifBlank { "??" }.uppercase(),
                        fontFamily = AppFonts.spaceMono,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.Black
                    )
                }
                Column {
                    Text(
                        text = post.username.ifBlank { "AGENT" }.uppercase(),
                        fontFamily = AppFonts.oswald,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = buildString {
                            post.content.location?.let { append("$it • ") }
                            append(post.getFormattedTimestamp())
                        },
                        fontFamily = AppFonts.spaceMono,
                        fontSize = 9.sp,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
            }
            // ─── Menu 3 chấm (View Comments / Delete) ───
            Box {
                var menuExpanded by remember { mutableStateOf(false) }
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More",
                        tint = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    modifier = Modifier.background(Color(0xFF1A1A1A))
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                "XEM BÌNH LUẬN",
                                fontFamily = AppFonts.spaceMono,
                                fontSize = 11.sp,
                                color = Color.White
                            )
                        },
                        leadingIcon = {
                            Icon(Icons.Default.ChatBubbleOutline, null, tint = CyberAcid, modifier = Modifier.size(16.dp))
                        },
                        onClick = { menuExpanded = false; onViewComments() }
                    )
                    if (post.userId == currentUserId) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "XÓA BÀI",
                                    fontFamily = AppFonts.spaceMono,
                                    fontSize = 11.sp,
                                    color = Color(0xFFFF4444)
                                )
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Delete, null, tint = Color(0xFFFF4444), modifier = Modifier.size(16.dp))
                            },
                            onClick = { menuExpanded = false; onDelete() }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ─── Ảnh (nếu có) ───
        if (post.content.images.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(2.dp))
            ) {
                AsyncImage(
                    model = post.content.images.first(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // ─── Title (nếu có) ───
        post.content.title?.takeIf { it.isNotBlank() }?.let { title ->
            Text(
                text = title.uppercase(),
                fontFamily = AppFonts.oswald,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // ─── Actions (Like / Comment thật sự) ───
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                // LIKE — bấm được, đổi màu khi đã like
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.clickable { onLike() }
                ) {
                    Icon(
                        imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (isLiked) CyberAcid else Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = post.getFormattedLikes(),
                        fontFamily = AppFonts.spaceMono,
                        fontSize = 12.sp,
                        color = if (isLiked) CyberAcid else Color.White.copy(alpha = 0.7f)
                    )
                }

                // COMMENT — mở dialog
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.clickable { onCommentClick() }
                ) {
                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = "Comment",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = post.getFormattedComments(),
                        fontFamily = AppFonts.spaceMono,
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }

                // SHARE
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share",
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(22.dp)
                )
            }

            Icon(
                imageVector = Icons.Default.BookmarkBorder,
                contentDescription = "Save",
                tint = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ─── Description ───
        if (post.content.description.isNotBlank()) {
            Text(
                text = post.content.description,
                fontFamily = AppFonts.spaceMono,
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.9f),
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // ─── Tags ───
        if (post.content.tags.isNotEmpty()) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.horizontalScroll(rememberScrollState())
            ) {
                post.content.tags.forEach { tag ->
                    Box(
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.08f))
                            .border(1.dp, CyberAcid.copy(alpha = 0.3f))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "#${tag.uppercase()}",
                            fontFamily = AppFonts.spaceMono,
                            fontSize = 10.sp,
                            color = CyberAcid,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// COMMENT DIALOG — gửi comment thật đến Firestore
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
@Composable
fun CommentDialog(
    post: DomainPost,
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF111111))
                .border(1.dp, CyberAcid.copy(alpha = 0.3f))
                .padding(20.dp)
        ) {
            Text(
                text = "// ADD COMMENT",
                fontFamily = AppFonts.spaceMono,
                fontSize = 11.sp,
                color = CyberAcid,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "ON: ${post.content.title?.uppercase() ?: post.content.description.take(40).uppercase()}...",
                fontFamily = AppFonts.spaceMono,
                fontSize = 9.sp,
                color = Color.White.copy(alpha = 0.4f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.fillMaxWidth().height(100.dp),
                placeholder = {
                    Text(
                        "TYPE YOUR TRANSMISSION...",
                        fontFamily = AppFonts.spaceMono,
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.2f)
                    )
                },
                textStyle = LocalTextStyle.current.copy(
                    fontFamily = AppFonts.spaceMono,
                    fontSize = 13.sp,
                    color = Color.White
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyberAcid,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                    cursorColor = CyberAcid
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                ) {
                    Text("ABORT", fontFamily = AppFonts.spaceMono, fontSize = 11.sp, color = Color.White.copy(alpha = 0.5f))
                }
                Button(
                    onClick = {
                        if (text.isNotBlank()) {
                            onSubmit(text.trim())
                            onDismiss()
                        }
                    },
                    enabled = text.isNotBlank(),
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = CyberAcid)
                ) {
                    Text("TRANSMIT", fontFamily = AppFonts.spaceMono, fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// VIEW COMMENTS DIALOG — xem danh sách comment thực từ Firestore
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
@Composable
fun ViewCommentsDialog(
    post: DomainPost,
    comments: List<SignalComment>,
    currentUserId: String,
    onDismiss: () -> Unit,
    onDeleteComment: (String) -> Unit,
    onAddComment: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF111111))
                .border(1.dp, CyberAcid.copy(alpha = 0.3f))
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0D0D0D))
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "// BÌNH LUẬN",
                        fontFamily = AppFonts.spaceMono,
                        fontSize = 11.sp,
                        color = CyberAcid,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "${comments.size} TRANSMISSIONS",
                        fontFamily = AppFonts.spaceMono,
                        fontSize = 9.sp,
                        color = Color.White.copy(alpha = 0.4f)
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, null, tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
                }
            }

            // Danh sách comment
            if (comments.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "CHƯA CÓ BÌNH LUẬN NÀO\n// BE THE FIRST TO TRANSMIT //",
                        fontFamily = AppFonts.spaceMono,
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.3f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 8.dp)
                ) {
                    comments.forEach { comment ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .background(CyberAcid.copy(alpha = 0.2f), CircleShape)
                                        .border(1.dp, CyberAcid.copy(alpha = 0.4f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = comment.userId.take(2).uppercase(),
                                        fontFamily = AppFonts.spaceMono,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        color = CyberAcid
                                    )
                                }
                                Column {
                                    Text(
                                        text = comment.userId.take(8).uppercase() + "...",
                                        fontFamily = AppFonts.spaceMono,
                                        fontSize = 9.sp,
                                        color = CyberAcid,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = comment.text,
                                        fontFamily = AppFonts.spaceMono,
                                        fontSize = 12.sp,
                                        color = Color.White.copy(alpha = 0.85f),
                                        lineHeight = 17.sp
                                    )
                                }
                            }
                            // Nút xóa (chỉ hiện khi là comment của mình)
                            if (comment.userId == currentUserId) {
                                IconButton(
                                    onClick = { onDeleteComment(comment.id) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        null,
                                        tint = Color(0xFFFF4444).copy(alpha = 0.7f),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .height(1.dp)
                                .background(Color.White.copy(alpha = 0.05f))
                        )
                    }
                }
            }

            // Footer — nút thêm comment
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0D0D0D))
                    .padding(16.dp)
            ) {
                Button(
                    onClick = onAddComment,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = CyberAcid)
                ) {
                    Icon(Icons.Default.Add, null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "+ TRANSMIT",
                        fontFamily = AppFonts.spaceMono,
                        fontSize = 11.sp,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// DELETE CONFIRM DIALOG — xác nhận xóa bài viết
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
@Composable
fun DeleteConfirmDialog(
    post: DomainPost,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF111111))
                .border(1.dp, Color(0xFFFF4444).copy(alpha = 0.5f))
                .padding(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFFFF4444).copy(alpha = 0.15f), CircleShape)
                    .border(1.dp, Color(0xFFFF4444).copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Delete, null, tint = Color(0xFFFF4444), modifier = Modifier.size(24.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "// XÓA BÀI VIẾT",
                fontFamily = AppFonts.spaceMono,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFF4444),
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Hành động này không thể hoàn tác. Bài viết sẽ bị xóa khỏi Firestore vĩnh viễn.",
                fontFamily = AppFonts.spaceMono,
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.6f),
                lineHeight = 16.sp
            )

            post.content.description.takeIf { it.isNotBlank() }?.let { desc ->
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.03f))
                        .border(1.dp, Color.White.copy(alpha = 0.08f))
                        .padding(10.dp)
                ) {
                    Text(
                        text = "\"${desc.take(80)}...\"",
                        fontFamily = AppFonts.spaceMono,
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.5f),
                        fontStyle = FontStyle.Italic
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                ) {
                    Text("HỦY", fontFamily = AppFonts.spaceMono, fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f))
                }
                Button(
                    onClick = onConfirm,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4444))
                ) {
                    Text("XÓA", fontFamily = AppFonts.spaceMono, fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// FILTER MENU DIALOG
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
@Composable
fun FilterMenuDialog(
    currentFilter: String?,
    allPosts: List<DomainPost>,
    onFilterByUser: (String) -> Unit,
    onClearFilter: () -> Unit,
    onDismiss: () -> Unit
) {
    // Lấy danh sách unique users
    val users = remember(allPosts) {
        allPosts.distinctBy { it.userId }
            .map { it.userId to it.username }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PhantomGrey,
        title = {
            Text(
                text = "LỌC BÀI ĐĂNG",
                fontFamily = AppFonts.oswald,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TeslaWhite
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Lọc theo người dùng:",
                    fontFamily = AppFonts.spaceMono,
                    fontSize = 12.sp,
                    color = TechSilver
                )

                if (users.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PersonOff,
                                contentDescription = null,
                                tint = TechSilver.copy(alpha = 0.3f),
                                modifier = Modifier.size(32.dp)
                            )
                            Text(
                                text = "Chưa có bài đăng nào",
                                fontFamily = AppFonts.spaceMono,
                                fontSize = 12.sp,
                                color = TechSilver.copy(alpha = 0.5f)
                            )
                        }
                    }
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        users.forEach { (userId, username) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onFilterByUser(userId) }
                                    .background(
                                        color = if (currentFilter == userId)
                                            Color(0xFF00D9FF).copy(alpha = 0.1f)
                                        else
                                            Color.Transparent,
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (currentFilter == userId)
                                            Color(0xFF00D9FF)
                                        else
                                            Color.White.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = if (currentFilter == userId)
                                        Color(0xFF00D9FF)
                                    else
                                        TechSilver,
                                    modifier = Modifier.size(18.dp)
                                )

                                Text(
                                    text = username,
                                    fontFamily = AppFonts.spaceMono,
                                    fontSize = 13.sp,
                                    color = if (currentFilter == userId)
                                        Color(0xFF00D9FF)
                                    else
                                        TeslaWhite,
                                    modifier = Modifier.weight(1f)
                                )

                                if (currentFilter == userId) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color(0xFF00D9FF),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (currentFilter != null) {
                TextButton(onClick = onClearFilter) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = null,
                            tint = GlitchRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "XÓA LỌC",
                            fontFamily = AppFonts.spaceMono,
                            fontSize = 12.sp,
                            color = GlitchRed
                        )
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "ĐÓNG",
                    fontFamily = AppFonts.spaceMono,
                    fontSize = 12.sp,
                    color = TechSilver
                )
            }
        }
    )
}
