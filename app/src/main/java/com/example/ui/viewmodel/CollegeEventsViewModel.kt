package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.CachedEventEntity
import com.example.data.local.ClubEntity
import com.example.data.local.ClubMemberEntity
import com.example.data.local.CollegeDatabase
import com.example.data.local.ConflictEntity
import com.example.data.local.EventApprovalDecisionEntity
import com.example.data.local.EventEntity
import com.example.data.local.EventHistoryEntity
import com.example.data.local.EventReportEntity
import com.example.data.local.NotificationEntity
import com.example.data.repository.CachedEventRepository
import com.example.data.repository.ClubRepository
import com.example.data.repository.EventRepository
import com.example.data.repository.FirestoreEventRepository
import com.example.data.repository.ReportRepository
import com.example.data.repository.UserSessionManager
import com.example.model.ClubAdminPermissions
import com.example.model.EventStatus
import com.example.model.OdStatus
import com.example.model.SourceType
import com.example.model.User
import com.example.model.UserRole
import com.example.model.VerificationStatus
import com.example.service.AiExtractionService
import com.example.service.AuthService
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

enum class NavigationTab {
    HOME,
    EVENTS,
    CLUBS,
    SAVED,
    SETTINGS,
    PROFILE
}

enum class PortalView {
    NONE,
    FACULTY_PORTAL,
    CLUB_PRESIDENT_PORTAL,
    COMMUNITY_PORTAL,
    ADMIN_DASHBOARD
}

data class SearchSuggestion(
    val title: String,
    val subtitle: String,
    val type: String, // "EVENT", "CATEGORY", "ORGANIZER", "VENUE", "FILTER"
    val queryValue: String
)

data class FilterState(
    val selectedCategories: Set<String> = emptySet(),
    val odOnly: Boolean = false,
    val selectedDateRange: String? = null, // "Today", "Tomorrow", "This Week"
    val selectedVenue: String? = null,
    val selectedClub: String? = null,
    val selectedDepartment: String? = null,
    val selectedStatus: String? = null,
    val discoveryMode: String = "ALL" // "ALL", "TRENDING", "TODAY", "TOMORROW", "STARTING_SOON", "RECOMMENDED", "FOLLOWED_CLUBS", "SAVED"
) {
    val activeFilterCount: Int
        get() = selectedCategories.size +
                (if (odOnly) 1 else 0) +
                (if (selectedDateRange != null) 1 else 0) +
                (if (selectedVenue != null) 1 else 0) +
                (if (selectedClub != null) 1 else 0) +
                (if (selectedDepartment != null) 1 else 0) +
                (if (selectedStatus != null) 1 else 0) +
                (if (discoveryMode != "ALL") 1 else 0)

    val isFiltering: Boolean
        get() = activeFilterCount > 0
}

class CollegeEventsViewModel(application: Application) : AndroidViewModel(application) {

    private val db = CollegeDatabase.getDatabase(application, viewModelScope)
    private val eventRepo = EventRepository(
        eventDao = db.eventDao(),
        historyDao = db.historyDao(),
        conflictDao = db.conflictDao(),
        notificationDao = db.notificationDao(),
        approvalDao = db.approvalDao(),
        clubDao = db.clubDao()
    )
    private val clubRepo = ClubRepository(db.clubDao())
    private val reportRepo = ReportRepository(db.reportDao())
    private val cachedEventRepo = CachedEventRepository(
        cachedEventDao = db.cachedEventDao(),
        firestoreEventRepository = FirestoreEventRepository.instance,
        eventDao = db.eventDao()
    )

    val allApprovalDecisions: Flow<List<EventApprovalDecisionEntity>> = eventRepo.allApprovalDecisions
    private val _selectedClubApprovalDecisions = MutableStateFlow<List<EventApprovalDecisionEntity>>(emptyList())
    val selectedClubApprovalDecisions: StateFlow<List<EventApprovalDecisionEntity>> = _selectedClubApprovalDecisions.asStateFlow()

    private val _selectedEventApprovalDecisions = MutableStateFlow<List<EventApprovalDecisionEntity>>(emptyList())
    val selectedEventApprovalDecisions: StateFlow<List<EventApprovalDecisionEntity>> = _selectedEventApprovalDecisions.asStateFlow()

    // Active User and Auth
    val currentUser: StateFlow<User> = UserSessionManager.currentUser
    val authService = AuthService.instance
    val isFirebaseAvailable: Boolean
        get() = authService.isFirebaseAvailable

    fun signInWithGoogle(context: android.content.Context, onComplete: ((Boolean, String?) -> Unit)? = null) {
        viewModelScope.launch {
            val result = authService.signInWithGoogle(context)
            result.onSuccess { user ->
                showStatus("Signed in via Google as ${user.name} (${user.role.displayName})")
                onComplete?.invoke(true, null)
            }.onFailure { err ->
                showStatus("Google sign-in: ${err.message ?: "Authentication cancelled"}")
                onComplete?.invoke(false, err.message)
            }
        }
    }

    fun signInWithEmail(email: String, pass: String, onComplete: ((Boolean, String?) -> Unit)? = null) {
        viewModelScope.launch {
            val result = authService.signInWithEmail(email, pass)
            result.onSuccess { user ->
                showStatus("Welcome, ${user.name} (${user.role.displayName})")
                onComplete?.invoke(true, null)
            }.onFailure { err ->
                showStatus("Sign in error: ${err.message}")
                onComplete?.invoke(false, err.message)
            }
        }
    }

    fun signUpWithEmail(
        email: String,
        pass: String,
        name: String,
        role: UserRole,
        department: String,
        clubAffiliation: String? = null,
        onComplete: ((Boolean, String?) -> Unit)? = null
    ) {
        viewModelScope.launch {
            val result = authService.signUpWithEmail(email, pass, name, role, department, clubAffiliation)
            result.onSuccess { user ->
                showStatus("Registered account for ${user.name} (${user.role.displayName})")
                onComplete?.invoke(true, null)
            }.onFailure { err ->
                showStatus("Sign up error: ${err.message}")
                onComplete?.invoke(false, err.message)
            }
        }
    }

    fun signInAnonymously(guestName: String = "Campus Guest", role: UserRole = UserRole.STUDENT, onComplete: ((Boolean, String?) -> Unit)? = null) {
        viewModelScope.launch {
            val result = authService.signInAnonymously(guestName, role)
            result.onSuccess { user ->
                showStatus("Signed in as ${user.name}")
                onComplete?.invoke(true, null)
            }.onFailure { err ->
                showStatus("Guest sign-in error: ${err.message}")
                onComplete?.invoke(false, err.message)
            }
        }
    }

    fun signOut(context: android.content.Context? = null) {
        viewModelScope.launch {
            authService.signOut(context)
            showStatus("Signed out")
        }
    }

    fun updateUserRoleAndProfile(role: UserRole, department: String? = null, clubAffiliation: String? = null) {
        authService.updateUserRole(role, department, clubAffiliation)
        showStatus("Updated role to ${role.displayName}")
    }

    // Raw database flows
    val allEvents = eventRepo.allEvents.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val savedEvents = eventRepo.savedEvents.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allClubs = clubRepo.allClubs.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allConflicts = eventRepo.allConflicts.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allReports = reportRepo.allReports.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allNotifications = eventRepo.allNotifications.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val unreadNotifications = eventRepo.unreadNotificationCount.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val allHistory = eventRepo.getAllHistory().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Room Cached Events Schema flows
    val allCachedEvents: StateFlow<List<CachedEventEntity>> = cachedEventRepo.allCachedEvents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val cachedEventEntities: StateFlow<List<EventEntity>> = cachedEventRepo.allCachedAsEventEntities
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Firestore Synchronization State
    private val _isFirestoreSyncing = MutableStateFlow(false)
    val isFirestoreSyncing: StateFlow<Boolean> = _isFirestoreSyncing.asStateFlow()

    private val _lastSyncTimestamp = MutableStateFlow<Long>(System.currentTimeMillis())
    val lastSyncTimestamp: StateFlow<Long> = _lastSyncTimestamp.asStateFlow()

    init {
        syncWithFirestore()
    }

    /**
     * Synchronizes events from remote Firestore and stores them in the local Room cached_events table.
     */
    fun syncWithFirestore() {
        viewModelScope.launch {
            _isFirestoreSyncing.value = true
            val result = cachedEventRepo.fetchAndCacheFromFirestore()
            _isFirestoreSyncing.value = false
            result.onSuccess { count ->
                _lastSyncTimestamp.value = System.currentTimeMillis()
                showStatus("Synchronized $count events from Firestore to Room cache")
            }.onFailure { err ->
                _lastSyncTimestamp.value = System.currentTimeMillis()
                showStatus("Cache sync: ${err.message ?: "Offline cached mode active"}")
            }
        }
    }

    // Navigation and Detail Selection
    private val _currentTab = MutableStateFlow(NavigationTab.HOME)
    val currentTab: StateFlow<NavigationTab> = _currentTab.asStateFlow()

    private val _currentPortal = MutableStateFlow(PortalView.NONE)
    val currentPortal: StateFlow<PortalView> = _currentPortal.asStateFlow()

    private val _selectedEvent = MutableStateFlow<EventEntity?>(null)
    val selectedEvent: StateFlow<EventEntity?> = _selectedEvent.asStateFlow()

    private val _selectedClub = MutableStateFlow<ClubEntity?>(null)
    val selectedClub: StateFlow<ClubEntity?> = _selectedClub.asStateFlow()

    // Club members for selected club
    private val _selectedClubMembers = MutableStateFlow<List<ClubMemberEntity>>(emptyList())
    val selectedClubMembers: StateFlow<List<ClubMemberEntity>> = _selectedClubMembers.asStateFlow()

    // Selected event history
    private val _selectedEventHistory = MutableStateFlow<List<EventHistoryEntity>>(emptyList())
    val selectedEventHistory: StateFlow<List<EventHistoryEntity>> = _selectedEventHistory.asStateFlow()

    // Search & Filter
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filterState = MutableStateFlow(FilterState())
    val filterState: StateFlow<FilterState> = _filterState.asStateFlow()

    val popularSearches = listOf("Hackathon", "OD Available", "Today", "Tomorrow", "Technical", "Cultural", "Robotics Lab", "Coding Club")
    private val _recentSearches = MutableStateFlow(listOf("HackVerse", "Cultural Fest", "OD Available"))
    val recentSearches: StateFlow<List<String>> = _recentSearches.asStateFlow()

    // Status Message / Toast
    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    // App Theme Setting (LIGHT, DARK, SYSTEM)
    private val prefs = application.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
    private val _themeMode = MutableStateFlow(
        try {
            ThemeMode.valueOf(prefs.getString("theme_mode", ThemeMode.DARK.name) ?: ThemeMode.DARK.name)
        } catch (e: Exception) {
            ThemeMode.DARK
        }
    )
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    // Authentication & App Open Login State
    private val _isLoggedIn = MutableStateFlow(
        prefs.getBoolean("is_logged_in", true)
    )
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    fun setLoggedIn(loggedIn: Boolean) {
        _isLoggedIn.value = loggedIn
        prefs.edit().putBoolean("is_logged_in", loggedIn).apply()
    }

    fun loginWithCustomUser(
        userId: String,
        name: String,
        role: UserRole,
        department: String,
        email: String? = null,
        clubAffiliation: String? = null
    ) {
        val cleanId = userId.trim().ifBlank { "user_" + UUID.randomUUID().toString().take(6) }
        val finalEmail = if (!email.isNullOrBlank()) {
            email.trim()
        } else if (cleanId.contains("@")) {
            cleanId
        } else {
            "${cleanId.lowercase()}@college.edu"
        }
        val finalName = name.trim().ifBlank { cleanId.replaceFirstChar { it.uppercase() } }
        val user = User(
            userId = cleanId,
            name = finalName,
            email = finalEmail,
            role = role,
            department = department.trim().ifBlank { "Computer Science & Engineering" },
            clubAffiliation = clubAffiliation,
            authProvider = "Campus Pulse ID"
        )
        UserSessionManager.switchUser(user)
        setLoggedIn(true)
        showStatus("Signed in as ${user.name} (${user.role.displayName})")
    }

    fun logout(context: Context? = null) {
        viewModelScope.launch {
            authService.signOut(context)
            UserSessionManager.signOut()
            setLoggedIn(false)
            showStatus("Logged out of Campus Pulse")
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        prefs.edit().putString("theme_mode", mode.name).apply()
        showStatus("Switched to ${mode.name.lowercase().replaceFirstChar { it.uppercase() }} Theme")
    }

    companion object {
        const val BASE_TODAY = "2026-09-20"
        const val BASE_TOMORROW = "2026-09-21"
        const val BASE_WEEK_END = "2026-09-27"
    }

    fun getEffectiveToday(): String {
        val currentStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
        return if (currentStr.startsWith("2026-09")) currentStr else BASE_TODAY
    }

    fun getEffectiveTomorrow(): String {
        return try {
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
            val date = sdf.parse(getEffectiveToday()) ?: return BASE_TOMORROW
            val cal = java.util.Calendar.getInstance().apply { time = date; add(java.util.Calendar.DAY_OF_YEAR, 1) }
            sdf.format(cal.time)
        } catch (e: Exception) {
            BASE_TOMORROW
        }
    }

    fun getEffectiveEndOfWeek(): String {
        return try {
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
            val date = sdf.parse(getEffectiveToday()) ?: return BASE_WEEK_END
            val cal = java.util.Calendar.getInstance().apply { time = date; add(java.util.Calendar.DAY_OF_YEAR, 7) }
            sdf.format(cal.time)
        } catch (e: Exception) {
            BASE_WEEK_END
        }
    }

    // Smart Search Suggestions
    val searchSuggestions: StateFlow<List<SearchSuggestion>> = combine(allEvents, _searchQuery) { events, query ->
        val trimmed = query.trim()
        if (trimmed.isEmpty()) {
            emptyList()
        } else {
            val list = mutableListOf<SearchSuggestion>()

            // Matching Event Titles
            events.filter { it.title.contains(trimmed, ignoreCase = true) }
                .take(3)
                .forEach {
                    list.add(SearchSuggestion(it.title, "${it.category} • ${it.startDate}", "EVENT", it.title))
                }

            // Matching Categories
            listOf("Technical", "Cultural", "Sports", "Academic", "Workshop", "Hackathon")
                .filter { it.contains(trimmed, ignoreCase = true) }
                .take(2)
                .forEach {
                    list.add(SearchSuggestion("$it Events", "Category Filter", "CATEGORY", it))
                }

            // Matching Venues
            val venues = listOf("Main Tech Auditorium", "Open Air Amphitheatre", "Robotics Lab", "Seminar Hall", "SAC Courtyard", "Sports Stadium")
            venues.filter { it.contains(trimmed, ignoreCase = true) }
                .take(2)
                .forEach {
                    list.add(SearchSuggestion(it, "Campus Venue", "VENUE", it))
                }

            // Matching Organizers
            val organizers = listOf("Coding Club", "Cultural Society", "Robotics Club", "IEEE Student Branch", "Sports Council")
            organizers.filter { it.contains(trimmed, ignoreCase = true) }
                .take(2)
                .forEach {
                    list.add(SearchSuggestion(it, "Club / Organizer", "ORGANIZER", it))
                }

            // OD Keyword
            if ("od available".contains(trimmed, ignoreCase = true) || "on duty".contains(trimmed, ignoreCase = true) || "duty".contains(trimmed, ignoreCase = true)) {
                list.add(SearchSuggestion("OD Available", "Filter official academic credit", "FILTER", "OD Available"))
            }

            list.distinctBy { it.title }.take(6)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Curated Event Streams for Discovery
    val publishedEvents: StateFlow<List<EventEntity>> = allEvents.combine(currentUser) { events, _ ->
        events.filter { it.eventStatus == "PUBLISHED" }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val trendingEvents: StateFlow<List<EventEntity>> = allEvents.combine(savedEvents) { events, _ ->
        events.filter { it.eventStatus == "PUBLISHED" }
            .sortedByDescending { event ->
                var score = 0
                if (event.posterUrl.isNotEmpty()) score += 10
                if (event.isSaved) score += 5
                if (event.odStatus == OdStatus.AVAILABLE.name) score += 4
                if (event.verificationStatus == "VERIFIED") score += 3
                score
            }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayEvents: StateFlow<List<EventEntity>> = allEvents.combine(allEvents) { events, _ ->
        val today = getEffectiveToday()
        events.filter { it.eventStatus == "PUBLISHED" && it.startDate == today }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tomorrowEvents: StateFlow<List<EventEntity>> = allEvents.combine(allEvents) { events, _ ->
        val tomorrow = getEffectiveTomorrow()
        events.filter { it.eventStatus == "PUBLISHED" && it.startDate == tomorrow }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val startingSoonEvents: StateFlow<List<EventEntity>> = allEvents.combine(allEvents) { events, _ ->
        val today = getEffectiveToday()
        events.filter { it.eventStatus == "PUBLISHED" && it.startDate >= today && it.startDate <= "2026-09-24" }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recommendedEvents: StateFlow<List<EventEntity>> = allEvents.combine(currentUser) { events, user ->
        events.filter { it.eventStatus == "PUBLISHED" && (it.odStatus == OdStatus.AVAILABLE.name || it.verificationStatus == "VERIFIED" || it.department == user.department) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val followedClubsEvents: StateFlow<List<EventEntity>> = combine(allEvents, allClubs) { events, clubs ->
        val followedClubNames = clubs.filter { it.isFollowed }.map { it.name }.toSet()
        val defaultFollowed = setOf("Coding Club", "Cultural Society")
        val allFollowed = if (followedClubNames.isNotEmpty()) followedClubNames else defaultFollowed
        events.filter { it.eventStatus == "PUBLISHED" && allFollowed.contains(it.organizer) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered Events
    val filteredEvents: StateFlow<List<EventEntity>> = combine(
        allEvents,
        searchQuery,
        filterState
    ) { events, query, filter ->
        val today = getEffectiveToday()
        val tomorrow = getEffectiveTomorrow()
        val endOfWeek = getEffectiveEndOfWeek()

        events.filter { event ->
            // Search query matching
            val matchesQuery = query.isBlank() ||
                    event.title.contains(query, ignoreCase = true) ||
                    event.description.contains(query, ignoreCase = true) ||
                    event.organizer.contains(query, ignoreCase = true) ||
                    event.department.contains(query, ignoreCase = true) ||
                    event.venue.contains(query, ignoreCase = true) ||
                    event.category.contains(query, ignoreCase = true) ||
                    event.subcategory.contains(query, ignoreCase = true)

            // Discovery mode matching
            val matchesDiscovery = when (filter.discoveryMode) {
                "TRENDING" -> event.eventStatus == "PUBLISHED" && (event.posterUrl.isNotEmpty() || event.isSaved)
                "TODAY" -> event.startDate == today
                "TOMORROW" -> event.startDate == tomorrow
                "STARTING_SOON" -> event.startDate >= today && event.startDate <= "2026-09-24"
                "RECOMMENDED" -> event.odStatus == OdStatus.AVAILABLE.name && event.verificationStatus == "VERIFIED"
                "FOLLOWED_CLUBS" -> event.organizer == "Coding Club" || event.organizer == "Cultural Society"
                "SAVED" -> event.isSaved
                else -> true
            }

            // Category filter
            val matchesCategory = filter.selectedCategories.isEmpty() ||
                    filter.selectedCategories.any { it.equals(event.category, ignoreCase = true) }

            // OD filter
            val matchesOd = !filter.odOnly || event.odStatus == OdStatus.AVAILABLE.name

            // Date range filter
            val matchesDate = when (filter.selectedDateRange) {
                "Today" -> event.startDate == today
                "Tomorrow" -> event.startDate == tomorrow
                "This Week" -> event.startDate >= today && event.startDate <= endOfWeek
                else -> true
            }

            // Venue filter
            val matchesVenue = filter.selectedVenue == null || event.venue.contains(filter.selectedVenue, ignoreCase = true)

            // Club filter
            val matchesClub = filter.selectedClub == null || event.organizer.contains(filter.selectedClub, ignoreCase = true)

            // Department filter
            val matchesDept = filter.selectedDepartment == null || event.department.contains(filter.selectedDepartment, ignoreCase = true)

            // Status filter
            val matchesStatus = filter.selectedStatus == null || event.eventStatus.equals(filter.selectedStatus, ignoreCase = true)

            matchesQuery && matchesDiscovery && matchesCategory && matchesOd && matchesDate && matchesVenue && matchesClub && matchesDept && matchesStatus
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setTab(tab: NavigationTab) {
        _currentTab.value = tab
        _currentPortal.value = PortalView.NONE
        _selectedEvent.value = null
        _selectedClub.value = null
    }

    fun openPortal(portal: PortalView) {
        _currentPortal.value = portal
        _selectedEvent.value = null
        _selectedClub.value = null
    }

    fun closePortal() {
        _currentPortal.value = PortalView.NONE
    }

    fun selectEvent(event: EventEntity?) {
        _selectedEvent.value = event
        if (event != null) {
            viewModelScope.launch {
                eventRepo.getEventHistory(event.eventId).collect {
                    _selectedEventHistory.value = it
                }
            }
            viewModelScope.launch {
                eventRepo.getApprovalDecisionsForEvent(event.eventId).collect {
                    _selectedEventApprovalDecisions.value = it
                }
            }
        } else {
            _selectedEventApprovalDecisions.value = emptyList()
        }
    }

    fun selectClub(club: ClubEntity?) {
        _selectedClub.value = club
        if (club != null) {
            viewModelScope.launch {
                clubRepo.getClubMembers(club.clubId).collect {
                    _selectedClubMembers.value = it
                }
            }
            viewModelScope.launch {
                eventRepo.getApprovalDecisionsForClub(club.clubId).collect {
                    _selectedClubApprovalDecisions.value = it
                }
            }
        } else {
            _selectedClubApprovalDecisions.value = emptyList()
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        if (query.isNotBlank() && !_recentSearches.value.contains(query)) {
            _recentSearches.value = (listOf(query) + _recentSearches.value).take(5)
        }
    }

    fun toggleCategoryFilter(category: String) {
        val current = _filterState.value.selectedCategories.toMutableSet()
        if (current.contains(category)) {
            current.remove(category)
        } else {
            current.add(category)
        }
        _filterState.value = _filterState.value.copy(selectedCategories = current)
    }

    fun toggleOdFilter() {
        _filterState.value = _filterState.value.copy(odOnly = !_filterState.value.odOnly)
    }

    fun setDateFilter(range: String?) {
        _filterState.value = _filterState.value.copy(selectedDateRange = range)
    }

    fun setVenueFilter(venue: String?) {
        _filterState.value = _filterState.value.copy(selectedVenue = venue)
    }

    fun setClubFilter(club: String?) {
        _filterState.value = _filterState.value.copy(selectedClub = club)
    }

    fun setDiscoveryMode(mode: String) {
        _filterState.value = _filterState.value.copy(discoveryMode = mode)
    }

    fun removeRecentSearch(term: String) {
        _recentSearches.value = _recentSearches.value.filter { it != term }
    }

    fun clearRecentSearches() {
        _recentSearches.value = emptyList()
    }

    fun addRecentSearch(query: String) {
        if (query.isNotBlank()) {
            _recentSearches.value = (listOf(query) + _recentSearches.value.filter { it != query }).take(8)
        }
    }

    fun clearFilters() {
        _filterState.value = FilterState()
        _searchQuery.value = ""
    }

    fun switchRole(role: UserRole) {
        UserSessionManager.switchRole(role)
        showStatus("Switched to role: ${role.displayName}")
    }

    fun toggleSaveEvent(eventId: String, isCurrentlySaved: Boolean) {
        viewModelScope.launch {
            val targetSaved = !isCurrentlySaved
            eventRepo.toggleSave(eventId, targetSaved)
            cachedEventRepo.toggleSave(eventId, targetSaved)
            showStatus(if (targetSaved) "Event saved to bookmarks" else "Removed from saved events")
        }
    }

    fun toggleFollowClub(clubId: String, isCurrentlyFollowed: Boolean) {
        viewModelScope.launch {
            clubRepo.toggleFollowClub(clubId, !isCurrentlyFollowed)
            showStatus(if (!isCurrentlyFollowed) "Following club updates" else "Unfollowed club")
        }
    }

    fun submitReport(
        eventId: String,
        eventTitle: String,
        issueType: String,
        description: String
    ) {
        val user = currentUser.value
        val report = EventReportEntity(
            reportId = "REP-${UUID.randomUUID().toString().take(6).uppercase()}",
            eventId = eventId,
            eventTitle = eventTitle,
            userId = user.userId,
            userName = "${user.name} (${user.role.displayName})",
            issueType = issueType,
            description = description,
            status = "PENDING",
            submittedAt = System.currentTimeMillis()
        )
        viewModelScope.launch {
            reportRepo.submitReport(report)
            showStatus("Report submitted for verification. Faculty and Admins notified.")
        }
    }

    // Faculty Actions
    fun facultyVerifyEvent(eventId: String, isApproved: Boolean, reason: String) {
        viewModelScope.launch {
            eventRepo.verifyEvent(eventId, currentUser.value, isApproved, reason)
            showStatus(if (isApproved) "Event officially verified!" else "Event rejected.")
            refreshSelectedEvent(eventId)
        }
    }

    fun facultyUpdateOd(eventId: String, odStatus: OdStatus, reason: String) {
        viewModelScope.launch {
            eventRepo.updateOdStatus(eventId, odStatus, currentUser.value, reason)
            showStatus("OD Status updated to ${odStatus.label}")
            refreshSelectedEvent(eventId)
        }
    }

    fun facultyUpdateVenueAndTime(
        eventId: String,
        newVenue: String,
        newDate: String,
        newTime: String,
        reason: String
    ) {
        viewModelScope.launch {
            val old = eventRepo.getEventByIdOnce(eventId) ?: return@launch
            val updated = old.copy(
                venue = newVenue,
                startDate = newDate,
                startTime = newTime,
                updatedBy = currentUser.value.name,
                updatedAt = System.currentTimeMillis()
            )
            eventRepo.updateEventDetails(old, updated, currentUser.value, reason)
            showStatus("Venue and timing updated.")
            refreshSelectedEvent(eventId)
        }
    }

    fun facultyCancelEvent(eventId: String, reason: String) {
        viewModelScope.launch {
            eventRepo.cancelEvent(eventId, currentUser.value, reason)
            showStatus("Event marked as cancelled.")
            refreshSelectedEvent(eventId)
        }
    }

    // ==========================================
    // BACKEND-ENFORCED CLUB MANAGEMENT ACTIONS
    // Hierarchy: President -> Admin -> Member
    // Workflow: Draft -> Submitted -> Under Review -> Approved -> Published
    // ==========================================

    fun submitClubEvent(
        title: String,
        description: String,
        category: String,
        startDate: String,
        startTime: String,
        endTime: String,
        venue: String,
        club: ClubEntity,
        isDraft: Boolean = false,
        reason: String = "Event submitted by member",
        onComplete: ((Boolean) -> Unit)? = null
    ) {
        viewModelScope.launch {
            val newEvent = EventEntity(
                eventId = "EVT-${UUID.randomUUID().toString().take(8).uppercase()}",
                title = title,
                description = description,
                category = category,
                startDate = startDate,
                startTime = startTime,
                endTime = endTime,
                venue = venue,
                organizer = club.name,
                organizerId = club.clubId,
                department = club.department,
                source = "Club Submission Portal",
                sourceType = SourceType.CLUB_PRESIDENT.name,
                eventStatus = if (isDraft) EventStatus.DRAFT.name else EventStatus.SUBMITTED.name,
                verificationStatus = VerificationStatus.UNVERIFIED.name,
                odStatus = OdStatus.NOT_AVAILABLE.name,
                createdBy = "${currentUser.value.name} (${currentUser.value.role.displayName})",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            val result = eventRepo.submitClubEvent(
                event = newEvent,
                user = currentUser.value,
                clubId = club.clubId,
                isDraft = isDraft,
                reason = reason
            )
            result.onSuccess {
                val stateDesc = if (isDraft) "saved as Draft" else "submitted for review (no auto-publish)"
                showStatus("✅ Event '$title' $stateDesc.")
                onComplete?.invoke(true)
            }.onFailure { err ->
                showStatus("⚠️ Submission Error: ${err.message}")
                onComplete?.invoke(false)
            }
        }
    }

    fun moveEventToReview(eventId: String, clubId: String, notes: String = "Moving to under review") {
        viewModelScope.launch {
            val result = eventRepo.moveEventToReview(eventId, currentUser.value, clubId, notes)
            result.onSuccess {
                showStatus("✅ Event moved to 'Under Review'")
                refreshSelectedEvent(eventId)
            }.onFailure { err ->
                showStatus("⚠️ Authorization Error: ${err.message}")
            }
        }
    }

    fun approveClubEvent(eventId: String, clubId: String, notes: String = "Event proposal approved") {
        viewModelScope.launch {
            val result = eventRepo.approveEvent(eventId, currentUser.value, clubId, notes)
            result.onSuccess {
                showStatus("✅ Event approved! Ready for publication.")
                refreshSelectedEvent(eventId)
            }.onFailure { err ->
                showStatus("⚠️ Authorization Error: ${err.message}")
            }
        }
    }

    fun publishClubEvent(eventId: String, clubId: String, notes: String = "Event officially published to campus") {
        viewModelScope.launch {
            val result = eventRepo.publishEvent(eventId, currentUser.value, clubId, notes)
            result.onSuccess {
                showStatus("🎉 Event published and verified campus-wide!")
                refreshSelectedEvent(eventId)
            }.onFailure { err ->
                showStatus("⚠️ Authorization Error: ${err.message}")
            }
        }
    }

    fun requestChangesOnClubEvent(eventId: String, clubId: String, reason: String) {
        viewModelScope.launch {
            val result = eventRepo.requestChanges(eventId, currentUser.value, clubId, reason)
            result.onSuccess {
                showStatus("📝 Changes requested: $reason")
                refreshSelectedEvent(eventId)
            }.onFailure { err ->
                showStatus("⚠️ Authorization Error: ${err.message}")
            }
        }
    }

    fun rejectClubEvent(eventId: String, clubId: String, reason: String) {
        viewModelScope.launch {
            val result = eventRepo.rejectEvent(eventId, currentUser.value, clubId, reason)
            result.onSuccess {
                showStatus("❌ Event proposal rejected: $reason")
                refreshSelectedEvent(eventId)
            }.onFailure { err ->
                showStatus("⚠️ Authorization Error: ${err.message}")
            }
        }
    }

    fun editClubEvent(
        oldEvent: EventEntity,
        updatedEvent: EventEntity,
        clubId: String,
        reason: String = "Edited by club administration"
    ) {
        viewModelScope.launch {
            val result = eventRepo.editClubEvent(oldEvent, updatedEvent, currentUser.value, clubId, reason)
            result.onSuccess {
                showStatus("✅ Event details successfully updated.")
                refreshSelectedEvent(oldEvent.eventId)
            }.onFailure { err ->
                showStatus("⚠️ Authorization Error: ${err.message}")
            }
        }
    }

    fun cancelClubEvent(eventId: String, clubId: String, reason: String) {
        viewModelScope.launch {
            val result = eventRepo.cancelClubEvent(eventId, currentUser.value, clubId, reason)
            result.onSuccess {
                showStatus("⚠️ Event cancelled: $reason")
                refreshSelectedEvent(eventId)
            }.onFailure { err ->
                showStatus("⚠️ Authorization Error: ${err.message}")
            }
        }
    }

    fun assignClubAdmin(clubId: String, memberId: Int, memberName: String) {
        viewModelScope.launch {
            val result = clubRepo.assignAdmin(currentUser.value, clubId, memberId)
            result.onSuccess {
                showStatus("✅ Promoted $memberName to Club Admin.")
            }.onFailure { err ->
                showStatus("⚠️ Authorization Error: ${err.message}")
            }
        }
    }

    fun removeClubAdmin(clubId: String, memberId: Int, memberName: String) {
        viewModelScope.launch {
            val result = clubRepo.removeAdmin(currentUser.value, clubId, memberId)
            result.onSuccess {
                showStatus("Demoted $memberName to standard Club Member.")
            }.onFailure { err ->
                showStatus("⚠️ Authorization Error: ${err.message}")
            }
        }
    }

    fun updateClubAdminPermissions(clubId: String, permissions: ClubAdminPermissions) {
        viewModelScope.launch {
            val result = clubRepo.updateAdminPermissions(currentUser.value, clubId, permissions)
            result.onSuccess {
                showStatus("✅ Club Admin permissions updated successfully.")
            }.onFailure { err ->
                showStatus("⚠️ Authorization Error: ${err.message}")
            }
        }
    }

    fun updateClubProfile(club: ClubEntity) {
        viewModelScope.launch {
            val result = clubRepo.updateClubProfile(currentUser.value, club)
            result.onSuccess {
                showStatus("✅ Club profile saved successfully.")
                _selectedClub.value = club
            }.onFailure { err ->
                showStatus("⚠️ Authorization Error: ${err.message}")
            }
        }
    }

    fun removeClubMember(clubId: String, memberId: Int, name: String) {
        viewModelScope.launch {
            val result = clubRepo.removeMember(currentUser.value, clubId, memberId)
            result.onSuccess {
                showStatus("Removed $name from club.")
            }.onFailure { err ->
                showStatus("⚠️ Authorization Error: ${err.message}")
            }
        }
    }

    fun approveMembershipRequest(clubId: String, memberId: Int, name: String) {
        viewModelScope.launch {
            val result = clubRepo.approveMemberRequest(currentUser.value, clubId, memberId)
            result.onSuccess {
                showStatus("Approved membership for $name.")
            }.onFailure { err ->
                showStatus("⚠️ Authorization Error: ${err.message}")
            }
        }
    }

    // Legacy overloads for compatibility
    fun approveMemberSubmission(eventId: String) {
        viewModelScope.launch {
            eventRepo.approveMemberSubmission(eventId, currentUser.value)
            showStatus("Member submission approved and published!")
            refreshSelectedEvent(eventId)
        }
    }

    fun rejectMemberSubmission(eventId: String, reason: String) {
        viewModelScope.launch {
            eventRepo.rejectMemberSubmission(eventId, currentUser.value, reason)
            showStatus("Submission returned to draft: $reason")
            refreshSelectedEvent(eventId)
        }
    }

    fun addClubMember(clubId: String, name: String, email: String, role: String) {
        viewModelScope.launch {
            val newMember = ClubMemberEntity(
                clubId = clubId,
                userId = "user_${UUID.randomUUID().toString().take(5)}",
                userName = name,
                userEmail = email,
                roleInClub = role,
                status = "ACTIVE"
            )
            clubRepo.addMember(newMember)
            showStatus("Member $name added to club.")
        }
    }

    fun removeClubMember(memberId: Int, name: String) {
        viewModelScope.launch {
            clubRepo.removeMember(memberId)
            showStatus("Removed $name from club.")
        }
    }

    fun approveMembershipRequest(memberId: Int, name: String) {
        viewModelScope.launch {
            clubRepo.approveMemberRequest(memberId)
            showStatus("Approved membership for $name.")
        }
    }

    fun updateMemberRole(memberId: Int, newRole: String) {
        viewModelScope.launch {
            clubRepo.updateMemberRole(memberId, newRole)
            showStatus("Updated member role to $newRole.")
        }
    }

    // Community / Member Event Creation
    fun submitNewEvent(
        title: String,
        description: String,
        category: String,
        startDate: String,
        startTime: String,
        endTime: String,
        venue: String,
        organizer: String,
        department: String,
        odStatus: OdStatus,
        registrationUrl: String,
        sourceType: SourceType,
        sourceUrl: String
    ) {
        val user = currentUser.value
        val isPresidentOrAdmin = user.role == UserRole.CLUB_PRESIDENT || user.role == UserRole.PLATFORM_ADMIN || user.role == UserRole.FACULTY
        val now = System.currentTimeMillis()
        val eventId = "EVT-${UUID.randomUUID().toString().take(6).uppercase()}"

        val newEvent = EventEntity(
            eventId = eventId,
            title = title,
            description = description,
            category = category,
            startDate = startDate,
            startTime = startTime,
            endTime = endTime,
            venue = venue,
            organizer = organizer,
            organizerType = if (user.role == UserRole.FACULTY) "DEPARTMENT" else "CLUB",
            department = department,
            odStatus = odStatus.name,
            registrationRequired = registrationUrl.isNotEmpty(),
            registrationUrl = registrationUrl,
            contactInformation = user.email,
            posterUrl = if (category == "Technical") "poster_hackathon" else "poster_cultural",
            source = sourceType.label,
            sourceType = sourceType.name,
            sourceUrl = sourceUrl,
            createdBy = "${user.name} (${user.role.displayName})",
            updatedBy = user.name,
            verifiedBy = if (user.role == UserRole.FACULTY || user.role == UserRole.PLATFORM_ADMIN) user.name else "",
            verificationStatus = if (user.role == UserRole.FACULTY || user.role == UserRole.PLATFORM_ADMIN) VerificationStatus.VERIFIED.name else VerificationStatus.PENDING.name,
            eventStatus = if (isPresidentOrAdmin) EventStatus.PUBLISHED.name else EventStatus.PENDING.name,
            createdAt = now,
            updatedAt = now
        )

        viewModelScope.launch {
            eventRepo.createEvent(newEvent, user, "New event submission via ${sourceType.label}")
            showStatus(
                if (isPresidentOrAdmin) "Event created & published successfully!"
                else "Event submitted for club president / faculty verification."
            )
        }
    }

    // AI Extraction
    fun extractEventFromAnnouncement(rawText: String) {
        val draft = AiExtractionService.extractEventFromText(rawText)
        val entity = AiExtractionService.createDraftEntity(draft)
        viewModelScope.launch {
            eventRepo.createEvent(entity, currentUser.value, "Extracted from circular announcement by AI")
            showStatus("AI Extracted Draft Event created: '${draft.title}'. Requires verification.")
        }
    }

    // Platform Admin Actions
    fun adminResolveConflict(conflictId: String, eventId: String, chosenSource: String, chosenValue: String) {
        viewModelScope.launch {
            eventRepo.resolveConflict(conflictId, eventId, chosenSource, chosenValue, currentUser.value)
            showStatus("Conflict resolved with $chosenSource data.")
            refreshSelectedEvent(eventId)
        }
    }

    fun adminMergeDuplicate(keepId: String, duplicateId: String) {
        viewModelScope.launch {
            eventRepo.mergeDuplicateEvent(keepId, duplicateId, currentUser.value)
            showStatus("Duplicate event merged into $keepId.")
        }
    }

    fun triggerCollegeApiSync() {
        viewModelScope.launch {
            val res = eventRepo.triggerSyncCycle(currentUser.value)
            showStatus(res)
        }
    }

    fun markNotificationRead(id: Int) {
        viewModelScope.launch {
            eventRepo.markNotificationAsRead(id)
        }
    }

    fun markAllNotificationsRead() {
        viewModelScope.launch {
            eventRepo.markAllNotificationsAsRead()
            showStatus("All notifications marked as read.")
        }
    }

    fun resolveReport(reportId: String, resolutionNotes: String) {
        viewModelScope.launch {
            reportRepo.resolveReport(reportId, "RESOLVED", resolutionNotes)
            showStatus("Report marked as resolved.")
        }
    }

    private fun refreshSelectedEvent(eventId: String) {
        viewModelScope.launch {
            val refreshed = eventRepo.getEventByIdOnce(eventId)
            _selectedEvent.value = refreshed
        }
    }

    private fun showStatus(message: String) {
        _statusMessage.value = message
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }
}
