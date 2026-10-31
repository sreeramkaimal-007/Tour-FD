package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        TourEntity::class,
        MemberEntity::class,
        ExpenseEntity::class,
        SettlementPaymentEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class TourDatabase : RoomDatabase() {
    abstract fun tourDao(): TourDao

    companion object {
        @Volatile
        private var INSTANCE: TourDatabase? = null

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    db.execSQL("ALTER TABLE tours ADD COLUMN userId TEXT NOT NULL DEFAULT ''")
                } catch (_: Exception) {
                    // Column may already exist
                }
            }
        }

        fun getDatabase(context: Context): TourDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TourDatabase::class.java,
                    "tour_tab_database"
                )
                    .addMigrations(MIGRATION_2_3)
                    .fallbackToDestructiveMigration(false)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
