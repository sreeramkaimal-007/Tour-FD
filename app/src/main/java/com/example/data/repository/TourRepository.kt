package com.example.data.repository

import com.example.data.local.ExpenseEntity
import com.example.data.local.MemberEntity
import com.example.data.local.SettlementPaymentEntity
import com.example.data.local.TourDao
import com.example.data.local.TourEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

import com.example.data.local.TourPersistentStorage

class TourRepository(
    private val tourDao: TourDao,
    private val persistentStorage: TourPersistentStorage? = null
) {

    fun getAllTours(): Flow<List<TourEntity>> = tourDao.getAllTours()
    suspend fun getAllToursList(): List<TourEntity> = tourDao.getAllToursList()

    fun getTourFlow(tourId: String): Flow<TourEntity?> = tourDao.getTourFlow(tourId)

    suspend fun getTourById(tourId: String): TourEntity? = tourDao.getTourById(tourId)

    fun getMembersByTour(tourId: String): Flow<List<MemberEntity>> = tourDao.getMembersByTour(tourId)

    suspend fun getMembersListByTour(tourId: String): List<MemberEntity> = tourDao.getMembersListByTour(tourId)

    fun getExpensesByTour(tourId: String): Flow<List<ExpenseEntity>> = tourDao.getExpensesByTour(tourId)

    fun getSettlementsByTour(tourId: String): Flow<List<SettlementPaymentEntity>> = tourDao.getSettlementsByTour(tourId)

    suspend fun getExpensesListByTour(tourId: String): List<ExpenseEntity> = tourDao.getExpensesListByTour(tourId)

    suspend fun restoreFromPersistentStorage() {
        persistentStorage?.let { storage ->
            val backupTours = storage.getAllTours()
            if (backupTours.isNotEmpty()) {
                tourDao.insertTours(backupTours)
            }
            // Ensure any tours currently in Room are also mirrored to disk backup
            try {
                val currentDbTours = tourDao.getAllToursList()
                if (currentDbTours.isNotEmpty()) {
                    storage.saveAllTours(currentDbTours)
                }
            } catch (e: Exception) {
                android.util.Log.w("TourRepository", "Mirror DB tours note: ${e.message}")
            }

            // Restore all members from persistent storage
            val members = storage.getAllMembers()
            if (members.isNotEmpty()) {
                tourDao.insertMembers(members)
            }
            // Ensure any members in Room are also backed up to persistent storage
            try {
                val currentDbMembers = tourDao.getAllMembersList()
                if (currentDbMembers.isNotEmpty()) {
                    storage.saveAllMembers(currentDbMembers)
                }
            } catch (e: Exception) {
                android.util.Log.w("TourRepository", "Mirror DB members note: ${e.message}")
            }

            // Restore all expenses from persistent storage
            val expenses = storage.getAllExpenses()
            if (expenses.isNotEmpty()) {
                tourDao.insertExpenses(expenses)
            }
            // Ensure any expenses in Room are also backed up to persistent storage
            try {
                val currentDbExpenses = tourDao.getAllExpensesList()
                if (currentDbExpenses.isNotEmpty()) {
                    storage.saveAllExpenses(currentDbExpenses)
                }
            } catch (e: Exception) {
                android.util.Log.w("TourRepository", "Mirror DB expenses note: ${e.message}")
            }

            // Restore all settlements from persistent storage
            val settlements = storage.getAllSettlements()
            if (settlements.isNotEmpty()) {
                tourDao.insertSettlements(settlements)
            }
            try {
                val currentDbSettlements = tourDao.getAllSettlementsList()
                if (currentDbSettlements.isNotEmpty()) {
                    storage.saveAllSettlements(currentDbSettlements)
                }
            } catch (e: Exception) {
                android.util.Log.w("TourRepository", "Mirror DB settlements note: ${e.message}")
            }
        }
    }

    suspend fun createTour(
        title: String,
        destination: String,
        startDate: Long,
        endDate: Long,
        currencySymbol: String,
        budget: Double,
        themeColorHex: String,
        userId: String = ""
    ): String {
        val tourId = UUID.randomUUID().toString()
        val tour = TourEntity(
            id = tourId,
            userId = userId,
            title = title,
            destination = destination,
            startDate = startDate,
            endDate = endDate,
            currencySymbol = currencySymbol,
            budget = budget,
            themeColorHex = themeColorHex,
            isSettled = false,
            createdAt = System.currentTimeMillis()
        )
        tourDao.insertTour(tour)
        persistentStorage?.saveTour(tour)
        return tourId
    }

    suspend fun updateTour(tour: TourEntity) {
        tourDao.insertTour(tour) // Use insert with REPLACE so new/remote tours are properly inserted
        persistentStorage?.saveTour(tour)
    }

    suspend fun deleteTour(tour: TourEntity) {
        tourDao.deleteTour(tour)
        tourDao.deleteMembersByTour(tour.id)
        tourDao.deleteExpensesByTour(tour.id)
        tourDao.clearSettlementsForTour(tour.id)
        persistentStorage?.deleteTour(tour.id)
    }

    suspend fun addMember(
        tourId: String,
        name: String,
        familyNote: String,
        weight: Float,
        avatarColorHex: String,
        familyMembersText: String = "",
        paymentUpiId: String = "",
        paymentLink: String = "",
        memberId: String = UUID.randomUUID().toString()
    ): String {
        val member = MemberEntity(
            id = memberId,
            tourId = tourId,
            name = name,
            familyNote = familyNote,
            weight = weight,
            avatarColorHex = avatarColorHex,
            familyMembersText = familyMembersText,
            paymentUpiId = paymentUpiId,
            paymentLink = paymentLink
        )
        tourDao.insertMember(member)
        persistentStorage?.saveMember(member)
        return memberId
    }

    suspend fun updateMember(member: MemberEntity) {
        tourDao.insertMember(member)
        persistentStorage?.saveMember(member)
    }

    suspend fun deleteMember(member: MemberEntity) {
        tourDao.deleteMember(member)
        persistentStorage?.deleteMember(member.id)
    }

    suspend fun addExpense(
        tourId: String,
        title: String,
        amount: Double,
        date: Long,
        category: String,
        paidById: String,
        splitType: String,
        splitMemberIds: String,
        notes: String,
        expenseId: String = UUID.randomUUID().toString()
    ): String {
        val expense = ExpenseEntity(
            id = expenseId,
            tourId = tourId,
            title = title,
            amount = amount,
            date = date,
            category = category,
            paidById = paidById,
            splitType = splitType,
            splitMemberIds = splitMemberIds,
            notes = notes
        )
        tourDao.insertExpense(expense)
        persistentStorage?.saveExpense(expense)
        return expenseId
    }

    suspend fun deleteExpense(expense: ExpenseEntity) = tourDao.deleteExpense(expense)

    suspend fun recordSettlement(
        tourId: String,
        fromMemberId: String,
        toMemberId: String,
        amount: Double,
        notes: String = "Settlement direct payment",
        paymentId: String = UUID.randomUUID().toString()
    ): String {
        val payment = SettlementPaymentEntity(
            id = paymentId,
            tourId = tourId,
            fromMemberId = fromMemberId,
            toMemberId = toMemberId,
            amount = amount,
            date = System.currentTimeMillis(),
            notes = notes
        )
        tourDao.insertSettlement(payment)
        persistentStorage?.saveSettlement(payment)
        return paymentId
    }

    suspend fun deleteSettlement(payment: SettlementPaymentEntity) = tourDao.deleteSettlement(payment)

    suspend fun seedSampleFamilyTour(userId: String = "", isSubscribed: Boolean = false) {
        val tourId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        val day = 86400000L

        val tour = TourEntity(
            id = tourId,
            userId = userId,
            title = "Alpine Road Trip",
            destination = "Swiss Alps & Lake Como",
            startDate = now - day * 3,
            endDate = now + day * 4,
            currencySymbol = "$",
            budget = if (isSubscribed) 4500.0 else 1800.0,
            themeColorHex = "#0D5C75",
            isSettled = false,
            createdAt = now
        )
        tourDao.insertTour(tour)
        persistentStorage?.saveTour(tour)

        val m1 = MemberEntity(
            id = UUID.randomUUID().toString(),
            tourId = tourId,
            name = "Alex's Family",
            familyNote = "2 Adults, 1 Kid",
            weight = 2.5f,
            avatarColorHex = "#0D5C75",
            familyMembersText = "Alex, Mia, Leo (Kid)",
            paymentUpiId = "alex@okaxis",
            paymentLink = "paypal.me/alextravel"
        )
        val m2 = MemberEntity(
            id = UUID.randomUUID().toString(),
            tourId = tourId,
            name = "The Cooper Family",
            familyNote = "2 Adults",
            weight = 2.0f,
            avatarColorHex = "#E67E22",
            familyMembersText = "Liam, Emma",
            paymentUpiId = "cooper@upi",
            paymentLink = "paypal.me/coopertravel"
        )
        val m3 = MemberEntity(
            id = UUID.randomUUID().toString(),
            tourId = tourId,
            name = "The Sharma Family",
            familyNote = "2 Adults, 2 Kids",
            weight = 3.0f,
            avatarColorHex = "#9C27B0",
            familyMembersText = "Raj, Priya, Aryan, Ananya",
            paymentUpiId = "raj@oksbi",
            paymentLink = "paypal.me/rajsharma"
        )
        val m4 = MemberEntity(
            id = UUID.randomUUID().toString(),
            tourId = tourId,
            name = "Marcus Solo",
            familyNote = "1 Adult",
            weight = 1.0f,
            avatarColorHex = "#2E7D32",
            familyMembersText = "Marcus",
            paymentUpiId = "marcus@upi",
            paymentLink = "paypal.me/marcus"
        )

        val members = listOf(m1, m2, m3, m4)
        tourDao.insertMembers(members)
        members.forEach { persistentStorage?.saveMember(it) }

        val allIds = members.joinToString(",") { it.id }

        // Sample realistic expenses
        tourDao.insertExpense(
            ExpenseEntity(
                id = UUID.randomUUID().toString(),
                tourId = tourId,
                title = "Scenic Villa Rental (3 nights)",
                amount = 1250.0,
                date = now - day * 3,
                category = "STAY",
                paidById = m1.id,
                splitType = "BY_WEIGHT",
                splitMemberIds = allIds,
                notes = "Booked via vacation portal, beautiful lake view"
            )
        )

        tourDao.insertExpense(
            ExpenseEntity(
                id = UUID.randomUUID().toString(),
                tourId = tourId,
                title = "Welcome Italian Dinner",
                amount = 320.0,
                date = now - day * 3 + 36000000L,
                category = "FOOD",
                paidById = m2.id,
                splitType = "BY_WEIGHT",
                splitMemberIds = allIds,
                notes = "Pasta, pizzas, desserts and drinks for everyone"
            )
        )

        if (isSubscribed) {
            tourDao.insertExpense(
                ExpenseEntity(
                    id = UUID.randomUUID().toString(),
                    tourId = tourId,
                    title = "Mountain Pass Tolls & Fuel",
                    amount = 185.0,
                    date = now - day * 2,
                    category = "TRANSPORT",
                    paidById = m3.id,
                    splitType = "EQUAL",
                    splitMemberIds = allIds,
                    notes = "2 rental vans gas & Swiss highway vignettes"
                )
            )

            tourDao.insertExpense(
                ExpenseEntity(
                    id = UUID.randomUUID().toString(),
                    tourId = tourId,
                    title = "Cable Car Summit Passes",
                    amount = 440.0,
                    date = now - day * 1,
                    category = "ACTIVITY",
                    paidById = m4.id,
                    splitType = "BY_WEIGHT",
                    splitMemberIds = allIds,
                    notes = "Panoramic glacier view cable car tickets"
                )
            )
        } else {
            tourDao.insertExpense(
                ExpenseEntity(
                    id = UUID.randomUUID().toString(),
                    tourId = tourId,
                    title = "Mountain Pass Tolls & Fuel",
                    amount = 85.0,
                    date = now - day * 2,
                    category = "TRANSPORT",
                    paidById = m2.id,
                    splitType = "EQUAL",
                    splitMemberIds = allIds,
                    notes = "Rental car fuel and highway tolls"
                )
            )
        }

        tourDao.insertExpense(
            ExpenseEntity(
                id = UUID.randomUUID().toString(),
                tourId = tourId,
                title = "Supermarket Snacks & Beverages",
                amount = 145.0,
                date = now - 3600000L * 5,
                category = "GROCERY",
                paidById = m1.id,
                splitType = "EQUAL",
                splitMemberIds = allIds,
                notes = "Breakfast essentials, fruits, sparkling water"
            )
        )
    }
}
