package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TourDao {
    @Query("SELECT * FROM tours ORDER BY createdAt DESC")
    fun getAllTours(): Flow<List<TourEntity>>

    @Query("SELECT * FROM tours ORDER BY createdAt DESC")
    suspend fun getAllToursList(): List<TourEntity>

    @Query("SELECT * FROM tours WHERE id = :tourId LIMIT 1")
    suspend fun getTourById(tourId: String): TourEntity?

    @Query("SELECT * FROM tours WHERE id = :tourId LIMIT 1")
    fun getTourFlow(tourId: String): Flow<TourEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTour(tour: TourEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTours(tours: List<TourEntity>)

    @Update
    suspend fun updateTour(tour: TourEntity)

    @Delete
    suspend fun deleteTour(tour: TourEntity)

    // Members
    @Query("SELECT * FROM members WHERE tourId = :tourId")
    fun getMembersByTour(tourId: String): Flow<List<MemberEntity>>

    @Query("SELECT * FROM members WHERE tourId = :tourId")
    suspend fun getMembersListByTour(tourId: String): List<MemberEntity>

    @Query("SELECT * FROM members")
    suspend fun getAllMembersList(): List<MemberEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: MemberEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMembers(members: List<MemberEntity>)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateMember(member: MemberEntity)

    @Delete
    suspend fun deleteMember(member: MemberEntity)

    @Query("DELETE FROM members WHERE tourId = :tourId")
    suspend fun deleteMembersByTour(tourId: String)

    // Expenses
    @Query("SELECT * FROM expenses WHERE tourId = :tourId ORDER BY date DESC")
    fun getExpensesByTour(tourId: String): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE tourId = :tourId ORDER BY date DESC")
    suspend fun getExpensesListByTour(tourId: String): List<ExpenseEntity>

    @Query("SELECT * FROM expenses")
    suspend fun getAllExpensesList(): List<ExpenseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpenses(expenses: List<ExpenseEntity>)

    @Update
    suspend fun updateExpense(expense: ExpenseEntity)

    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)

    @Query("DELETE FROM expenses WHERE tourId = :tourId")
    suspend fun deleteExpensesByTour(tourId: String)

    // Settlements
    @Query("SELECT * FROM settlement_payments WHERE tourId = :tourId ORDER BY date DESC")
    fun getSettlementsByTour(tourId: String): Flow<List<SettlementPaymentEntity>>

    @Query("SELECT * FROM settlement_payments")
    suspend fun getAllSettlementsList(): List<SettlementPaymentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettlement(payment: SettlementPaymentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettlements(payments: List<SettlementPaymentEntity>)

    @Delete
    suspend fun deleteSettlement(payment: SettlementPaymentEntity)

    @Query("DELETE FROM settlement_payments WHERE tourId = :tourId")
    suspend fun clearSettlementsForTour(tourId: String)
}
