package com.example.data.local

import android.content.Context
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/**
 * Robust JSON-based persistent storage in SharedPreferences.
 * Acts as an offline-first durable backup layer to guarantee tours,
 * members, expenses, and settlements are never lost across app restarts,
 * database migrations, or logout/login cycles.
 */
class TourPersistentStorage(context: Context) {
    private val prefs = context.getSharedPreferences("tour_fd_data_backup_v2", Context.MODE_PRIVATE)

    @Synchronized
    fun saveTour(tour: TourEntity) {
        try {
            val list = getAllTours().toMutableList()
            list.removeAll { it.id == tour.id }
            list.add(0, tour)
            saveAllTours(list)

            // Also save individual backup key for safety
            val obj = tourToJson(tour)
            prefs.edit().putString("tour_${tour.id}", obj.toString()).commit()
        } catch (e: Exception) {
            Log.e("TourPersistentStorage", "Failed to save tour", e)
        }
    }

    private fun tourToJson(t: TourEntity): JSONObject {
        return JSONObject().apply {
            put("id", t.id)
            put("userId", t.userId)
            put("title", t.title)
            put("destination", t.destination)
            put("startDate", t.startDate)
            put("endDate", t.endDate)
            put("currencySymbol", t.currencySymbol)
            put("budget", t.budget)
            put("themeColorHex", t.themeColorHex)
            put("isSettled", t.isSettled)
            put("createdAt", t.createdAt)
        }
    }

    private fun jsonToTour(obj: JSONObject): TourEntity {
        val uId = obj.optString("userId", "").ifBlank { obj.optString("user_id", "") }
        val tTitle = obj.optString("title", "").ifBlank { obj.optString("name", "Vacation Tour") }
        val tDest = obj.optString("destination", "").ifBlank { obj.optString("location", "Holiday Destination") }
        return TourEntity(
            id = obj.getString("id"),
            userId = uId,
            title = tTitle,
            destination = tDest,
            startDate = obj.optLong("startDate", obj.optLong("start_date", System.currentTimeMillis())),
            endDate = obj.optLong("endDate", obj.optLong("end_date", System.currentTimeMillis() + 86400000L)),
            currencySymbol = obj.optString("currencySymbol", obj.optString("currency_symbol", "$")),
            budget = obj.optDouble("budget", 0.0),
            themeColorHex = obj.optString("themeColorHex", obj.optString("theme_color_hex", "#0D5C75")),
            isSettled = obj.optBoolean("isSettled", obj.optBoolean("is_settled", false)),
            createdAt = obj.optLong("createdAt", obj.optLong("created_at", System.currentTimeMillis()))
        )
    }

    @Synchronized
    fun saveAllTours(tours: List<TourEntity>) {
        val array = JSONArray()
        for (t in tours) {
            array.put(tourToJson(t))
        }
        val editor = prefs.edit()
        editor.putString("backup_tours", array.toString())
        for (t in tours) {
            editor.putString("tour_${t.id}", tourToJson(t).toString())
        }
        editor.commit()
    }

    @Synchronized
    fun getAllTours(): List<TourEntity> {
        val json = prefs.getString("backup_tours", null)
        val list = mutableListOf<TourEntity>()
        if (!json.isNullOrBlank()) {
            try {
                val array = JSONArray(json)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(jsonToTour(obj))
                }
            } catch (e: Exception) {
                Log.e("TourPersistentStorage", "Failed to parse backup_tours array", e)
            }
        }

        // Secondary fallback: recover any standalone tour keys
        try {
            val allEntries = prefs.all
            for ((key, value) in allEntries) {
                if (key.startsWith("tour_") && value is String) {
                    try {
                        val obj = JSONObject(value)
                        val recoveredTour = jsonToTour(obj)
                        if (list.none { it.id == recoveredTour.id }) {
                            list.add(recoveredTour)
                        }
                    } catch (_: Exception) {}
                }
            }
        } catch (e: Exception) {
            Log.e("TourPersistentStorage", "Secondary tour recovery note", e)
        }

        return list
    }

    private fun memberToJson(m: MemberEntity): JSONObject {
        return JSONObject().apply {
            put("id", m.id)
            put("tourId", m.tourId)
            put("tour_id", m.tourId)
            put("name", m.name)
            put("familyNote", m.familyNote)
            put("family_note", m.familyNote)
            put("weight", m.weight.toDouble())
            put("avatarColorHex", m.avatarColorHex)
            put("avatar_color_hex", m.avatarColorHex)
            put("familyMembersText", m.familyMembersText)
            put("family_members_text", m.familyMembersText)
            put("paymentUpiId", m.paymentUpiId)
            put("payment_upi_id", m.paymentUpiId)
            put("paymentLink", m.paymentLink)
            put("payment_link", m.paymentLink)
        }
    }

    private fun jsonToMember(obj: JSONObject): MemberEntity {
        val id = obj.optString("id", UUID.randomUUID().toString())
        val tourId = obj.optString("tourId", obj.optString("tour_id", ""))
        val name = obj.optString("name", "Tour Member")
        val familyNote = obj.optString("familyNote", obj.optString("family_note", "1 Adult"))
        val weight = obj.optDouble("weight", 1.0).toFloat()
        val avatarColorHex = obj.optString("avatarColorHex", obj.optString("avatar_color_hex", "#0D5C75"))
        val familyMembersText = obj.optString(
            "familyMembersText",
            obj.optString("family_members_text", obj.optString("family_members", name))
        )
        val paymentUpiId = obj.optString("paymentUpiId", obj.optString("payment_upi_id", ""))
        val paymentLink = obj.optString("paymentLink", obj.optString("payment_link", ""))

        return MemberEntity(
            id = id,
            tourId = tourId,
            name = name,
            familyNote = familyNote,
            weight = weight,
            avatarColorHex = avatarColorHex,
            familyMembersText = familyMembersText,
            paymentUpiId = paymentUpiId,
            paymentLink = paymentLink
        )
    }

    private fun expenseToJson(e: ExpenseEntity): JSONObject {
        return JSONObject().apply {
            put("id", e.id)
            put("tourId", e.tourId)
            put("tour_id", e.tourId)
            put("title", e.title)
            put("amount", e.amount)
            put("date", e.date)
            put("category", e.category)
            put("paidById", e.paidById)
            put("paid_by_id", e.paidById)
            put("splitType", e.splitType)
            put("split_type", e.splitType)
            put("splitMemberIds", e.splitMemberIds)
            put("split_member_ids", e.splitMemberIds)
            put("notes", e.notes)
        }
    }

    private fun jsonToExpense(obj: JSONObject): ExpenseEntity {
        val id = obj.optString("id", UUID.randomUUID().toString())
        val tourId = obj.optString("tourId", obj.optString("tour_id", ""))
        val title = obj.optString("title", "Expense")
        val amount = obj.optDouble("amount", 0.0)
        val date = obj.optLong("date", System.currentTimeMillis())
        val category = obj.optString("category", "OTHER")
        val paidById = obj.optString("paidById", obj.optString("paid_by_id", ""))
        val splitType = obj.optString("splitType", obj.optString("split_type", "EQUAL"))
        val splitMemberIds = obj.optString("splitMemberIds", obj.optString("split_member_ids", ""))
        val notes = obj.optString("notes", "")

        return ExpenseEntity(
            id = id,
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
    }

    @Synchronized
    fun saveMember(member: MemberEntity) {
        val list = getAllMembers().toMutableList()
        val existing = list.find { it.id == member.id }
        val toSave = if (existing != null && member.name == "Tour Member" && existing.name != "Tour Member") {
            existing.copy(
                familyNote = if (member.familyNote != "Tour Member" && member.familyNote != "1 Adult") member.familyNote else existing.familyNote,
                weight = if (member.weight > 0f) member.weight else existing.weight,
                familyMembersText = if (member.familyMembersText.isNotBlank() && member.familyMembersText != "Tour Member") member.familyMembersText else existing.familyMembersText
            )
        } else {
            member
        }

        val editor = prefs.edit()
        editor.putString("member_${toSave.id}", memberToJson(toSave).toString())
        editor.putString("tour_${toSave.tourId}_member_${toSave.id}", memberToJson(toSave).toString())

        list.removeAll { it.id == toSave.id }
        list.add(toSave)

        val array = JSONArray()
        for (m in list) {
            array.put(memberToJson(m))
        }
        editor.putString("backup_members", array.toString())
        editor.commit()
    }

    @Synchronized
    fun deleteMember(memberId: String) {
        val list = getAllMembers().toMutableList()
        val toRemove = list.filter { it.id == memberId }
        list.removeAll { it.id == memberId }
        val array = JSONArray()
        for (m in list) {
            array.put(memberToJson(m))
        }
        val editor = prefs.edit()
            .putString("backup_members", array.toString())
            .remove("member_$memberId")
        for (m in toRemove) {
            editor.remove("tour_${m.tourId}_member_${m.id}")
        }
        editor.commit()
    }

    @Synchronized
    fun saveAllMembers(members: List<MemberEntity>) {
        val currentList = getAllMembers().toMutableList()
        val editor = prefs.edit()
        for (incoming in members) {
            val existing = currentList.find { it.id == incoming.id }
            val m = if (existing != null && incoming.name == "Tour Member" && existing.name != "Tour Member") {
                existing.copy(
                    familyNote = if (incoming.familyNote != "Tour Member" && incoming.familyNote != "1 Adult") incoming.familyNote else existing.familyNote,
                    weight = if (incoming.weight > 0f) incoming.weight else existing.weight,
                    familyMembersText = if (incoming.familyMembersText.isNotBlank() && incoming.familyMembersText != "Tour Member") incoming.familyMembersText else existing.familyMembersText
                )
            } else {
                incoming
            }
            currentList.removeAll { it.id == m.id }
            currentList.add(m)
            editor.putString("member_${m.id}", memberToJson(m).toString())
            editor.putString("tour_${m.tourId}_member_${m.id}", memberToJson(m).toString())
        }
        val array = JSONArray()
        for (m in currentList) {
            array.put(memberToJson(m))
        }
        editor.putString("backup_members", array.toString()).commit()
    }

    @Synchronized
    fun getAllMembers(): List<MemberEntity> {
        val list = mutableListOf<MemberEntity>()
        val json = prefs.getString("backup_members", null)
        if (!json.isNullOrBlank()) {
            try {
                val array = JSONArray(json)
                for (i in 0 until array.length()) {
                    list.add(jsonToMember(array.getJSONObject(i)))
                }
            } catch (e: Exception) {
                Log.e("TourPersistentStorage", "Failed to parse backup_members", e)
            }
        }

        // Secondary fallback: recover any standalone member keys so members are never lost and always up to date
        try {
            val allEntries = prefs.all
            for ((key, value) in allEntries) {
                if ((key.startsWith("member_") || key.contains("_member_")) && value is String) {
                    try {
                        val recovered = jsonToMember(JSONObject(value))
                        val existingIdx = list.indexOfFirst { it.id == recovered.id }
                        if (existingIdx == -1) {
                            list.add(recovered)
                        } else if (list[existingIdx].name == "Tour Member" && recovered.name != "Tour Member") {
                            list[existingIdx] = recovered
                        }
                    } catch (_: Exception) {}
                }
            }
        } catch (e: Exception) {
            Log.e("TourPersistentStorage", "Member recovery note", e)
        }

        return list
    }

    @Synchronized
    fun getMembersForTour(tourId: String): List<MemberEntity> {
        return getAllMembers().filter { it.tourId == tourId }
    }

    @Synchronized
    fun saveExpense(expense: ExpenseEntity) {
        val list = getAllExpenses().toMutableList()
        list.removeAll { it.id == expense.id }
        list.add(0, expense)
        saveAllExpenses(list)

        // Store individual expense key for safety
        prefs.edit()
            .putString("expense_${expense.id}", expenseToJson(expense).toString())
            .commit()
    }

    @Synchronized
    fun saveAllExpenses(expenses: List<ExpenseEntity>) {
        val currentList = getAllExpenses().toMutableList()
        val editor = prefs.edit()
        for (e in expenses) {
            currentList.removeAll { it.id == e.id }
            currentList.add(e)
            editor.putString("expense_${e.id}", expenseToJson(e).toString())
        }
        val array = JSONArray()
        for (e in currentList) {
            array.put(expenseToJson(e))
        }
        editor.putString("backup_expenses", array.toString()).commit()
    }

    @Synchronized
    fun getAllExpenses(): List<ExpenseEntity> {
        val list = mutableListOf<ExpenseEntity>()
        val json = prefs.getString("backup_expenses", null)
        if (!json.isNullOrBlank()) {
            try {
                val array = JSONArray(json)
                for (i in 0 until array.length()) {
                    list.add(jsonToExpense(array.getJSONObject(i)))
                }
            } catch (e: Exception) {
                Log.e("TourPersistentStorage", "Failed to parse backup_expenses", e)
            }
        }

        // Secondary fallback: recover any standalone expense keys
        try {
            val allEntries = prefs.all
            for ((key, value) in allEntries) {
                if (key.startsWith("expense_") && value is String) {
                    try {
                        val recovered = jsonToExpense(JSONObject(value))
                        if (list.none { it.id == recovered.id }) {
                            list.add(recovered)
                        }
                    } catch (_: Exception) {}
                }
            }
        } catch (e: Exception) {
            Log.e("TourPersistentStorage", "Expense recovery note", e)
        }

        return list
    }

    @Synchronized
    fun saveSettlement(settlement: SettlementPaymentEntity) {
        val list = getAllSettlements().toMutableList()
        list.removeAll { it.id == settlement.id }
        list.add(0, settlement)
        saveAllSettlements(list)
    }

    @Synchronized
    fun saveAllSettlements(settlements: List<SettlementPaymentEntity>) {
        val array = JSONArray()
        for (s in settlements) {
            val obj = JSONObject().apply {
                put("id", s.id)
                put("tourId", s.tourId)
                put("fromMemberId", s.fromMemberId)
                put("toMemberId", s.toMemberId)
                put("amount", s.amount)
                put("date", s.date)
                put("notes", s.notes)
            }
            array.put(obj)
        }
        prefs.edit().putString("backup_settlements", array.toString()).commit()
    }

    @Synchronized
    fun getAllSettlements(): List<SettlementPaymentEntity> {
        val json = prefs.getString("backup_settlements", null) ?: return emptyList()
        val list = mutableListOf<SettlementPaymentEntity>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    SettlementPaymentEntity(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        tourId = obj.optString("tourId", obj.optString("tour_id", "")),
                        fromMemberId = obj.optString("fromMemberId", obj.optString("from_member_id", "")),
                        toMemberId = obj.optString("toMemberId", obj.optString("to_member_id", "")),
                        amount = obj.optDouble("amount", 0.0),
                        date = obj.optLong("date", System.currentTimeMillis()),
                        notes = obj.optString("notes", "")
                    )
                )
            }
        } catch (e: Exception) {
            Log.e("TourPersistentStorage", "Failed to parse settlements", e)
        }
        return list
    }

    @Synchronized
    fun deleteTour(tourId: String) {
        val editor = prefs.edit()
        val tours = getAllTours().filterNot { it.id == tourId }
        val arrayT = JSONArray()
        for (t in tours) arrayT.put(tourToJson(t))
        editor.putString("backup_tours", arrayT.toString())
        editor.remove("tour_$tourId")

        val allM = getAllMembers()
        val membersToDelete = allM.filter { it.tourId == tourId }
        val remainingMembers = allM.filterNot { it.tourId == tourId }
        for (m in membersToDelete) editor.remove("member_${m.id}")
        val arrayM = JSONArray()
        for (m in remainingMembers) arrayM.put(memberToJson(m))
        editor.putString("backup_members", arrayM.toString())

        val allE = getAllExpenses()
        val expensesToDelete = allE.filter { it.tourId == tourId }
        val remainingExpenses = allE.filterNot { it.tourId == tourId }
        for (e in expensesToDelete) editor.remove("expense_${e.id}")
        val arrayE = JSONArray()
        for (e in remainingExpenses) arrayE.put(expenseToJson(e))
        editor.putString("backup_expenses", arrayE.toString())

        val remainingSettlements = getAllSettlements().filterNot { it.tourId == tourId }
        val arrayS = JSONArray()
        for (s in remainingSettlements) {
            arrayS.put(JSONObject().apply {
                put("id", s.id)
                put("tourId", s.tourId)
                put("fromMemberId", s.fromMemberId)
                put("toMemberId", s.toMemberId)
                put("amount", s.amount)
                put("date", s.date)
                put("notes", s.notes)
            })
        }
        editor.putString("backup_settlements", arrayS.toString())

        editor.commit()
    }
}
