package com.subhunt.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [SubscriptionEntity::class],
    version = 2,
    exportSchema = false
)
abstract class SubHuntDatabase : RoomDatabase() {
    abstract fun subscriptionDao(): SubscriptionDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_subscriptions_isActive_nextBillingDate` " +
                        "ON `subscriptions` (`isActive`, `nextBillingDate`)"
                )
            }
        }
    }
}
