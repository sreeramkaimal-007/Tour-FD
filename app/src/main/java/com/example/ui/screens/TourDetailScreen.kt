package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ExpenseEntity
import com.example.data.local.MemberEntity
import com.example.data.local.SettlementPaymentEntity
import com.example.domain.model.DirectSettlementProposal
import com.example.domain.model.ExpenseCategory
import com.example.domain.model.MemberBalance
import com.example.domain.model.SplitType
import com.example.ui.components.AddExpenseDialog
import com.example.ui.components.AddFamilyDialog
import com.example.ui.components.AddMemberDialog
import com.example.ui.components.CategoryIconBadge
import com.example.ui.components.ManageFamilyMembersDialog
import com.example.ui.components.MemberAvatar
import com.example.ui.components.RecordSettlementDialog
import com.example.ui.components.ViewTourMembersDialog
import com.example.ui.components.parseHexColor
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.TourViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TourDetailScreen(
    tourId: String,
    viewModel: TourViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val tour by viewModel.selectedTour.collectAsState()
    val members by viewModel.tourMembers.collectAsState()
    val expenses by viewModel.tourExpenses.collectAsState()
    val settlements by viewModel.tourSettlements.collectAsState()
    val balances by viewModel.memberBalances.collectAsState()
    val directSettlements by viewModel.directSettlements.collectAsState()
    val totalSpent by viewModel.totalSpent.collectAsState()
    val categoryBreakdown by viewModel.categoryBreakdown.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val isSubscribed by viewModel.isSubscribed.collectAsState()
    val showSubscriptionPaywall by viewModel.showSubscriptionPaywall.collectAsState()
    val selectedCategoryFilter by viewModel.selectedCategoryFilter.collectAsState()

    var activeTab by remember { mutableIntStateOf(0) }
    var showAddExpenseDialog by remember { mutableStateOf(false) }
    var showAddMemberDialog by remember { mutableStateOf(false) }
    var showAllMembersDialog by remember { mutableStateOf(false) }
    var settlementToRecord by remember { mutableStateOf<DirectSettlementProposal?>(null) }
    var memberToManage by remember { mutableStateOf<MemberEntity?>(null) }
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showProExportDialog by remember { mutableStateOf(false) }
    var showSupabaseDialog by remember { mutableStateOf(false) }
    var showAlgorithmInfoDialog by remember { mutableStateOf(false) }
    var memberToDeleteConfirm by remember { mutableStateOf<MemberEntity?>(null) }

    // Intercept hardware and gesture back navigation
    BackHandler {
        viewModel.navigateBack()
    }

    if (tour == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Tour not found")
        }
        return
    }

    val currentTour = tour!!
    val isProfileOwner = currentUser != null && (
        currentTour.userId == currentUser?.loginId ||
        currentTour.userId == currentUser?.email
    )
    val currency = currentTour.currencySymbol
    val themeColor = parseHexColor(currentTour.themeColorHex)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            currentTour.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            maxLines = 1
                        )
                        Text(
                            currentTour.destination,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateBack() },
                        modifier = Modifier.testTag("tour_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val report = viewModel.getShareableReportText()
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("YIGRO Report", report))
                            Toast.makeText(context, "Settlement summary copied to clipboard! Ready to share.", Toast.LENGTH_LONG).show()
                        },
                        modifier = Modifier.testTag("share_summary_btn")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share Report", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("How Settlements Work") },
                            leadingIcon = { Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                            onClick = {
                                showMenu = false
                                showAlgorithmInfoDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Export PDF / Excel Report") },
                            leadingIcon = { Icon(Icons.Default.PictureAsPdf, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                showProExportDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Add Friend / Family") },
                            leadingIcon = { Icon(Icons.Default.GroupAdd, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                showAddMemberDialog = true
                            }
                        )
                        if (isProfileOwner) {
                            DropdownMenuItem(
                                text = { Text("Delete Tour", color = Color(0xFFD32F2F)) },
                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFD32F2F)) },
                                onClick = {
                                    showMenu = false
                                    showDeleteConfirm = true
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            if (activeTab == 0) {
                FloatingActionButton(
                    onClick = { showAddExpenseDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("add_expense_fab")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Expense")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Expense", fontWeight = FontWeight.Bold)
                    }
                }
            } else if (activeTab == 1) {
                FloatingActionButton(
                    onClick = {
                        showAddMemberDialog = true
                    },
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("add_member_fab")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.GroupAdd, contentDescription = "Add Family")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Family", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header stats banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Total Spent",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "$currency${String.format(Locale.getDefault(), "%.2f", totalSpent)}",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Surface(
                                onClick = { showAllMembersDialog = true },
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Group, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        "${members.size} Accounts • View All",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            if (currentTour.budget > 0) {
                                val remaining = currentTour.budget - totalSpent
                                Text(
                                    text = if (remaining >= 0) "Budget left: $currency${String.format(Locale.getDefault(), "%.0f", remaining)}"
                                    else "Over budget by $currency${String.format(Locale.getDefault(), "%.0f", abs(remaining))}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (remaining >= 0) Color(0xFF2E7D32) else Color(0xFFD32F2F),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    if (currentTour.budget > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        val progress = (totalSpent / currentTour.budget).coerceIn(0.0, 1.0).toFloat()
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = if (totalSpent > currentTour.budget) Color(0xFFD32F2F) else MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surface
                        )
                    }
                }
            }

            // Tabs
            TabRow(
                selectedTabIndex = activeTab,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = { Text("Expenses (${expenses.size})", fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = { Text("Balances & Settle", fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = activeTab == 2,
                    onClick = { activeTab = 2 },
                    text = { Text("Analytics", fontWeight = FontWeight.SemiBold) }
                )
            }

            // Tab Content
            when (activeTab) {
                0 -> ExpensesTabContent(
                    expenses = expenses,
                    members = members,
                    currencySymbol = currency,
                    selectedFilter = selectedCategoryFilter,
                    onSelectFilter = { viewModel.selectCategoryFilter(it) },
                    onDeleteExpense = { viewModel.deleteExpense(it) },
                    onAddExpenseClick = { showAddExpenseDialog = true }
                )
                1 -> BalancesTabContent(
                    balances = balances,
                    members = members,
                    directSettlements = directSettlements,
                    settlements = settlements,
                    currencySymbol = currency,
                    isSubscribed = isSubscribed,
                    onUpgradeRequired = { viewModel.triggerPaywall(true) },
                    onRecordSettlementClick = { settlementToRecord = it },
                    onDeleteSettlement = { viewModel.deleteSettlement(it) },
                    onAddMemberClick = {
                        showAddMemberDialog = true
                    },
                    onViewAllMembersClick = { showAllMembersDialog = true },
                    onEditMemberClick = { memberToManage = it },
                    onShareSummary = {
                        val report = viewModel.getShareableReportText()
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("YIGRO Report", report))
                        Toast.makeText(context, "Settlement text copied! Paste it in your WhatsApp group chat.", Toast.LENGTH_LONG).show()
                    }
                )
                2 -> AnalyticsTabContent(
                    totalSpent = totalSpent,
                    budget = currentTour.budget,
                    breakdown = categoryBreakdown,
                    balances = balances,
                    currencySymbol = currency
                )
            }
        }
    }

    if (showAllMembersDialog) {
        ViewTourMembersDialog(
            tourTitle = currentTour.title,
            members = members,
            onDismiss = { showAllMembersDialog = false },
            onManageMember = { memberToManage = it }
        )
    }

    if (showAddExpenseDialog) {
        AddExpenseDialog(
            members = members,
            currencySymbol = currency,
            onDismiss = { showAddExpenseDialog = false },
            onQuickAddMember = { name ->
                viewModel.addFamilyAccount(
                    tourId = currentTour.id,
                    name = name,
                    familyNote = "Tour Member",
                    weight = 1.0f,
                    avatarColorHex = "#0D5C75",
                    familyMembersText = name,
                    paymentUpiId = "",
                    paymentLink = "",
                    onLimitReached = { viewModel.triggerPaywall(true) }
                )
            },
            onOpenAddMember = {
                showAddExpenseDialog = false
                showAddMemberDialog = true
            },
            onConfirm = { title, amt, cat, payer, splitType, splitIds, notes ->
                viewModel.addExpense(
                    tourId = currentTour.id,
                    title = title,
                    amount = amt,
                    date = System.currentTimeMillis(),
                    category = cat,
                    paidById = payer,
                    splitType = splitType,
                    splitMemberIds = splitIds,
                    notes = notes
                )
                showAddExpenseDialog = false
            }
        )
    }

    if (showAddMemberDialog) {
        AddFamilyDialog(
            onDismiss = { showAddMemberDialog = false },
            onConfirm = { name, note, weight, colorHex, membersText ->
                viewModel.addFamilyAccount(
                    tourId = currentTour.id,
                    name = name,
                    familyNote = note,
                    weight = weight,
                    avatarColorHex = colorHex,
                    familyMembersText = membersText
                )
                showAddMemberDialog = false
            }
        )
    }

    memberToManage?.let { member ->
        ManageFamilyMembersDialog(
            member = member,
            onDismiss = { memberToManage = null },
            onSave = { name, note, weight, membersText ->
                viewModel.updateFamilyAccount(
                    member = member,
                    name = name,
                    familyNote = note,
                    weight = weight,
                    familyMembersText = membersText,
                    paymentUpiId = "",
                    paymentLink = ""
                )
                memberToManage = null
                Toast.makeText(context, "Account updated! Member names permanently saved.", Toast.LENGTH_SHORT).show()
            },
            onDelete = if (isProfileOwner) {
                { memberToDeleteConfirm = member }
            } else null
        )
    }

    memberToDeleteConfirm?.let { m ->
        AlertDialog(
            onDismissRequest = { memberToDeleteConfirm = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = Color(0xFFD32F2F)
                )
            },
            title = {
                Text("Delete ${m.name}?", fontWeight = FontWeight.Bold)
            },
            text = {
                Text("Are you sure you want to remove ${m.name} from this tour? All associated expenses and debt calculations will be updated. Only the profile owner can remove tour members.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteMember(m)
                        memberToDeleteConfirm = null
                        memberToManage = null
                        Toast.makeText(context, "${m.name} removed by profile owner", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text("Delete Account", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { memberToDeleteConfirm = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    settlementToRecord?.let { proposal ->
        RecordSettlementDialog(
            fromName = proposal.fromMemberName,
            toName = proposal.toMemberName,
            suggestedAmount = proposal.amount,
            currencySymbol = currency,
            tourTitle = currentTour.title,
            onDismiss = { settlementToRecord = null },
            onConfirm = { amt, note ->
                viewModel.recordSettlement(
                    tourId = currentTour.id,
                    fromMemberId = proposal.fromMemberId,
                    toMemberId = proposal.toMemberId,
                    amount = amt,
                    notes = note
                )
                settlementToRecord = null
                Toast.makeText(context, "Payment recorded! Balances recalculated.", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete this tour?") },
            text = { Text("All expenses, participants, and settlement records for '${currentTour.title}' will be permanently deleted.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteTour(currentTour)
                        showDeleteConfirm = false
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
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
                Toast.makeText(context, "Subscription activated! You can now add unlimited members.", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (showProExportDialog) {
        AlertDialog(
            onDismissRequest = { showProExportDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export Financial Report", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("YIGRO Pro allows you to export itemized PDF and Excel reports with receipt attachments and company tax summaries.")
                    if (isSubscribed) {
                        Surface(
                            color = Color(0xFFE8F5E9),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text(
                                "✅ YIGRO Subscription Active! PDF Summary generated successfully to device storage.",
                                color = Color(0xFF2E7D32),
                                fontSize = 13.sp,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    } else {
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text(
                                "⭐ Available with YIGRO Subscription (1-Month or 1-Year Plan). You can subscribe from the members tab or owner hub!",
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showProExportDialog = false
                        if (!isSubscribed) {
                            viewModel.triggerPaywall(true)
                        } else {
                            Toast.makeText(context, "Report exported to Documents/YIGRO_${currentTour.title}.pdf", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text(if (isSubscribed) "Download PDF" else "View Subscription")
                }
            },
            dismissButton = {
                TextButton(onClick = { showProExportDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    if (showSupabaseDialog) {
        com.example.ui.components.SupabaseStatusDialog(
            viewModel = viewModel,
            onDismiss = { showSupabaseDialog = false }
        )
    }

    if (showAlgorithmInfoDialog) {
        AlertDialog(
            onDismissRequest = { showAlgorithmInfoDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("How YIGRO Settle Works", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "YIGRO uses two core mathematical models to make group travel expenses effortless and fair:",
                        fontSize = 13.sp
                    )
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("1. Headcount & Family Weighting", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                            Text(
                                "Solo travelers have 1.0x weight, couples 2.0x, and families with kids (e.g. 2.5x). When splitting a hotel or dinner by weight, the bill distributes proportionally without forcing you to create fake individual accounts for children.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("2. Greedy Debt Minimizer", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary)
                            Text(
                                "Instead of a messy web of dozens of individual paybacks, we compute each member's net position (Total Paid minus Fair Share) and match the biggest debtors to the biggest creditors. This settles the entire trip in the minimum possible transfers!",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showAlgorithmInfoDialog = false }) {
                    Text("Got It")
                }
            }
        )
    }
}

@Composable
fun ExpensesTabContent(
    expenses: List<ExpenseEntity>,
    members: List<MemberEntity>,
    currencySymbol: String,
    selectedFilter: String?,
    onSelectFilter: (String?) -> Unit,
    onDeleteExpense: (ExpenseEntity) -> Unit,
    onAddExpenseClick: () -> Unit
) {
    val memberMap = members.associateBy { it.id }
    val filteredExpenses = if (selectedFilter == null) expenses else expenses.filter { it.category == selectedFilter }
    val dateFormat = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())

    Column(modifier = Modifier.fillMaxSize()) {
        // Category Filter Chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                val isAll = selectedFilter == null
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isAll) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.clickable { onSelectFilter(null) }
                ) {
                    Text(
                        "All Categories",
                        color = if (isAll) Color.White else MaterialTheme.colorScheme.onSurface,
                        fontWeight = if (isAll) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
            items(ExpenseCategory.entries) { cat ->
                val isSelected = selectedFilter == cat.id
                val catColor = parseHexColor(cat.colorHex)
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) catColor else catColor.copy(alpha = 0.12f),
                    modifier = Modifier.clickable { onSelectFilter(cat.id) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = com.example.ui.components.getCategoryIcon(cat),
                            contentDescription = null,
                            tint = if (isSelected) Color.White else catColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            cat.displayName,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        if (filteredExpenses.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "No Expenses Recorded",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Track dinner bills, hotel stays, fuel, or activity passes with accurate split calculations.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Button(onClick = onAddExpenseClick) {
                        Text("Add First Expense")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredExpenses, key = { it.id }) { expense ->
                    val cat = ExpenseCategory.fromId(expense.category)
                    val payer = memberMap[expense.paidById]
                    val splitDesc = when (expense.splitType) {
                        SplitType.BY_WEIGHT.id -> "By Family Weight"
                        SplitType.CUSTOM.id -> "Custom Split"
                        else -> "Split Equally"
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CategoryIconBadge(category = cat, size = 44.dp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    expense.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        "Paid by ",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        payer?.name ?: "Someone",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        " • $splitDesc",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (expense.notes.isNotBlank()) {
                                    Text(
                                        expense.notes,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    "$currencySymbol${String.format(Locale.getDefault(), "%.2f", expense.amount)}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                IconButton(
                                    onClick = { onDeleteExpense(expense) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BalancesTabContent(
    balances: List<MemberBalance>,
    members: List<MemberEntity>,
    directSettlements: List<DirectSettlementProposal>,
    settlements: List<SettlementPaymentEntity>,
    currencySymbol: String,
    isSubscribed: Boolean,
    onUpgradeRequired: () -> Unit,
    onRecordSettlementClick: (DirectSettlementProposal) -> Unit,
    onDeleteSettlement: (SettlementPaymentEntity) -> Unit,
    onAddMemberClick: () -> Unit,
    onViewAllMembersClick: () -> Unit,
    onEditMemberClick: (MemberEntity) -> Unit,
    onShareSummary: () -> Unit
) {
    val memberMap = balances.associateBy { it.memberId }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Direct Settlement Plan Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Payment, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Debt Minimizer Settlement Plan",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                        Button(
                            onClick = onShareSummary,
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy WhatsApp", fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (directSettlements.isEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFE8F5E9), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Everything is balanced! No pending payments required.",
                                color = Color(0xFF2E7D32),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    } else {
                        Text(
                            "Instead of complicated back-and-forth transfers, settle everything with these direct payments:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        directSettlements.forEach { item ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(item.fromMemberName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Icon(Icons.Default.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(item.toMemberName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        }
                                        Text(
                                            "Pays $currencySymbol${String.format(Locale.getDefault(), "%.2f", item.amount)}",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 15.sp,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }

                                    Button(
                                        onClick = { onRecordSettlementClick(item) },
                                        shape = RoundedCornerShape(12.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text("Mark Paid", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Participant Balances Section
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Tour Members & Families",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "${balances.size} Tour Members & Families",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedButton(
                            onClick = onViewAllMembersClick,
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.People, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Who's in?", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = onAddMemberClick,
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Family", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        items(balances) { b ->
            val memberEntity = members.find { it.id == b.memberId }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (memberEntity != null) onEditMemberClick(memberEntity)
                    },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MemberAvatar(
                        name = b.memberName,
                        avatarColorHex = b.avatarColorHex,
                        size = 42.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(b.memberName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            if (memberEntity != null) {
                                Spacer(modifier = Modifier.width(6.dp))
                                IconButton(
                                    onClick = { onEditMemberClick(memberEntity) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = "Manage Family Members",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            "${b.familyNote} • Weight: ${String.format(Locale.getDefault(), "%.1f", b.weight)}x",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Clear interactive chip showing who are the members inside this family
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .clickable {
                                    if (memberEntity != null) onEditMemberClick(memberEntity)
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    if (memberEntity != null && memberEntity.familyMembersText.isNotBlank())
                                        "Members: ${memberEntity.familyMembersText} ➔ Details"
                                    else "Solo: ${b.memberName} ➔ Details",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            }
                        }



                        Row(modifier = Modifier.padding(top = 2.dp)) {
                            Text(
                                "Paid $currencySymbol${String.format(Locale.getDefault(), "%.0f", b.totalPaid)}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text("  •  ", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                "Share $currencySymbol${String.format(Locale.getDefault(), "%.0f", b.totalShare)}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Balance Badge
                    val isPositive = b.netBalance > 0.01
                    val isNegative = b.netBalance < -0.01
                    val badgeBg = when {
                        isPositive -> Color(0xFFE8F5E9)
                        isNegative -> Color(0xFFFFEBEE)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                    val badgeColor = when {
                        isPositive -> Color(0xFF2E7D32)
                        isNegative -> Color(0xFFD32F2F)
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                    val badgeText = when {
                        isPositive -> "+$currencySymbol${String.format(Locale.getDefault(), "%.2f", b.netBalance)}"
                        isNegative -> "-$currencySymbol${String.format(Locale.getDefault(), "%.2f", abs(b.netBalance))}"
                        else -> "Settled"
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = badgeBg
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                badgeText,
                                color = badgeColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                if (isPositive) "Gets back" else if (isNegative) "Owes" else "Balanced",
                                color = badgeColor.copy(alpha = 0.8f),
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }

        // Settlement Payment Records
        if (settlements.isNotEmpty()) {
            item {
                Text(
                    "Recorded Settlement Payments (${settlements.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(settlements) { s ->
                val fromName = memberMap[s.fromMemberId]?.memberName ?: "Unknown"
                val toName = memberMap[s.toMemberId]?.memberName ?: "Unknown"
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "$fromName paid $toName $currencySymbol${String.format(Locale.getDefault(), "%.2f", s.amount)}",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                            if (s.notes.isNotBlank()) {
                                Text(s.notes, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        IconButton(
                            onClick = { onDeleteSettlement(s) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Undo", tint = Color.Gray, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AnalyticsTabContent(
    totalSpent: Double,
    budget: Double,
    breakdown: Map<ExpenseCategory, Double>,
    balances: List<MemberBalance>,
    currencySymbol: String
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Spending by Category
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Spending by Category",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    breakdown.entries.filter { it.value > 0 }.sortedByDescending { it.value }.forEach { (cat, amount) ->
                        val percent = if (totalSpent > 0) (amount / totalSpent).toFloat() else 0f
                        val catColor = parseHexColor(cat.colorHex)

                        Column(modifier = Modifier.padding(vertical = 6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = com.example.ui.components.getCategoryIcon(cat),
                                        contentDescription = null,
                                        tint = catColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(cat.displayName, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                                }
                                Text(
                                    "$currencySymbol${String.format(Locale.getDefault(), "%.2f", amount)} (${String.format(Locale.getDefault(), "%.0f", percent * 100)}%)",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { percent },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = catColor,
                                trackColor = catColor.copy(alpha = 0.15f)
                            )
                        }
                    }

                    if (totalSpent <= 0.0) {
                        Text(
                            "No expenses logged to generate breakdown.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Member Expense Share Distribution
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Member Paid Upfront vs Share",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    balances.forEach { b ->
                        Column(modifier = Modifier.padding(vertical = 6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(b.memberName, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text(
                                    "Paid $currencySymbol${String.format(Locale.getDefault(), "%.0f", b.totalPaid)} | Share $currencySymbol${String.format(Locale.getDefault(), "%.0f", b.totalShare)}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            // Double progress bar: Paid vs Share
                            val maxAmount = balances.maxOfOrNull { maxOf(it.totalPaid, it.totalShare) } ?: 1.0
                            val safeMax = if (maxAmount > 0) maxAmount else 1.0

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth((b.totalPaid / safeMax).coerceIn(0.0, 1.0).toFloat())
                                        .background(parseHexColor(b.avatarColorHex))
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
