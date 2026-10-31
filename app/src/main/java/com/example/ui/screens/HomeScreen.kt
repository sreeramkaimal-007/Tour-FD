package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.TourEntity
import com.example.ui.components.CreateTourDialog
import com.example.ui.components.parseHexColor
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.TourViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: TourViewModel,
    modifier: Modifier = Modifier
) {
    val tours by viewModel.allTours.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val isSubscribed by viewModel.isSubscribed.collectAsState()
    val subscriptionPlan by viewModel.subscriptionPlan.collectAsState()
    val showSubscriptionPaywall by viewModel.showSubscriptionPaywall.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var showSupabaseDialog by remember { mutableStateOf(false) }
    var tourToDelete by remember { mutableStateOf<TourEntity?>(null) }

    val displayedTours = remember(tours, currentUser) {
        val userLoginId = currentUser?.loginId?.trim()
        val userEmail = currentUser?.email?.trim()
        if (userLoginId.isNullOrBlank() && userEmail.isNullOrBlank()) {
            emptyList()
        } else {
            tours.filter { tour ->
                (userLoginId != null && tour.userId == userLoginId) ||
                (userEmail != null && tour.userId == userEmail)
            }
        }
    }

    Scaffold(
        topBar = {
            var showProfileMenu by remember { mutableStateOf(false) }

            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White,
                            shadowElevation = 2.dp,
                            modifier = Modifier.size(32.dp)
                        ) {
                            androidx.compose.foundation.Image(
                                painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.ic_tour_fd_logo),
                                contentDescription = "Tour FD Logo",
                                contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                                modifier = Modifier.padding(2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Tour FD",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        if (isSubscribed) {
                            Surface(
                                color = MaterialTheme.colorScheme.secondary,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    if (subscriptionPlan == TourViewModel.PLAN_1_YEAR) "1 YEAR PRO" else "1 MONTH PRO",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        } else {
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.clickable { viewModel.triggerPaywall(true) }
                            ) {
                                Text(
                                    "PRO Features",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                },
                actions = {
                    // Profile / Account
                    IconButton(
                        onClick = { showProfileMenu = true },
                        modifier = Modifier.testTag("user_profile_menu_btn")
                    ) {
                        com.example.ui.components.MemberAvatar(
                            name = currentUser?.name ?: "Guest",
                            avatarColorHex = currentUser?.avatarColorHex ?: "#0D5C75",
                            size = 30.dp
                        )
                    }

                    androidx.compose.material3.DropdownMenu(
                        expanded = showProfileMenu,
                        onDismissRequest = { showProfileMenu = false }
                    ) {
                        androidx.compose.material3.DropdownMenuItem(
                            text = {
                                Column {
                                    Text(
                                        currentUser?.name ?: "Guest User",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        currentUser?.email ?: "guest@tourfd.app",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            onClick = {}
                        )
                        androidx.compose.material3.Divider()
                        androidx.compose.material3.DropdownMenuItem(
                            text = { Text("Load Demo Vacation Tour") },
                            leadingIcon = { Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                            onClick = {
                                showProfileMenu = false
                                viewModel.seedDemoTour()
                            }
                        )
                        androidx.compose.material3.DropdownMenuItem(
                            text = { Text("Sign Out / Switch Account", color = Color(0xFFD32F2F)) },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFFD32F2F)) },
                            onClick = {
                                showProfileMenu = false
                                viewModel.logout()
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier.testTag("create_new_tour_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = "New Tour")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("New Tour", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Your Group Tours",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (displayedTours.isEmpty()) {
                        OutlinedButton(
                            onClick = { viewModel.seedDemoTour() },
                            modifier = Modifier.testTag("load_demo_tour_btn")
                        ) {
                            Icon(Icons.Default.CardGiftcard, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Load Demo Tour", fontSize = 12.sp)
                        }
                    }
                }
            }

            // Tours list or empty state
            if (displayedTours.isEmpty()) {
                item {
                    EmptyToursCard(
                        onCreateClick = { showCreateDialog = true },
                        onLoadDemoClick = { viewModel.seedDemoTour() }
                    )
                }
            } else {
                items(displayedTours) { tour ->
                    val isOwner = currentUser != null && (
                        tour.userId == currentUser?.loginId ||
                        tour.userId == currentUser?.email
                    )
                    TourItemCard(
                        tour = tour,
                        isOwner = isOwner,
                        onDelete = { tourToDelete = tour },
                        onClick = {
                            viewModel.navigateTo(AppScreen.TourDetail(tour.id))
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }

    if (showCreateDialog) {
        CreateTourDialog(
            isSubscribed = isSubscribed,
            onDismiss = { showCreateDialog = false },
            onUpgradeRequired = {
                viewModel.triggerPaywall(true)
            },
            onConfirm = { title, dest, start, end, curr, budget, colorHex, members, orgIsFam, orgWeight, orgFamName, orgMembers, orgKids ->
                viewModel.createTour(
                    title = title,
                    destination = dest,
                    startDate = start,
                    endDate = end,
                    currencySymbol = curr,
                    budget = budget,
                    themeColorHex = colorHex,
                    initialMembers = members,
                    organizerIsFamily = orgIsFam,
                    organizerWeight = orgWeight,
                    organizerFamilyName = orgFamName,
                    organizerFamilyMembers = orgMembers,
                    organizerKidsInfo = orgKids
                )
                showCreateDialog = false
            }
        )
    }

    tourToDelete?.let { tour ->
        AlertDialog(
            onDismissRequest = { tourToDelete = null },
            icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFD32F2F)) },
            title = { Text("Delete '${tour.title}'?") },
            text = { Text("All participant records, balances, expenses, and settlements for '${tour.title}' will be permanently deleted. Only you (the profile owner) can delete this tour.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteTour(tour)
                        tourToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text("Delete Tour")
                }
            },
            dismissButton = {
                TextButton(onClick = { tourToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showSubscriptionPaywall) {
        com.example.ui.components.SubscriptionPaywallDialog(
            onDismiss = { viewModel.triggerPaywall(false) },
            onSubscribe = { plan ->
                viewModel.subscribe(plan)
            }
        )
    }

    if (showSupabaseDialog) {
        com.example.ui.components.SupabaseStatusDialog(
            viewModel = viewModel,
            onDismiss = { showSupabaseDialog = false }
        )
    }
}

@Composable
fun TourItemCard(
    tour: TourEntity,
    onClick: () -> Unit,
    isOwner: Boolean = false,
    onDelete: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val themeColor = parseHexColor(tour.themeColorHex)
    val dateFormat = SimpleDateFormat("MMM d", Locale.getDefault())
    val datesStr = "${dateFormat.format(Date(tour.startDate))} - ${dateFormat.format(Date(tour.endDate))}"

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("tour_card_${tour.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Gradient banner with destination and title
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(themeColor, themeColor.copy(alpha = 0.8f))
                        )
                    )
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.align(Alignment.BottomStart)) {
                    Text(
                        tour.title,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            tour.destination,
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 13.sp
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Black.copy(alpha = 0.25f),
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Text(
                        datesStr,
                        color = Color.White,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Body info
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Budget Planned",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "${tour.currencySymbol}${String.format(Locale.getDefault(), "%.0f", tour.budget)}",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (tour.isSettled) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (tour.isSettled) Icons.Default.CheckCircle else Icons.Default.FlightTakeoff,
                                contentDescription = null,
                                tint = if (tour.isSettled) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (tour.isSettled) "Settled" else "Active Tour",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (tour.isSettled) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    if (isOwner && onDelete != null) {
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(32.dp).testTag("delete_tour_btn_${tour.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Tour",
                                tint = Color(0xFFD32F2F),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyToursCard(
    onCreateClick: () -> Unit,
    onLoadDemoClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.FlightTakeoff,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }

            Text(
                "No Group Tours Yet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                "Going on a holiday with your friends and their families? Create a tour to effortlessly track who spent what, split accurately, and settle all debts at the end without confusion!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = onCreateClick) {
                    Text("Plan a Tour")
                }
                OutlinedButton(onClick = onLoadDemoClick) {
                    Text("Try Demo Tour")
                }
            }
        }
    }
}
