package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsScore
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.ClubEntity
import com.example.data.local.EventEntity
import com.example.ui.components.CategoryBadge
import com.example.ui.components.EventCard
import com.example.ui.components.OdBadge
import com.example.ui.components.VerificationBadge
import com.example.ui.components.launchAddToCalendar
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkCardSurface
import com.example.ui.theme.DarkSurfaceTranslucent
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.PurpleCultural
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.SoftEmerald
import com.example.ui.theme.VividCrimson
import com.example.ui.theme.WarmAmber
import com.example.ui.viewmodel.CollegeEventsViewModel
import com.example.ui.viewmodel.NavigationTab
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: CollegeEventsViewModel,
    onOpenNotifications: () -> Unit,
    onOpenRoleSwitcher: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val allEvents by viewModel.allEvents.collectAsState()
    val allClubs by viewModel.allClubs.collectAsState()
    val unreadCount by viewModel.unreadNotifications.collectAsState()
    val filterState by viewModel.filterState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    val publishedEvents by viewModel.publishedEvents.collectAsState()
    // Select top 4 high-impact events for the Hotstar Hero Carousel
    val heroEvents = publishedEvents.filter { it.posterUrl.isNotEmpty() }.ifEmpty { publishedEvents.take(3) }
    val todayEvents by viewModel.todayEvents.collectAsState()
    val tomorrowEvents by viewModel.tomorrowEvents.collectAsState()
    val trendingEvents by viewModel.trendingEvents.collectAsState()
    val startingSoonEvents by viewModel.startingSoonEvents.collectAsState()
    val followedClubsEvents by viewModel.followedClubsEvents.collectAsState()
    val recommendedEvents by viewModel.recommendedEvents.collectAsState()
    val savedEvents by viewModel.savedEvents.collectAsState()
    val cachedEventEntities by viewModel.cachedEventEntities.collectAsState()
    val isFirestoreSyncing by viewModel.isFirestoreSyncing.collectAsState()
    val lastSyncTimestamp by viewModel.lastSyncTimestamp.collectAsState()

    // Prioritize events from the Room cached_events table
    val liveCachedEvents = if (cachedEventEntities.isNotEmpty()) cachedEventEntities else publishedEvents

    val technicalEvents = publishedEvents.filter { it.category.contains("Technical", ignoreCase = true) }
    val culturalEvents = publishedEvents.filter { it.category.contains("Cultural", ignoreCase = true) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("home_screen"),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        // 1. Clean Apple-style Top Header: App Name + Notifications only
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.98f))
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                // Top Row: App Name on Left, Notifications on Right
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // App Name (Large, Bold, Clean Apple-quality typography)
                    Text(
                        text = "Campus Events",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Notifications Icon with Unread Badge
                    IconButton(
                        onClick = onOpenNotifications,
                        modifier = Modifier
                            .size(42.dp)
                            .testTag("notification_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (unreadCount > 0) {
                                    Badge(containerColor = VividCrimson) {
                                        Text("$unreadCount", color = Color.White)
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Discover what's happening on campus.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Omnibox Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        viewModel.setSearchQuery(it)
                        if (it.isNotBlank()) {
                            viewModel.setTab(NavigationTab.EVENTS)
                        }
                    },
                    placeholder = {
                        Text(
                            "Search events, clubs, venues, OD...",
                            color = Slate400,
                            fontSize = 13.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    tint = Slate400,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("home_search_bar")
                )
            }
        }

        // 2. Horizontal Category Swiper / Filter Strip
        item {
            Column(modifier = Modifier.padding(vertical = 10.dp)) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = filterState.discoveryMode == "ALL" && filterState.selectedCategories.isEmpty() && !filterState.odOnly,
                            onClick = {
                                viewModel.clearFilters()
                                viewModel.setTab(NavigationTab.EVENTS)
                            },
                            label = { Text("All") },
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                    item {
                        FilterChip(
                            selected = filterState.discoveryMode == "TRENDING",
                            onClick = {
                                viewModel.clearFilters()
                                viewModel.setDiscoveryMode("TRENDING")
                                viewModel.setTab(NavigationTab.EVENTS)
                            },
                            label = { Text("Trending") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("chip_trending")
                        )
                    }
                    item {
                        FilterChip(
                            selected = filterState.odOnly,
                            onClick = {
                                viewModel.clearFilters()
                                viewModel.toggleOdFilter()
                                viewModel.setTab(NavigationTab.EVENTS)
                            },
                            label = { Text("OD Available") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("chip_od")
                        )
                    }
                    item {
                        FilterChip(
                            selected = filterState.selectedCategories.contains("Technical"),
                            onClick = {
                                viewModel.clearFilters()
                                viewModel.toggleCategoryFilter("Technical")
                                viewModel.setTab(NavigationTab.EVENTS)
                            },
                            label = { Text("Technical") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("chip_technical")
                        )
                    }
                    item {
                        FilterChip(
                            selected = filterState.selectedCategories.contains("Cultural"),
                            onClick = {
                                viewModel.clearFilters()
                                viewModel.toggleCategoryFilter("Cultural")
                                viewModel.setTab(NavigationTab.EVENTS)
                            },
                            label = { Text("Cultural") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("chip_cultural")
                        )
                    }
                    item {
                        FilterChip(
                            selected = filterState.discoveryMode == "STARTING_SOON",
                            onClick = {
                                viewModel.clearFilters()
                                viewModel.setDiscoveryMode("STARTING_SOON")
                                viewModel.setTab(NavigationTab.EVENTS)
                            },
                            label = { Text("Starting Soon") },
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                    item {
                        FilterChip(
                            selected = filterState.selectedCategories.contains("Sports"),
                            onClick = {
                                viewModel.clearFilters()
                                viewModel.toggleCategoryFilter("Sports")
                                viewModel.setTab(NavigationTab.EVENTS)
                            },
                            label = { Text("Sports") },
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }
        }

        // 3. HOTSTAR-STYLE FEATURED HERO CAROUSEL
        if (heroEvents.isNotEmpty()) {
            item {
                Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
                    HotstarFeaturedHeroCarousel(
                        events = heroEvents,
                        onEventClick = { viewModel.selectEvent(it) },
                        onRegisterClick = { event ->
                            if (event.registrationUrl.isNotEmpty()) {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(event.registrationUrl))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    viewModel.selectEvent(event)
                                }
                            } else {
                                viewModel.selectEvent(event)
                            }
                        },
                        onAddToCalendar = { launchAddToCalendar(context, it) }
                    )
                }
            }
        }

        // 4. THARUNSPEAKS-INSPIRED BENTO GRID FOR CLUBS & DEPARTMENTS
        if (allClubs.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(14.dp))
                TharunBentoGrid(
                    clubs = allClubs,
                    onClubClick = { club ->
                        viewModel.selectClub(club)
                        viewModel.setTab(NavigationTab.CLUBS)
                    },
                    onFollowToggle = { club ->
                        viewModel.toggleFollowClub(club.clubId, club.isFollowed)
                    },
                    onSeeAllClubs = { viewModel.setTab(NavigationTab.CLUBS) }
                )
            }
        }

        // 5. HORIZONTAL CONTENT SWIPER: Recommended For You
        if (recommendedEvents.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(14.dp))
                SectionHeader(
                    title = "Recommended for You",
                    subtitle = "Events with OD attendance eligibility",
                    onSeeAll = {
                        viewModel.clearFilters()
                        viewModel.toggleOdFilter()
                        viewModel.setTab(NavigationTab.EVENTS)
                    }
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(recommendedEvents) { event ->
                        Box(modifier = Modifier.width(290.dp)) {
                            EventCard(
                                event = event,
                                onClick = { viewModel.selectEvent(event) },
                                onSaveToggle = { viewModel.toggleSaveEvent(event.eventId, event.isSaved) }
                            )
                        }
                    }
                }
            }
        }

        // 6. HORIZONTAL CONTENT SWIPER: Events From Followed Clubs
        if (followedClubsEvents.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(14.dp))
                SectionHeader(
                    title = "From Followed Clubs",
                    subtitle = "Updates from student societies you follow",
                    onSeeAll = { viewModel.setTab(NavigationTab.CLUBS) }
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(followedClubsEvents) { event ->
                        Box(modifier = Modifier.width(290.dp)) {
                            EventCard(
                                event = event,
                                onClick = { viewModel.selectEvent(event) },
                                onSaveToggle = { viewModel.toggleSaveEvent(event.eventId, event.isSaved) }
                            )
                        }
                    }
                }
            }
        }

        // 7. HORIZONTAL CONTENT SWIPER: Trending Technical Hackathons
        if (technicalEvents.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(14.dp))
                SectionHeader(
                    title = "Technical & Hackathons",
                    subtitle = "Code sprints, AI workshops, and engineering challenges",
                    onSeeAll = {
                        viewModel.clearFilters()
                        viewModel.toggleCategoryFilter("Technical")
                        viewModel.setTab(NavigationTab.EVENTS)
                    }
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(technicalEvents) { event ->
                        Box(modifier = Modifier.width(290.dp)) {
                            EventCard(
                                event = event,
                                onClick = { viewModel.selectEvent(event) },
                                onSaveToggle = { viewModel.toggleSaveEvent(event.eventId, event.isSaved) }
                            )
                        }
                    }
                }
            }
        }

        // 8. HORIZONTAL CONTENT SWIPER: Starting Soon
        if (startingSoonEvents.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(14.dp))
                SectionHeader(
                    title = "Starting Soon",
                    subtitle = "Events kicking off on campus over the next 72 hours",
                    onSeeAll = {
                        viewModel.clearFilters()
                        viewModel.setDiscoveryMode("STARTING_SOON")
                        viewModel.setTab(NavigationTab.EVENTS)
                    }
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(startingSoonEvents) { event ->
                        Box(modifier = Modifier.width(290.dp)) {
                            EventCard(
                                event = event,
                                onClick = { viewModel.selectEvent(event) },
                                onSaveToggle = { viewModel.toggleSaveEvent(event.eventId, event.isSaved) }
                            )
                        }
                    }
                }
            }
        }

        // 9. HORIZONTAL CONTENT SWIPER: Cultural & Arts Showcases
        if (culturalEvents.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(14.dp))
                SectionHeader(
                    title = "Cultural & Arts",
                    subtitle = "Concerts, open mics, dance battles, and photography",
                    onSeeAll = {
                        viewModel.clearFilters()
                        viewModel.toggleCategoryFilter("Cultural")
                        viewModel.setTab(NavigationTab.EVENTS)
                    }
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(culturalEvents) { event ->
                        Box(modifier = Modifier.width(290.dp)) {
                            EventCard(
                                event = event,
                                onClick = { viewModel.selectEvent(event) },
                                onSaveToggle = { viewModel.toggleSaveEvent(event.eventId, event.isSaved) }
                            )
                        }
                    }
                }
            }
        }

        // 10. Saved Events Quick Shelf (if user has bookmarked events)
        if (savedEvents.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(14.dp))
                SectionHeader(
                    title = "Saved Events",
                    subtitle = "${savedEvents.size} events bookmarked in your schedule",
                    onSeeAll = { viewModel.setTab(NavigationTab.SAVED) }
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(savedEvents) { event ->
                        Box(modifier = Modifier.width(290.dp)) {
                            EventCard(
                                event = event,
                                onClick = { viewModel.selectEvent(event) },
                                onSaveToggle = { viewModel.toggleSaveEvent(event.eventId, event.isSaved) }
                            )
                        }
                    }
                }
            }
        }

        // 11. PRIMARY VERTICAL LAZYCOLUMN FEED: Live Event Cards Fetched from Firestore & Cached in Room
        item {
            Spacer(modifier = Modifier.height(18.dp))
            SectionHeader(
                title = "Live Events Feed",
                subtitle = "Event cards fetched from Firestore and cached in local Room database schema",
                onSeeAll = { viewModel.setTab(NavigationTab.EVENTS) }
            )
        }

        // Firestore & Room Cache Synchronization Control Card
        item {
            FirestoreRoomCacheBanner(
                isSyncing = isFirestoreSyncing,
                eventCount = liveCachedEvents.size,
                onSync = { viewModel.syncWithFirestore() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
            )
        }

        // Vertical List of Event Cards using LazyColumn items()
        if (liveCachedEvents.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isFirestoreSyncing) "Syncing events from Firestore..." else "No events currently cached. Tap Sync above to refresh.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(liveCachedEvents, key = { "live_cached_${it.eventId}" }) { event ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 7.dp)
                ) {
                    EventCard(
                        event = event,
                        onClick = { viewModel.selectEvent(event) },
                        onSaveToggle = { viewModel.toggleSaveEvent(event.eventId, event.isSaved) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

/**
 * Banner showing synchronization status between Firebase Firestore and the Room cached events schema.
 */
@Composable
fun FirestoreRoomCacheBanner(
    isSyncing: Boolean,
    eventCount: Int,
    onSync: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.testTag("firestore_room_cache_banner"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = "Firestore & Room Sync",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Firestore & Room Cache",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF10B981).copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "SYNCED",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp
                                ),
                                color = Color(0xFF10B981)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isSyncing) "Syncing from remote Firestore..." else "$eventCount cached event cards available offline",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(
                onClick = onSync,
                enabled = !isSyncing,
                modifier = Modifier
                    .size(44.dp)
                    .testTag("sync_firestore_button")
            ) {
                if (isSyncing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Sync now",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

/**
 * HOTSTAR-STYLE FEATURED HERO CAROUSEL
 * Large auto-advancing hero banner with full-bleed dark gradient, high-res poster art,
 * live metadata overlay, and high-energy Hotstar-inspired CTAs.
 */
@Composable
fun HotstarFeaturedHeroCarousel(
    events: List<EventEntity>,
    onEventClick: (EventEntity) -> Unit,
    onRegisterClick: (EventEntity) -> Unit,
    onAddToCalendar: (EventEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    if (events.isEmpty()) return
    var currentIndex by remember { mutableIntStateOf(0) }

    // Auto-advancing effect
    LaunchedEffect(events.size) {
        while (isActive) {
            delay(5500L)
            if (events.isNotEmpty()) {
                currentIndex = (currentIndex + 1) % events.size
            }
        }
    }

    val currentEvent = events.getOrNull(currentIndex) ?: return

    Column(modifier = modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(310.dp)
                .clickable { onEventClick(currentEvent) }
                .testTag("hotstar_hero_card_${currentEvent.eventId}"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = DarkCardSurface),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Background Poster Art
                val posterRes = if (currentEvent.posterUrl.contains("cultural", ignoreCase = true)) {
                    R.drawable.poster_cultural
                } else {
                    R.drawable.poster_hackathon
                }

                Image(
                    painter = painterResource(id = posterRes),
                    contentDescription = currentEvent.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Dark multi-directional gradient for Hotstar depth
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF090D16).copy(alpha = 0.96f),
                                    Color(0xFF0B0F17).copy(alpha = 0.82f),
                                    Color(0xFF0B0F17).copy(alpha = 0.35f)
                                )
                            )
                        )
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color(0xFF0B0F17).copy(alpha = 0.6f),
                                    Color(0xFF090D16).copy(alpha = 0.98f)
                                ),
                                startY = 80f
                            )
                        )
                )

                // Overlay Content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Top: Badges & Live Hotstar Status Tag
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            CategoryBadge(category = currentEvent.category)
                            VerificationBadge(status = currentEvent.verificationStatus)
                        }

                        // Hotstar-style Live Priority Pill
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = VividCrimson.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, VividCrimson.copy(alpha = 0.6f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(VividCrimson, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "FEATURED",
                                    color = VividCrimson,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }

                    // Bottom info & CTAs
                    Column {
                        // Title
                        Text(
                            text = currentEvent.title,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 19.sp
                            ),
                            color = Color.White,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Metadata row: Date/Time + Venue
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Event,
                                    contentDescription = null,
                                    tint = NeonCyan,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${currentEvent.startDate} • ${currentEvent.startTime}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate300
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Event,
                                    contentDescription = null,
                                    tint = WarmAmber,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = currentEvent.venue,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate300,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // OD Badge
                        OdBadge(status = currentEvent.odStatus)

                        Spacer(modifier = Modifier.height(14.dp))

                        // Action Buttons: Register Now (Vivid Crimson/Coral) + Save to Calendar
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = { onRegisterClick(currentEvent) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = VividCrimson,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                modifier = Modifier.testTag("hero_register_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.OpenInNew,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Register Now",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }

                            Surface(
                                onClick = { onAddToCalendar(currentEvent) },
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White.copy(alpha = 0.12f),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
                                modifier = Modifier.testTag("hero_calendar_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarToday,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "+ Calendar",
                                        color = Color.White,
                                        fontWeight = FontWeight.SemiBold,
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Indicator dots
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            events.forEachIndexed { index, _ ->
                val isSelected = index == currentIndex
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .height(6.dp)
                        .width(if (isSelected) 22.dp else 6.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) NeonCyan else Color.White.copy(alpha = 0.25f)
                        )
                        .clickable { currentIndex = index }
                )
            }
        }
    }
}

/**
 * THARUNSPEAKS-INSPIRED BENTO GRID FOR CLUBS & DEPARTMENTS
 * Asymmetric bento grid showcasing featured student clubs and college departments
 * with glowing neon outlines, member counts, and interactive follow pills.
 */
@Composable
fun TharunBentoGrid(
    clubs: List<ClubEntity>,
    onClubClick: (ClubEntity) -> Unit,
    onFollowToggle: (ClubEntity) -> Unit,
    onSeeAllClubs: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Campus Clubs & Chapters",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Active student organizations and technical chapters",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            TextButton(onClick = onSeeAllClubs) {
                Text("See All", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Asymmetric Bento Grid: Top Featured Club Card (Wide) + 4 Compact Bento Tiles
        val heroClub = clubs.firstOrNull()
        val otherClubs = clubs.drop(1).take(4)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Large Hero Bento Card
            if (heroClub != null) {
                BentoHeroClubCard(
                    club = heroClub,
                    onClick = { onClubClick(heroClub) },
                    onFollowToggle = { onFollowToggle(heroClub) }
                )
            }

            // 2-Column Bento Grid for Remaining Clubs
            for (i in otherClubs.indices step 2) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val club1 = otherClubs[i]
                    val club2 = otherClubs.getOrNull(i + 1)

                    Box(modifier = Modifier.weight(1f)) {
                        BentoTileCard(
                            club = club1,
                            onClick = { onClubClick(club1) },
                            onFollowToggle = { onFollowToggle(club1) }
                        )
                    }

                    if (club2 != null) {
                        Box(modifier = Modifier.weight(1f)) {
                            BentoTileCard(
                                club = club2,
                                onClick = { onClubClick(club2) },
                                onFollowToggle = { onFollowToggle(club2) }
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun BentoHeroClubCard(
    club: ClubEntity,
    onClick: () -> Unit,
    onFollowToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("bento_hero_club_${club.clubId}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCardSurface),
        border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Glowing Icon Container
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = NeonCyan.copy(alpha = 0.16f),
                        border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.45f)),
                        modifier = Modifier.size(46.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Groups,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = club.name,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Official",
                                tint = SoftEmerald,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = club.department,
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate400,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Follow toggle pill
                Button(
                    onClick = onFollowToggle,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (club.isFollowed) Color.White.copy(alpha = 0.12f) else NeonCyan,
                        contentColor = if (club.isFollowed) Color.White else Color.Black
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("follow_hero_${club.clubId}")
                ) {
                    Text(
                        text = if (club.isFollowed) "Following" else "+ Follow",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = club.tagline,
                style = MaterialTheme.typography.bodyMedium,
                color = Slate300,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Metrics row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(SoftEmerald, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${club.memberCount} Active Members",
                        style = MaterialTheme.typography.labelSmall,
                        color = Slate300
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(WarmAmber, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Led by ${club.leadPresident}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Slate300
                    )
                }
            }
        }
    }
}

@Composable
fun BentoTileCard(
    club: ClubEntity,
    onClick: () -> Unit,
    onFollowToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("bento_tile_${club.clubId}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCardSurface),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.10f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color.White.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                    modifier = Modifier.size(34.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = when {
                                club.category.contains("Cultural", ignoreCase = true) -> Icons.Default.MusicNote
                                club.category.contains("Sports", ignoreCase = true) -> Icons.Default.SportsScore
                                else -> Icons.Default.Code
                            },
                            contentDescription = null,
                            tint = when {
                                club.category.contains("Cultural", ignoreCase = true) -> PurpleCultural
                                club.category.contains("Sports", ignoreCase = true) -> WarmAmber
                                else -> NeonCyan
                            },
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Surface(
                    onClick = onFollowToggle,
                    shape = RoundedCornerShape(8.dp),
                    color = if (club.isFollowed) Color.White.copy(alpha = 0.10f) else NeonCyan.copy(alpha = 0.20f),
                    border = BorderStroke(
                        1.dp,
                        if (club.isFollowed) Color.White.copy(alpha = 0.20f) else NeonCyan.copy(alpha = 0.50f)
                    )
                ) {
                    Text(
                        text = if (club.isFollowed) "✓" else "+ Follow",
                        color = if (club.isFollowed) Color.White else NeonCyan,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = club.name,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = club.tagline,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${club.memberCount} members",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = Slate500,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    subtitle: String? = null,
    onSeeAll: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        TextButton(onClick = onSeeAll) {
            Text("See All", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        }
    }
}
