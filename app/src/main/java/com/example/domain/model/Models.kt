package com.example.domain.model

import java.util.Locale
import kotlin.math.abs
import kotlin.math.min

enum class ExpenseCategory(
    val id: String,
    val displayName: String,
    val iconName: String,
    val colorHex: String
) {
    FOOD("FOOD", "Dining & Food", "Restaurant", "#FF7043"),
    STAY("STAY", "Hotel & Stay", "Hotel", "#42A5F5"),
    TRANSPORT("TRANSPORT", "Travel & Fuel", "DirectionsCar", "#26A69A"),
    ACTIVITY("ACTIVITY", "Tickets & Fun", "Attractions", "#AB47BC"),
    GROCERY("GROCERY", "Snacks & Drinks", "LocalGroceryStore", "#66BB6A"),
    SHOPPING("SHOPPING", "Shopping", "ShoppingBag", "#EC407A"),
    MISC("MISC", "Miscellaneous", "ReceiptLong", "#78909C");

    companion object {
        fun fromId(id: String): ExpenseCategory = entries.firstOrNull { it.id == id } ?: MISC
    }
}

enum class SplitType(val id: String, val title: String) {
    EQUAL("EQUAL", "Split Equally"),
    BY_WEIGHT("BY_WEIGHT", "By Family Weight"),
    CUSTOM("CUSTOM", "Custom / Selected Only")
}

data class MemberBalance(
    val memberId: String,
    val memberName: String,
    val familyNote: String,
    val weight: Float,
    val avatarColorHex: String,
    val totalPaid: Double,
    val totalShare: Double,
    val netBalance: Double, // Positive = to receive, Negative = to pay
    val familyMembersText: String = "",
    val paymentUpiId: String = "",
    val paymentLink: String = ""
)

data class DirectSettlementProposal(
    val fromMemberId: String,
    val fromMemberName: String,
    val toMemberId: String,
    val toMemberName: String,
    val amount: Double,
    val isSettled: Boolean = false,
    val toMemberUpiId: String = "",
    val toMemberPaymentLink: String = "",
    val fromMemberFamily: String = "",
    val toMemberFamily: String = ""
)

object SettlementEngine {

    /**
     * Calculates balances for each member in the tour.
     * Takes into account expenses and existing settlement payments.
     */
    fun calculateBalances(
        members: List<com.example.data.local.MemberEntity>,
        expenses: List<com.example.data.local.ExpenseEntity>,
        settlements: List<com.example.data.local.SettlementPaymentEntity>
    ): List<MemberBalance> {
        val memberMap = members.associateBy { it.id }
        val paidMap = mutableMapOf<String, Double>()
        val shareMap = mutableMapOf<String, Double>()

        members.forEach {
            paidMap[it.id] = 0.0
            shareMap[it.id] = 0.0
        }

        // 1. Process expenses
        for (expense in expenses) {
            val payerId = expense.paidById
            paidMap[payerId] = (paidMap[payerId] ?: 0.0) + expense.amount

            // Parse who is sharing this expense
            val isCustom = expense.splitType == SplitType.CUSTOM.id || expense.splitMemberIds.contains(":")
            if (isCustom && expense.splitMemberIds.contains(":")) {
                // Exact custom amount per specific member: "id1:45.0,id2:55.0"
                val items = expense.splitMemberIds.split(",").filter { it.isNotBlank() }
                for (item in items) {
                    val pair = item.split(":")
                    if (pair.isNotEmpty()) {
                        val mId = pair[0].trim()
                        val customShare = pair.getOrNull(1)?.toDoubleOrNull() ?: 0.0
                        shareMap[mId] = (shareMap[mId] ?: 0.0) + customShare
                    }
                }
            } else {
                val participatingIds = if (expense.splitMemberIds.isBlank()) {
                    members.map { it.id }
                } else {
                    expense.splitMemberIds.split(",").map { it.trim() }.filter { it.isNotBlank() }
                }.ifEmpty { members.map { it.id } }

                when (expense.splitType) {
                    SplitType.BY_WEIGHT.id -> {
                        // Split proportionally to family weight among participating members
                        val totalWeight = participatingIds.sumOf { (memberMap[it]?.weight ?: 1.0f).toDouble() }
                        val safeTotalWeight = if (totalWeight > 0.0) totalWeight else participatingIds.size.toDouble()

                        for (memberId in participatingIds) {
                            val weight = (memberMap[memberId]?.weight ?: 1.0f).toDouble()
                            val share = (expense.amount * weight) / safeTotalWeight
                            shareMap[memberId] = (shareMap[memberId] ?: 0.0) + share
                        }
                    }
                    else -> {
                        // Equal split among participating specific members
                        val count = participatingIds.size
                        if (count > 0) {
                            val perPersonShare = expense.amount / count
                            for (memberId in participatingIds) {
                                shareMap[memberId] = (shareMap[memberId] ?: 0.0) + perPersonShare
                            }
                        }
                    }
                }
            }
        }

        // 2. Adjust for settlements already paid
        // If fromMember paid toMember $X, fromMember paid upfront, toMember received money
        for (settlement in settlements) {
            paidMap[settlement.fromMemberId] = (paidMap[settlement.fromMemberId] ?: 0.0) + settlement.amount
            shareMap[settlement.toMemberId] = (shareMap[settlement.toMemberId] ?: 0.0) + settlement.amount
        }

        return members.map { member ->
            val paid = paidMap[member.id] ?: 0.0
            val share = shareMap[member.id] ?: 0.0
            val net = paid - share
            MemberBalance(
                memberId = member.id,
                memberName = member.name,
                familyNote = member.familyNote,
                weight = member.weight,
                avatarColorHex = member.avatarColorHex,
                totalPaid = paid,
                totalShare = share,
                netBalance = net,
                familyMembersText = member.familyMembersText,
                paymentUpiId = member.paymentUpiId,
                paymentLink = member.paymentLink
            )
        }
    }

    /**
     * Greedy algorithm to minimize transactions between participants.
     */
    fun computeDirectSettlements(
        balances: List<MemberBalance>
    ): List<DirectSettlementProposal> {
        val creditors = mutableListOf<Pair<String, Double>>() // (MemberId, positive balance)
        val debtors = mutableListOf<Pair<String, Double>>()   // (MemberId, positive debt)
        val balanceMap = balances.associateBy { it.memberId }

        for (b in balances) {
            val roundedNet = (Math.round(b.netBalance * 100.0) / 100.0)
            if (roundedNet > 0.01) {
                creditors.add(b.memberId to roundedNet)
            } else if (roundedNet < -0.01) {
                debtors.add(b.memberId to abs(roundedNet))
            }
        }

        // Sort descending so largest debts match with largest credits
        creditors.sortByDescending { it.second }
        debtors.sortByDescending { it.second }

        val settlements = mutableListOf<DirectSettlementProposal>()
        var cIdx = 0
        var dIdx = 0

        val currentCreditors = creditors.map { it.first to it.second }.toMutableList()
        val currentDebtors = debtors.map { it.first to it.second }.toMutableList()

        while (cIdx < currentCreditors.size && dIdx < currentDebtors.size) {
            val (cId, cAmount) = currentCreditors[cIdx]
            val (dId, dAmount) = currentDebtors[dIdx]

            val isLastPair = (cIdx == currentCreditors.lastIndex && dIdx == currentDebtors.lastIndex)
            val settleAmount = if (isLastPair && abs(cAmount - dAmount) <= 0.02) {
                // Absorb 1-cent floating point rounding into the final settlement
                Math.max(cAmount, dAmount)
            } else {
                min(cAmount, dAmount)
            }

            if (settleAmount > 0.009) {
                val creditorBalance = balanceMap[cId]
                val debtorBalance = balanceMap[dId]
                settlements.add(
                    DirectSettlementProposal(
                        fromMemberId = dId,
                        fromMemberName = debtorBalance?.memberName ?: "Unknown",
                        toMemberId = cId,
                        toMemberName = creditorBalance?.memberName ?: "Unknown",
                        amount = Math.round(settleAmount * 100.0) / 100.0,
                        toMemberUpiId = creditorBalance?.paymentUpiId ?: "",
                        toMemberPaymentLink = creditorBalance?.paymentLink ?: "",
                        fromMemberFamily = debtorBalance?.familyMembersText ?: "",
                        toMemberFamily = creditorBalance?.familyMembersText ?: ""
                    )
                )
            }

            val remCredit = cAmount - settleAmount
            val remDebt = dAmount - settleAmount

            if (remCredit <= 0.015 || isLastPair) {
                cIdx++
            } else {
                currentCreditors[cIdx] = cId to remCredit
            }

            if (remDebt <= 0.015 || isLastPair) {
                dIdx++
            } else {
                currentDebtors[dIdx] = dId to remDebt
            }
        }

        return settlements
    }

    /**
     * Generates a clean, friendly WhatsApp/SMS text report of the tour settlement.
     */
    fun generateTourReport(
        tourTitle: String,
        currencySymbol: String,
        balances: List<MemberBalance>,
        settlements: List<DirectSettlementProposal>,
        totalSpent: Double
    ): String {
        val sb = StringBuilder()
        sb.append("✈️ Tour FD Expense Summary: $tourTitle\n")
        sb.append("────────────────────────\n")
        sb.append(String.format(Locale.getDefault(), "💰 Total Tour Spending: %s%.2f\n\n", currencySymbol, totalSpent))

        sb.append("👨‍👩‍👧‍👦 Family & Group Balances:\n")
        for (b in balances) {
            val status = when {
                b.netBalance > 0.01 -> String.format(Locale.getDefault(), "Gets back %s%.2f", currencySymbol, b.netBalance)
                b.netBalance < -0.01 -> String.format(Locale.getDefault(), "Owes %s%.2f", currencySymbol, abs(b.netBalance))
                else -> "All Settled ✅"
            }
            val membersInfo = if (b.familyMembersText.isNotBlank()) " [${b.familyMembersText}]" else ""
            sb.append(String.format(Locale.getDefault(), "• %s%s (%s): Paid %s%.2f | Share %s%.2f ➜ %s\n",
                b.memberName, membersInfo, b.familyNote, currencySymbol, b.totalPaid, currencySymbol, b.totalShare, status))
        }

        sb.append("\n🤝 Direct Settlement Transfers (Greedy Minimizer):\n")
        if (settlements.isEmpty()) {
            sb.append("🎉 All family accounts are balanced! No pending payments.\n")
        } else {
            for (s in settlements) {
                val payMethods = mutableListOf<String>()
                if (s.toMemberUpiId.isNotBlank()) payMethods.add("UPI: ${s.toMemberUpiId}")
                if (s.toMemberPaymentLink.isNotBlank()) payMethods.add("Link: ${s.toMemberPaymentLink}")
                val paySuffix = if (payMethods.isNotEmpty()) " (${payMethods.joinToString(", ")})" else ""

                sb.append(String.format(Locale.getDefault(), "👉 %s pays %s %s%.2f%s\n",
                    s.fromMemberName, s.toMemberName, currencySymbol, s.amount, paySuffix))
            }
        }
        sb.append("\nSettled seamlessly via Tour FD 📱")
        return sb.toString()
    }
}
