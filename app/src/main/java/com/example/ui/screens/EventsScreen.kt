package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AppleCard
import com.example.ui.components.EventCard
import com.example.ui.theme.RoyalBlue600
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import com.example.ui.viewmodel.CollegeEventsViewModel
import com.example.ui.viewmodel.NavigationTab
import com.example.ui.viewmodel.SearchSuggestion
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EventsScreen(
    viewModel: CollegeEventsViewModel,
    modifier: Modifier = Modifier
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filterState by viewModel.filterState.collectAsState()
    val filteredEvents by viewModel.filteredEvents.collectAsState()
    val searchSuggestions by viewModel.searchSuggestions.collectAsState()
    val recentSearches by viewModel.recentSearches.collectAsState()

    var showFilterSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("events_screen")
    ) {
        // 1. Search Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text(
                text = "Events",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Discover workshops, hackathons, and cultural meets across campus",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Search input with filter trigger
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("Search title, club, venue...", color = Slate400, fontSize = 14.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = Slate400)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear search", tint = Slate500)
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("events_search_input")
                )

                Spacer(modifier = Modifier.width(10.dp))

                // Filter Button with active count badge
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (filterState.isFiltering) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, if (filterState.isFiltering) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .size(52.dp)
                        .clickable { showFilterSheet = true }
                        .testTag("filter_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = "Filters",
                                tint = if (filterState.isFiltering) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                            if (filterState.activeFilterCount > 0) {
                                Text(
                                    text = "${filterState.activeFilterCount}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(start = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Smart Search Suggestions Dropdown Card
        if (searchQuery.isNotBlank() && searchSuggestions.isNotEmpty()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                shadowElevation = 6.dp
            ) {
                Column(modifier = Modifier.padding(vertical = 6.dp)) {
                    Text(
                        text = "SUGGESTIONS",
                        style = MaterialTheme.typography.labelSmall,
                        color = Slate500,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                    searchSuggestions.forEach { suggestion ->
                        val icon = when (suggestion.type) {
                            "EVENT" -> Icons.Default.Event
                            "CATEGORY" -> Icons.Default.Category
                            "VENUE" -> Icons.Default.LocationOn
                            "ORGANIZER" -> Icons.Default.Business
                            else -> Icons.Default.Shield
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.addRecentSearch(suggestion.queryValue)
                                    when (suggestion.type) {
                                        "CATEGORY" -> {
                                            viewModel.toggleCategoryFilter(suggestion.queryValue)
                                            viewModel.setSearchQuery("")
                                        }
                                        "VENUE" -> {
                                            viewModel.setVenueFilter(suggestion.queryValue)
                                            viewModel.setSearchQuery("")
                                        }
                                        "FILTER" -> {
                                            viewModel.toggleOdFilter()
                                            viewModel.setSearchQuery("")
                                        }
                                        "ORGANIZER" -> {
                                            viewModel.setClubFilter(suggestion.queryValue)
                                            viewModel.setSearchQuery("")
                                        }
                                        else -> {
                                            viewModel.setSearchQuery(suggestion.queryValue)
                                        }
                                    }
                                }
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = RoyalBlue600,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = suggestion.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = suggestion.subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate500,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Quick Discovery Filter Chips (All, Trending, Today, Tomorrow, Starting Soon, OD, Recommended, Followed Clubs, Saved)
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            item {
                FilterChip(
                    selected = filterState.discoveryMode == "ALL" && !filterState.isFiltering,
                    onClick = {
                        viewModel.clearFilters()
                    },
                    label = { Text("All") },
                    shape = RoundedCornerShape(12.dp)
                )
            }
            item {
                FilterChip(
                    selected = filterState.discoveryMode == "TRENDING",
                    onClick = {
                        viewModel.setDiscoveryMode(if (filterState.discoveryMode == "TRENDING") "ALL" else "TRENDING")
                    },
                    label = { Text("Trending") },
                    shape = RoundedCornerShape(12.dp)
                )
            }
            item {
                FilterChip(
                    selected = filterState.discoveryMode == "TODAY" || filterState.selectedDateRange == "Today",
                    onClick = {
                        val newMode = if (filterState.discoveryMode == "TODAY") "ALL" else "TODAY"
                        viewModel.setDiscoveryMode(newMode)
                    },
                    label = { Text("Today") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("filter_chip_today")
                )
            }
            item {
                FilterChip(
                    selected = filterState.discoveryMode == "TOMORROW" || filterState.selectedDateRange == "Tomorrow",
                    onClick = {
                        val newMode = if (filterState.discoveryMode == "TOMORROW") "ALL" else "TOMORROW"
                        viewModel.setDiscoveryMode(newMode)
                    },
                    label = { Text("Tomorrow") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("filter_chip_tomorrow")
                )
            }
            item {
                FilterChip(
                    selected = filterState.discoveryMode == "STARTING_SOON",
                    onClick = {
                        val newMode = if (filterState.discoveryMode == "STARTING_SOON") "ALL" else "STARTING_SOON"
                        viewModel.setDiscoveryMode(newMode)
                    },
                    label = { Text("Starting Soon") },
                    shape = RoundedCornerShape(12.dp)
                )
            }
            item {
                FilterChip(
                    selected = filterState.odOnly,
                    onClick = { viewModel.toggleOdFilter() },
                    label = { Text("OD Available") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("filter_chip_od")
                )
            }
            item {
                FilterChip(
                    selected = filterState.discoveryMode == "RECOMMENDED",
                    onClick = {
                        val newMode = if (filterState.discoveryMode == "RECOMMENDED") "ALL" else "RECOMMENDED"
                        viewModel.setDiscoveryMode(newMode)
                    },
                    label = { Text("Recommended") },
                    shape = RoundedCornerShape(12.dp)
                )
            }
            item {
                FilterChip(
                    selected = filterState.discoveryMode == "FOLLOWED_CLUBS",
                    onClick = {
                        val newMode = if (filterState.discoveryMode == "FOLLOWED_CLUBS") "ALL" else "FOLLOWED_CLUBS"
                        viewModel.setDiscoveryMode(newMode)
                    },
                    label = { Text("Followed Clubs") },
                    shape = RoundedCornerShape(12.dp)
                )
            }
            item {
                FilterChip(
                    selected = filterState.discoveryMode == "SAVED",
                    onClick = {
                        val newMode = if (filterState.discoveryMode == "SAVED") "ALL" else "SAVED"
                        viewModel.setDiscoveryMode(newMode)
                    },
                    label = { Text("Saved") },
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        // 4. Active Removable Filter Chips Row
        if (filterState.isFiltering) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Clear All Button
                item {
                    AssistChip(
                        onClick = { viewModel.clearFilters() },
                        label = { Text("Clear all", fontWeight = FontWeight.Bold) },
                        leadingIcon = {
                            Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(14.dp))
                        },
                        colors = AssistChipDefaults.assistChipColors(labelColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.testTag("clear_filters_chip")
                    )
                }

                // Discovery Mode Chip (if not ALL)
                if (filterState.discoveryMode != "ALL") {
                    item {
                        InputChip(
                            selected = true,
                            onClick = { viewModel.setDiscoveryMode("ALL") },
                            label = { Text("Mode: ${filterState.discoveryMode.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }}") },
                            trailingIcon = {
                                Icon(Icons.Default.Close, contentDescription = "Remove", modifier = Modifier.size(14.dp))
                            }
                        )
                    }
                }

                // Category chips
                items(filterState.selectedCategories.toList()) { cat ->
                    InputChip(
                        selected = true,
                        onClick = { viewModel.toggleCategoryFilter(cat) },
                        label = { Text(cat) },
                        trailingIcon = {
                            Icon(Icons.Default.Close, contentDescription = "Remove", modifier = Modifier.size(14.dp))
                        }
                    )
                }

                // OD Chip
                if (filterState.odOnly) {
                    item {
                        InputChip(
                            selected = true,
                            onClick = { viewModel.toggleOdFilter() },
                            label = { Text("✓ OD Available") },
                            trailingIcon = {
                                Icon(Icons.Default.Close, contentDescription = "Remove", modifier = Modifier.size(14.dp))
                            }
                        )
                    }
                }

                // Date Chip
                filterState.selectedDateRange?.let { date ->
                    item {
                        InputChip(
                            selected = true,
                            onClick = { viewModel.setDateFilter(null) },
                            label = { Text("Date: $date") },
                            trailingIcon = {
                                Icon(Icons.Default.Close, contentDescription = "Remove", modifier = Modifier.size(14.dp))
                            }
                        )
                    }
                }

                // Venue Chip
                filterState.selectedVenue?.let { venue ->
                    item {
                        InputChip(
                            selected = true,
                            onClick = { viewModel.setVenueFilter(null) },
                            label = { Text("Venue: $venue") },
                            trailingIcon = {
                                Icon(Icons.Default.Close, contentDescription = "Remove", modifier = Modifier.size(14.dp))
                            }
                        )
                    }
                }

                // Club Chip
                filterState.selectedClub?.let { club ->
                    item {
                        InputChip(
                            selected = true,
                            onClick = { viewModel.setClubFilter(null) },
                            label = { Text("Club: $club") },
                            trailingIcon = {
                                Icon(Icons.Default.Close, contentDescription = "Remove", modifier = Modifier.size(14.dp))
                            }
                        )
                    }
                }
            }
        }

        // 5. Recent Searches Row
        if (searchQuery.isBlank() && recentSearches.isNotEmpty() && !filterState.isFiltering) {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.History, contentDescription = null, tint = Slate500, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Recent Searches", style = MaterialTheme.typography.labelMedium, color = Slate500)
                    }
                    TextButton(
                        onClick = { viewModel.clearRecentSearches() },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Clear", style = MaterialTheme.typography.labelSmall, color = Slate500)
                    }
                }
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(top = 2.dp, bottom = 4.dp)
                ) {
                    items(recentSearches) { term ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable {
                                viewModel.setSearchQuery(term)
                            }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = term,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate700
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove",
                                    tint = Slate400,
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clickable { viewModel.removeRecentSearch(term) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // 5. Results Count Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${filteredEvents.size} Events",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.2).sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
            if (filterState.isFiltering) {
                TextButton(
                    onClick = { viewModel.clearFilters() },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                ) {
                    Text("Reset filters", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // 8. Event List or Improved Empty State
        if (filteredEvents.isEmpty()) {
            val emptyTitle = when {
                searchQuery.isNotBlank() -> "No events found matching \"$searchQuery\""
                filterState.selectedCategories.isNotEmpty() && filterState.selectedDateRange != null ->
                    "No ${filterState.selectedCategories.first().lowercase()} events found ${filterState.selectedDateRange?.lowercase()}."
                filterState.selectedCategories.isNotEmpty() ->
                    "No ${filterState.selectedCategories.joinToString()} events found."
                filterState.discoveryMode == "TODAY" || filterState.selectedDateRange == "Today" ->
                    "No events happening today."
                filterState.discoveryMode == "TOMORROW" || filterState.selectedDateRange == "Tomorrow" ->
                    "No events happening tomorrow."
                filterState.discoveryMode == "STARTING_SOON" ->
                    "No events starting soon."
                filterState.discoveryMode == "SAVED" ->
                    "No saved events found."
                filterState.discoveryMode == "FOLLOWED_CLUBS" ->
                    "No updates from followed clubs yet."
                filterState.odOnly ->
                    "No OD-approved events found matching your current filters."
                filterState.selectedVenue != null ->
                    "No events scheduled at ${filterState.selectedVenue}."
                else -> "No events match your criteria"
            }

            val emptySubtitle = when {
                searchQuery.isNotBlank() -> "Try searching with broader terms or clear your search to explore all campus events."
                filterState.isFiltering -> "Try clearing some filters or explore what different clubs have scheduled."
                else -> "Check back soon for fresh university notices and club announcements."
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(28.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(72.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.SearchOff,
                                contentDescription = null,
                                tint = Slate400,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(18.dp))
                    Text(
                        text = emptyTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = emptySubtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate500,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    // Useful Contextual Action Buttons
                    Column(
                        modifier = Modifier.fillMaxWidth(0.85f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (filterState.isFiltering) {
                            Button(
                                onClick = { viewModel.clearFilters() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("empty_state_clear_filters_button"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Clear filters")
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.clearFilters()
                                viewModel.setSearchQuery("")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("empty_state_view_all_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Event, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("View all events")
                        }

                        OutlinedButton(
                            onClick = { viewModel.setTab(NavigationTab.CLUBS) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("empty_state_explore_clubs_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Explore, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Explore clubs")
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 100.dp, top = 6.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredEvents, key = { it.eventId }) { event ->
                    EventCard(
                        event = event,
                        onClick = { viewModel.selectEvent(event) },
                        onSaveToggle = { viewModel.toggleSaveEvent(event.eventId, event.isSaved) }
                    )
                }
            }
        }
    }

    // Multi-Filter Bottom Sheet
    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            sheetState = sheetState
        ) {
            FilterBottomSheetContent(
                filterState = filterState,
                onToggleCategory = { viewModel.toggleCategoryFilter(it) },
                onToggleOd = { viewModel.toggleOdFilter() },
                onSetDate = { viewModel.setDateFilter(it) },
                onSetVenue = { viewModel.setVenueFilter(it) },
                onClear = { viewModel.clearFilters() },
                onApply = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) showFilterSheet = false
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FilterBottomSheetContent(
    filterState: com.example.ui.viewmodel.FilterState,
    onToggleCategory: (String) -> Unit,
    onToggleOd: () -> Unit,
    onSetDate: (String?) -> Unit,
    onSetVenue: (String?) -> Unit,
    onClear: () -> Unit,
    onApply: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp)
            .padding(bottom = 32.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Filter Events",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = onClear) {
                Text("Clear all", color = RoyalBlue600)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Category Filter
        Text("Event Category", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Technical", "Cultural", "Sports", "Academic", "Workshop", "Hackathon").forEach { cat ->
                FilterChip(
                    selected = filterState.selectedCategories.contains(cat),
                    onClick = { onToggleCategory(cat) },
                    label = { Text(cat) },
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // OD Availability
        Text("Attendance & Duty", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        Row(modifier = Modifier.padding(vertical = 6.dp)) {
            FilterChip(
                selected = filterState.odOnly,
                onClick = onToggleOd,
                label = { Text("✓ OD Available Only") },
                shape = RoundedCornerShape(12.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Date Range Filter
        Text("Date Schedule", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Today", "Tomorrow", "This Week").forEach { range ->
                FilterChip(
                    selected = filterState.selectedDateRange == range,
                    onClick = { onSetDate(if (filterState.selectedDateRange == range) null else range) },
                    label = { Text(range) },
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Venue Filter
        Text("Campus Venue", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val venues = listOf(
                "Main Tech Auditorium",
                "Open Air Amphitheatre",
                "Robotics Lab",
                "Seminar Hall",
                "SAC Courtyard",
                "Sports Stadium"
            )
            venues.forEach { venue ->
                FilterChip(
                    selected = filterState.selectedVenue == venue,
                    onClick = { onSetVenue(if (filterState.selectedVenue == venue) null else venue) },
                    label = { Text(venue) },
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onApply,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("apply_filters_button"),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Apply Filters", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}
