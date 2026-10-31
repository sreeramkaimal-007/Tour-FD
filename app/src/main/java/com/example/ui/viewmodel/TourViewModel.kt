package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.ExpenseEntity
import com.example.data.local.MemberEntity
import com.example.data.local.SettlementPaymentEntity
import com.example.data.local.TourDatabase
import com.example.data.local.TourEntity
import com.example.data.remote.SupabaseService
import com.example.data.repository.TourRepository
import com.example.domain.model.DirectSettlementProposal
import com.example.domain.model.ExpenseCategory
import com.example.domain.model.MemberBalance
import com.example.domain.model.SettlementEngine
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID

import com.example.data.local.TourPersistentStorage

data class UserProfile(
    val loginId: String,
    val name: String,
    val email: String,
    val avatarColorHex: String = "#0D5C75"
)

sealed interface AppScreen {
    object WelcomeIntro : AppScreen
    data class Auth(val isRegisterMode: Boolean = false) : AppScreen
    object TourList : AppScreen
    data class TourDetail(val tourId: String, val activeTab: Int = 0) : AppScreen
    object MonetizationHub : AppScreen
}

class TourViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        const val MAX_FREE_FAMILIES = 2
        const val MAX_FREE_MEMBERS = 2
        const val PLAN_1_MONTH = "1_MONTH"
        const val PLAN_1_YEAR = "1_YEAR"
    }

    private val persistentStorage = TourPersistentStorage(application)
    private val repository: TourRepository
    private val supabaseService = SupabaseService()
    private val prefs = application.getSharedPreferences("tour_fd_prefs", Context.MODE_PRIVATE)

    private val _currentUser = MutableStateFlow<UserProfile?>(null)
    val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()

    private val _currentScreen = MutableStateFlow<AppScreen>(AppScreen.WelcomeIntro)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _isSubscribed = MutableStateFlow(false)
    val isSubscribed: StateFlow<Boolean> = _isSubscribed.asStateFlow()

    private val _subscriptionPlan = MutableStateFlow<String?>(null)
    val subscriptionPlan: StateFlow<String?> = _subscriptionPlan.asStateFlow()

    // Dialog state for subscription paywall
    private val _showSubscriptionPaywall = MutableStateFlow(false)
    val showSubscriptionPaywall: StateFlow<Boolean> = _showSubscriptionPaywall.asStateFlow()

    private var pendingMemberAction: (() -> Unit)? = null

    fun setPendingMemberAction(action: (() -> Unit)?) {
        pendingMemberAction = action
    }

    private val _supabaseSyncStatus = MutableStateFlow("Supabase Connected (mcvnyelqcztmffknpwkw)")
    val supabaseSyncStatus: StateFlow<String> = _supabaseSyncStatus.asStateFlow()

    private val _supabaseTestResult = MutableStateFlow<String?>(null)
    val supabaseTestResult: StateFlow<String?> = _supabaseTestResult.asStateFlow()

    init {
        val database = TourDatabase.getDatabase(application)
        repository = TourRepository(database.tourDao(), persistentStorage)

        // Restore local persistent tours immediately
        viewModelScope.launch {
            repository.restoreFromPersistentStorage()
        }

        // Check persistent subscription status
        val savedSub = prefs.getBoolean("is_subscribed", false)
        val savedPlan = prefs.getString("subscription_plan", null)
        _isSubscribed.value = savedSub
        _subscriptionPlan.value = savedPlan

        // Check persistent login session (MUST have loginId)
        val savedLoginId = prefs.getString("user_login_id", null)
        val savedName = prefs.getString("user_name", null)
        val savedEmail = prefs.getString("user_email", null)

        if (savedLoginId != null) {
            _currentUser.value = UserProfile(
                loginId = savedLoginId,
                name = savedName ?: savedLoginId,
                email = savedEmail ?: "$savedLoginId@tourfd.app"
            )
            _currentScreen.value = AppScreen.TourList
            syncCloudTours(savedLoginId)
        } else {
            _currentScreen.value = AppScreen.WelcomeIntro
        }
    }

    val allTours: StateFlow<List<TourEntity>> = repository.getAllTours()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _selectedTourId = MutableStateFlow<String?>(null)
    val selectedTourId: StateFlow<String?> = _selectedTourId.asStateFlow()

    val isProUnlocked: StateFlow<Boolean> = _isSubscribed.asStateFlow()

    private val _selectedCategoryFilter = MutableStateFlow<String?>(null)
    val selectedCategoryFilter: StateFlow<String?> = _selectedCategoryFilter.asStateFlow()

    fun triggerPaywall(show: Boolean) {
        _showSubscriptionPaywall.value = show
    }

    fun subscribe(plan: String) {
        _isSubscribed.value = true
        _subscriptionPlan.value = plan
        _showSubscriptionPaywall.value = false
        prefs.edit()
            .putBoolean("is_subscribed", true)
            .putString("subscription_plan", plan)
            .apply()

        // Execute pending member action if an action was queued during paywall
        val action = pendingMemberAction
        pendingMemberAction = null
        action?.invoke()
    }

    fun cancelSubscription() {
        _isSubscribed.value = false
        _subscriptionPlan.value = null
        prefs.edit()
            .putBoolean("is_subscribed", false)
            .remove("subscription_plan")
            .apply()
    }

    fun toggleProUnlocked() {
        if (_isSubscribed.value) {
            cancelSubscription()
        } else {
            subscribe(PLAN_1_YEAR)
        }
    }

    fun syncCloudTours(userId: String) {
        viewModelScope.launch {
            // Restore from reliable local storage first
            repository.restoreFromPersistentStorage()

            // Clean up any legacy corrupted/blank ghost tours
            try {
                val allToursList = repository.getAllToursList()
                for (t in allToursList) {
                    if (t.userId.isBlank() && (t.title.isBlank() || t.title == "Trip") && t.budget == 0.0) {
                        repository.deleteTour(t)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.w("TourViewModel", "Clean ghost tours note: ${e.message}")
            }

            // Fetch from Supabase
            try {
                val remoteTours = supabaseService.fetchUserTours(userId).getOrNull()
                if (!remoteTours.isNullOrEmpty()) {
                    for (tour in remoteTours) {
                        repository.updateTour(tour)
                        val remoteMembers = supabaseService.fetchMembersForTour(tour.id).getOrNull()
                        if (!remoteMembers.isNullOrEmpty()) {
                            val localMembers = repository.getMembersListByTour(tour.id).associateBy { it.id }
                            for (m in remoteMembers) {
                                val local = localMembers[m.id]
                                if (local == null) {
                                    repository.addMember(
                                        tourId = m.tourId,
                                        name = m.name,
                                        familyNote = m.familyNote,
                                        weight = m.weight,
                                        avatarColorHex = m.avatarColorHex,
                                        familyMembersText = m.familyMembersText,
                                        paymentUpiId = m.paymentUpiId,
                                        paymentLink = m.paymentLink,
                                        memberId = m.id
                                    )
                                } else if (local.name == "Tour Member" && m.name != "Tour Member" && m.name.isNotBlank()) {
                                    repository.updateMember(local.copy(name = m.name, familyMembersText = m.familyMembersText, weight = m.weight, familyNote = m.familyNote))
                                } else if (local.name != "Tour Member" && m.name == "Tour Member") {
                                    supabaseService.syncMember(
                                        memberId = local.id,
                                        tourId = local.tourId,
                                        name = local.name,
                                        familyNote = local.familyNote,
                                        weight = local.weight,
                                        avatarColorHex = local.avatarColorHex,
                                        familyMembersText = local.familyMembersText,
                                        paymentUpiId = local.paymentUpiId,
                                        paymentLink = local.paymentLink
                                    )
                                }
                            }
                        }
                        val remoteExpenses = supabaseService.fetchExpensesForTour(tour.id).getOrNull()
                        if (!remoteExpenses.isNullOrEmpty()) {
                            for (e in remoteExpenses) {
                                repository.addExpense(
                                    tourId = e.tourId,
                                    title = e.title,
                                    amount = e.amount,
                                    date = e.date,
                                    category = e.category,
                                    paidById = e.paidById,
                                    splitType = e.splitType,
                                    splitMemberIds = e.splitMemberIds,
                                    notes = e.notes,
                                    expenseId = e.id
                                )
                            }
                        }
                        val remoteSettlements = supabaseService.fetchSettlementsForTour(tour.id).getOrNull()
                        if (!remoteSettlements.isNullOrEmpty()) {
                            for (s in remoteSettlements) {
                                repository.recordSettlement(
                                    tourId = s.tourId,
                                    fromMemberId = s.fromMemberId,
                                    toMemberId = s.toMemberId,
                                    amount = s.amount,
                                    notes = s.notes,
                                    paymentId = s.id
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.w("TourViewModel", "Sync cloud tours: ${e.message}")
            }
        }
    }

    fun testSupabaseConnection(onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            _supabaseSyncStatus.value = "Testing Supabase connection..."
            val res = supabaseService.testConnection()
            if (res.isSuccess) {
                val msg = res.getOrDefault("Connected to Supabase Project (mcvnyelqcztmffknpwkw)")
                _supabaseTestResult.value = msg
                _supabaseSyncStatus.value = "Online: Supabase Connected"
                onResult(true, msg)
            } else {
                val errorMsg = res.exceptionOrNull()?.message ?: "Connection check failed"
                _supabaseTestResult.value = errorMsg
                _supabaseSyncStatus.value = "Offline Mode (Local Room DB active)"
                onResult(false, errorMsg)
            }
        }
    }

    fun login(loginId: String, password: String): Boolean {
        val cleanId = loginId.trim()
        if (cleanId.isBlank() || password.isBlank()) return false

        val savedAccName = prefs.getString("account_${cleanId}_name", null)
        val savedAccEmail = prefs.getString("account_${cleanId}_email", null)
        val name = savedAccName ?: cleanId.substringBefore("@").replaceFirstChar { it.uppercase() }
        val email = savedAccEmail ?: if (cleanId.contains("@")) cleanId else "$cleanId@tourfd.app"

        val user = UserProfile(
            loginId = cleanId,
            name = name,
            email = email,
            avatarColorHex = "#6A54FA"
        )
        prefs.edit()
            .putString("user_login_id", user.loginId)
            .putString("user_name", user.name)
            .putString("user_email", user.email)
            .commit()

        _currentUser.value = user
        _currentScreen.value = AppScreen.TourList

        // Immediately restore all saved tours from durable persistent storage
        viewModelScope.launch {
            repository.restoreFromPersistentStorage()
        }

        // Sync with local backup and Supabase immediately
        syncCloudTours(cleanId)

        // Sync with Supabase Auth in background
        viewModelScope.launch {
            _supabaseSyncStatus.value = "Authenticating with Supabase..."
            supabaseService.loginUser(cleanId, password)
            _supabaseSyncStatus.value = "Supabase Synced"
        }

        return true
    }

    fun register(loginId: String, name: String, email: String, password: String): Boolean {
        val cleanId = loginId.trim()
        val cleanName = name.trim().ifEmpty { cleanId.replaceFirstChar { it.uppercase() } }
        val cleanEmail = email.trim().ifEmpty { "$cleanId@tourfd.app" }

        if (cleanId.isBlank() || password.isBlank()) return false

        val user = UserProfile(
            loginId = cleanId,
            name = cleanName,
            email = cleanEmail,
            avatarColorHex = "#F5B82E"
        )
        prefs.edit()
            .putString("user_login_id", user.loginId)
            .putString("user_name", user.name)
            .putString("user_email", user.email)
            .putString("account_${cleanId}_name", user.name)
            .putString("account_${cleanId}_email", user.email)
            .commit()

        _currentUser.value = user
        _currentScreen.value = AppScreen.TourList

        // Immediately restore all saved tours from durable persistent storage
        viewModelScope.launch {
            repository.restoreFromPersistentStorage()
        }

        // Sync with local backup and Supabase immediately
        syncCloudTours(cleanId)

        // Register user and save profile to Supabase
        viewModelScope.launch {
            _supabaseSyncStatus.value = "Saving profile to Supabase..."
            val result = supabaseService.registerUser(
                loginId = cleanId,
                name = cleanName,
                email = cleanEmail,
                password = password
            )
            if (result.isSuccess) {
                _supabaseSyncStatus.value = "Account saved to Supabase"
            } else {
                _supabaseSyncStatus.value = "Saved locally (Supabase offline)"
            }
        }

        return true
    }

    fun logout() {
        prefs.edit()
            .remove("user_login_id")
            .remove("user_name")
            .remove("user_email")
            .commit()
        _currentUser.value = null
        _currentScreen.value = AppScreen.WelcomeIntro
    }

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
        if (screen is AppScreen.TourDetail) {
            _selectedTourId.value = screen.tourId
            viewModelScope.launch {
                try {
                    // 1. Check current Room members for this tour
                    var list = repository.getMembersListByTour(screen.tourId)

                    // 2. If Room is empty, restore from persistent storage cache
                    if (list.isEmpty()) {
                        val cachedMembers = persistentStorage?.getMembersForTour(screen.tourId) ?: emptyList()
                        if (cachedMembers.isNotEmpty()) {
                            for (m in cachedMembers) {
                                repository.addMember(
                                    tourId = m.tourId,
                                    name = m.name,
                                    familyNote = m.familyNote,
                                    weight = m.weight,
                                    avatarColorHex = m.avatarColorHex,
                                    familyMembersText = m.familyMembersText,
                                    paymentUpiId = m.paymentUpiId,
                                    paymentLink = m.paymentLink,
                                    memberId = m.id
                                )
                            }
                            list = repository.getMembersListByTour(screen.tourId)
                        }
                    }

                    // 3. Heal any members referenced in expenses that may be missing
                    val tourExpenses = repository.getExpensesListByTour(screen.tourId)
                    val allStorageMembers = persistentStorage?.getAllMembers() ?: emptyList()
                    val missingPayerIds = tourExpenses.map { it.paidById }
                        .filter { id -> id.isNotBlank() && list.none { it.id == id } }
                        .toSet()

                    for (missingId in missingPayerIds) {
                        val recovered = allStorageMembers.find { it.id == missingId }
                        if (recovered != null) {
                            repository.addMember(
                                tourId = screen.tourId,
                                name = recovered.name,
                                familyNote = recovered.familyNote,
                                weight = recovered.weight,
                                avatarColorHex = recovered.avatarColorHex,
                                familyMembersText = recovered.familyMembersText,
                                paymentUpiId = recovered.paymentUpiId,
                                paymentLink = recovered.paymentLink,
                                memberId = recovered.id
                            )
                        } else {
                            val shortTag = if (missingId.length >= 4) missingId.take(4) else missingId
                            repository.addMember(
                                tourId = screen.tourId,
                                name = "Participant $shortTag",
                                familyNote = "Solo Member",
                                weight = 1.0f,
                                avatarColorHex = "#E67E22",
                                familyMembersText = "Participant $shortTag",
                                memberId = missingId
                            )
                        }
                    }

                    // 4. If tour is brand new with zero members and zero expenses, create organizer account
                    list = repository.getMembersListByTour(screen.tourId)
                    if (list.isEmpty() && tourExpenses.isEmpty()) {
                        val myName = _currentUser.value?.name?.ifBlank { "Organizer" } ?: "Organizer"
                        val myMemberId = UUID.randomUUID().toString()
                        repository.addMember(
                            tourId = screen.tourId,
                            name = myName,
                            familyNote = "Organizer Family",
                            weight = 1.0f,
                            avatarColorHex = "#0D5C75",
                            familyMembersText = myName,
                            paymentUpiId = "",
                            paymentLink = "",
                            memberId = myMemberId
                        )
                    }
                } catch (e: Exception) {
                    android.util.Log.w("TourViewModel", "Member recovery note: ${e.message}")
                }
            }
        }
    }

    fun navigateBack() {
        when (_currentScreen.value) {
            is AppScreen.TourDetail, is AppScreen.MonetizationHub -> {
                _currentScreen.value = AppScreen.TourList
            }
            is AppScreen.Auth -> {
                _currentScreen.value = AppScreen.WelcomeIntro
            }
            else -> {}
        }
    }

    fun selectCategoryFilter(catId: String?) {
        _selectedCategoryFilter.value = catId
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedTour: StateFlow<TourEntity?> = _selectedTourId.flatMapLatest { id ->
        if (id == null) flowOf(null) else repository.getTourFlow(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val tourMembers: StateFlow<List<MemberEntity>> = _selectedTourId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else repository.getMembersByTour(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val tourExpenses: StateFlow<List<ExpenseEntity>> = _selectedTourId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else repository.getExpensesByTour(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val tourSettlements: StateFlow<List<SettlementPaymentEntity>> = _selectedTourId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else repository.getSettlementsByTour(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val memberBalances: StateFlow<List<MemberBalance>> = combine(
        tourMembers,
        tourExpenses,
        tourSettlements
    ) { members, expenses, settlements ->
        SettlementEngine.calculateBalances(members, expenses, settlements)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val directSettlements: StateFlow<List<DirectSettlementProposal>> = memberBalances.combine(
        tourMembers
    ) { balances, _ ->
        SettlementEngine.computeDirectSettlements(balances)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalSpent: StateFlow<Double> = tourExpenses.combine(flowOf(Unit)) { expenses, _ ->
        expenses.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val categoryBreakdown: StateFlow<Map<ExpenseCategory, Double>> = tourExpenses.combine(flowOf(Unit)) { expenses, _ ->
        val map = mutableMapOf<ExpenseCategory, Double>()
        for (cat in ExpenseCategory.entries) {
            map[cat] = 0.0
        }
        for (e in expenses) {
            val cat = ExpenseCategory.fromId(e.category)
            map[cat] = (map[cat] ?: 0.0) + e.amount
        }
        map
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    fun createTour(
        title: String,
        destination: String,
        startDate: Long,
        endDate: Long,
        currencySymbol: String,
        budget: Double,
        themeColorHex: String,
        initialMembers: List<Pair<String, String>>,
        organizerIsFamily: Boolean = false,
        organizerWeight: Float = 1.0f,
        organizerFamilyName: String = "",
        organizerFamilyMembers: String = "",
        organizerKidsInfo: String = ""
    ) {
        viewModelScope.launch {
            val currentUserId = _currentUser.value?.loginId?.ifBlank { _currentUser.value?.email } 
                ?: _currentUser.value?.email ?: ""
            val tourId = repository.createTour(
                title = title.ifBlank { "Vacation Tour" },
                destination = destination.ifBlank { "Holiday Trip" },
                startDate = startDate,
                endDate = endDate,
                currencySymbol = currencySymbol,
                budget = budget,
                themeColorHex = themeColorHex,
                userId = currentUserId
            )

            // Sync Tour to Supabase
            supabaseService.syncTour(
                tourId = tourId,
                userId = currentUserId,
                title = title.ifBlank { "Vacation Tour" },
                destination = destination.ifBlank { "Holiday Trip" },
                startDate = startDate,
                endDate = endDate,
                currencySymbol = currencySymbol,
                budget = budget,
                themeColorHex = themeColorHex,
                isSettled = false
            )

            // Current user as Organizer: either Solo Member or Family
            val myRawName = _currentUser.value?.name?.ifBlank { "Organizer" } ?: "Organizer"
            val myName = if (organizerIsFamily) {
                organizerFamilyName.ifBlank { "$myRawName's Family" }
            } else {
                myRawName
            }
            val myMemberId = UUID.randomUUID().toString()
            val myFamilyNote = if (organizerIsFamily) {
                if (organizerKidsInfo.isNotBlank()) "Family ($organizerKidsInfo)" else "Family Account"
            } else {
                "Solo Member"
            }
            val myWeight = if (organizerIsFamily && organizerWeight > 0f) organizerWeight else 1.0f
            val myMembers = organizerFamilyMembers.ifBlank { myRawName }

            repository.addMember(
                tourId = tourId,
                name = myName,
                familyNote = myFamilyNote,
                weight = myWeight,
                avatarColorHex = "#0D5C75",
                familyMembersText = myMembers,
                paymentUpiId = "",
                paymentLink = "",
                memberId = myMemberId
            )
            supabaseService.syncMember(
                memberId = myMemberId,
                tourId = tourId,
                name = myName,
                familyNote = myFamilyNote,
                weight = myWeight,
                avatarColorHex = "#0D5C75",
                familyMembersText = myMembers,
                paymentUpiId = "",
                paymentLink = ""
            )

            // Save all companion accounts joining this tour
            val allowedExtraFamilies = initialMembers
            val colors = listOf("#E67E22", "#9C27B0", "#2E7D32", "#00838F", "#D81B60", "#F57C00")

            allowedExtraFamilies.forEachIndexed { idx, pair ->
                if (pair.first.isNotBlank()) {
                    val color = colors[idx % colors.size]
                    val parsedWeight = Regex("weight:([0-9.]+)").find(pair.second)?.groupValues?.getOrNull(1)?.toFloatOrNull()
                    val isFamily = pair.second.contains("Family", ignoreCase = true) || pair.second.contains("Couple", ignoreCase = true)
                    val weight = parsedWeight ?: when {
                        pair.second.contains("Couple", ignoreCase = true) -> 2.0f
                        pair.second.contains("3", ignoreCase = true) -> 2.5f
                        isFamily -> 2.0f
                        else -> 1.0f
                    }

                    val extraMemberId = UUID.randomUUID().toString()
                    val namesInParens = Regex("\\((.*?)\\)").find(pair.second)?.groupValues?.getOrNull(1)
                    val membersText = if (!namesInParens.isNullOrBlank()) namesInParens.trim() else pair.first.trim()
                    val cleanNote = pair.second.replace(Regex(",?\\s*weight:[0-9.]+"), "").ifBlank { "Family Group" }

                    repository.addMember(
                        tourId = tourId,
                        name = pair.first.trim(),
                        familyNote = cleanNote,
                        weight = weight,
                        avatarColorHex = color,
                        familyMembersText = membersText,
                        memberId = extraMemberId
                    )
                    supabaseService.syncMember(
                        memberId = extraMemberId,
                        tourId = tourId,
                        name = pair.first.trim(),
                        familyNote = cleanNote,
                        weight = weight,
                        avatarColorHex = color,
                        familyMembersText = membersText
                    )
                }
            }
            _selectedTourId.value = tourId
            _currentScreen.value = AppScreen.TourDetail(tourId, activeTab = 0)
        }
    }

    /**
     * Adds a new Family / Group Account.
     * Always persists member to Room DB, local persistent storage, and Supabase.
     */
    fun addFamilyAccount(
        tourId: String,
        name: String,
        familyNote: String,
        weight: Float,
        avatarColorHex: String,
        familyMembersText: String = "",
        paymentUpiId: String = "",
        paymentLink: String = "",
        onLimitReached: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val memberId = UUID.randomUUID().toString()
            val mName = name.ifBlank { "Family Account" }
            val mFamily = familyNote.ifBlank { "1 Adult" }
            val mWeight = if (weight <= 0f) 1.0f else weight

            repository.addMember(
                tourId = tourId,
                name = mName,
                familyNote = mFamily,
                weight = mWeight,
                avatarColorHex = avatarColorHex,
                familyMembersText = familyMembersText,
                paymentUpiId = paymentUpiId.trim(),
                paymentLink = paymentLink.trim(),
                memberId = memberId
            )

            supabaseService.syncMember(
                memberId = memberId,
                tourId = tourId,
                name = mName,
                familyNote = mFamily,
                weight = mWeight,
                avatarColorHex = avatarColorHex,
                familyMembersText = familyMembersText,
                paymentUpiId = paymentUpiId.trim(),
                paymentLink = paymentLink.trim()
            )
        }
    }

    fun addMember(
        tourId: String,
        name: String,
        familyNote: String,
        weight: Float,
        avatarColorHex: String,
        onLimitReached: () -> Unit
    ) {
        addFamilyAccount(
            tourId = tourId,
            name = name,
            familyNote = familyNote,
            weight = weight,
            avatarColorHex = avatarColorHex,
            familyMembersText = "",
            paymentUpiId = "",
            paymentLink = "",
            onLimitReached = onLimitReached
        )
    }

    /**
     * Updates family members and weighting inside an existing family account.
     * Adding/editing family members inside an existing family account is 100% free!
     */
    fun updateFamilyAccount(
        member: MemberEntity,
        name: String,
        familyNote: String,
        weight: Float,
        familyMembersText: String,
        paymentUpiId: String,
        paymentLink: String
    ) {
        viewModelScope.launch {
            val cleanName = name.trim().ifBlank { member.name }
            val cleanFamilyNote = familyNote.trim().ifBlank { member.familyNote }
            val cleanWeight = if (weight <= 0f) 1.0f else weight
            val cleanMembersText = familyMembersText.trim().let {
                if (it.isBlank() || it == "Tour Member") cleanName else it
            }
            val cleanUpi = paymentUpiId.trim()
            val cleanLink = paymentLink.trim()

            val updated = member.copy(
                name = cleanName,
                familyNote = cleanFamilyNote,
                weight = cleanWeight,
                familyMembersText = cleanMembersText,
                paymentUpiId = cleanUpi,
                paymentLink = cleanLink
            )
            repository.updateMember(updated)
            persistentStorage?.saveMember(updated)
            supabaseService.syncMember(
                memberId = updated.id,
                tourId = updated.tourId,
                name = updated.name,
                familyNote = updated.familyNote,
                weight = updated.weight,
                avatarColorHex = updated.avatarColorHex,
                familyMembersText = updated.familyMembersText,
                paymentUpiId = updated.paymentUpiId,
                paymentLink = updated.paymentLink
            )
        }
    }

    /**
     * Launches a direct UPI payment (Google Pay, PhonePe, Paytm, BHIM)
     */
    fun openUpiPayment(
        context: Context,
        payeeUpiId: String,
        payeeName: String,
        amount: Double,
        currencySymbol: String,
        tourTitle: String
    ): Boolean {
        if (payeeUpiId.isBlank()) return false
        return try {
            val note = "YIGRO: $tourTitle settlement"
            val uri = Uri.parse(
                "upi://pay?pa=$payeeUpiId&pn=${Uri.encode(payeeName)}&am=${String.format(Locale.US, "%.2f", amount)}&cu=INR&tn=${Uri.encode(note)}"
            )
            val intent = Intent(Intent.ACTION_VIEW, uri)
            context.startActivity(intent)
            true
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Launches PayPal / Stripe or web payment link
     */
    fun openPaymentLink(
        context: Context,
        rawLinkOrUser: String,
        amount: Double
    ): Boolean {
        if (rawLinkOrUser.isBlank()) return false
        return try {
            val clean = rawLinkOrUser.trim()
            val url = when {
                clean.startsWith("http://") || clean.startsWith("https://") -> {
                    if (clean.contains("paypal.me") && !clean.endsWith("/")) {
                        "$clean/${String.format(Locale.US, "%.2f", amount)}"
                    } else clean
                }
                clean.contains("paypal.me") -> "https://$clean/${String.format(Locale.US, "%.2f", amount)}"
                clean.startsWith("@") -> "https://paypal.me/${clean.removePrefix("@")}/${String.format(Locale.US, "%.2f", amount)}"
                else -> "https://paypal.me/$clean/${String.format(Locale.US, "%.2f", amount)}"
            }
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
            true
        } catch (_: Exception) {
            false
        }
    }

    fun deleteMember(member: MemberEntity) {
        viewModelScope.launch {
            repository.deleteMember(member)
            supabaseService.deleteMember(member.id)
        }
    }

    fun addExpense(
        tourId: String,
        title: String,
        amount: Double,
        date: Long,
        category: String,
        paidById: String,
        splitType: String,
        splitMemberIds: List<String>,
        notes: String
    ) {
        viewModelScope.launch {
            val expenseId = UUID.randomUUID().toString()
            val expTitle = title.ifBlank { "Expense" }
            val splitIdsStr = splitMemberIds.joinToString(",")

            repository.addExpense(
                tourId = tourId,
                title = expTitle,
                amount = amount,
                date = date,
                category = category,
                paidById = paidById,
                splitType = splitType,
                splitMemberIds = splitIdsStr,
                notes = notes,
                expenseId = expenseId
            )

            supabaseService.syncExpense(
                expenseId = expenseId,
                tourId = tourId,
                title = expTitle,
                amount = amount,
                date = date,
                category = category,
                paidById = paidById,
                splitType = splitType,
                splitMemberIds = splitIdsStr,
                notes = notes
            )
        }
    }

    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
        }
    }

    fun recordSettlement(
        tourId: String,
        fromMemberId: String,
        toMemberId: String,
        amount: Double,
        notes: String = "Paid via cash/app"
    ) {
        viewModelScope.launch {
            val paymentId = UUID.randomUUID().toString()
            repository.recordSettlement(
                tourId = tourId,
                fromMemberId = fromMemberId,
                toMemberId = toMemberId,
                amount = amount,
                notes = notes,
                paymentId = paymentId
            )

            supabaseService.syncSettlement(
                id = paymentId,
                tourId = tourId,
                fromMemberId = fromMemberId,
                toMemberId = toMemberId,
                amount = amount,
                date = System.currentTimeMillis(),
                notes = notes
            )
        }
    }

    fun deleteSettlement(settlement: SettlementPaymentEntity) {
        viewModelScope.launch {
            repository.deleteSettlement(settlement)
        }
    }

    fun deleteTour(tour: TourEntity) {
        viewModelScope.launch {
            repository.deleteTour(tour)
            supabaseService.deleteTour(tour.id)
            if (_selectedTourId.value == tour.id) {
                _selectedTourId.value = null
                _currentScreen.value = AppScreen.TourList
            }
        }
    }

    fun seedDemoTour() {
        viewModelScope.launch {
            val uid = _currentUser.value?.loginId ?: _currentUser.value?.email ?: ""
            repository.seedSampleFamilyTour(userId = uid, isSubscribed = _isSubscribed.value)
        }
    }

    fun getShareableReportText(): String {
        val tour = selectedTour.value ?: return ""
        val balances = memberBalances.value
        val settlements = directSettlements.value
        val total = totalSpent.value
        return SettlementEngine.generateTourReport(
            tourTitle = tour.title,
            currencySymbol = tour.currencySymbol,
            balances = balances,
            settlements = settlements,
            totalSpent = total
        )
    }
}
