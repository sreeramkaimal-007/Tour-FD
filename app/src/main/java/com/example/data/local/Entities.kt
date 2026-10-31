package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tours")
data class TourEntity(
    @PrimaryKey val id: String,
    val userId: String = "",
    val title: String,
    val destination: String,
    val startDate: Long,
    val endDate: Long,
    val currencySymbol: String = "$",
    val budget: Double = 0.0,
    val themeColorHex: String = "#0D5C75",
    val isSettled: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "members")
data class MemberEntity(
    @PrimaryKey val id: String,
    val tourId: String,
    val name: String,
    val familyNote: String = "1 Adult",
    val weight: Float = 1.0f,
    val avatarColorHex: String = "#0D5C75",
    val familyMembersText: String = "",
    val paymentUpiId: String = "",
    val paymentLink: String = ""
)

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey val id: String,
    val tourId: String,
    val title: String,
    val amount: Double,
    val date: Long,
    val category: String, // FOOD, STAY, TRANSPORT, ACTIVITY, GROCERY, SHOPPING, MISC
    val paidById: String,
    val splitType: String, // EQUAL, BY_WEIGHT, CUSTOM
    val splitMemberIds: String, // Comma separated IDs of members sharing this expense
    val notes: String = ""
)

@Entity(tableName = "settlement_payments")
data class SettlementPaymentEntity(
    @PrimaryKey val id: String,
    val tourId: String,
    val fromMemberId: String,
    val toMemberId: String,
    val amount: Double,
    val date: Long,
    val notes: String = ""
)
