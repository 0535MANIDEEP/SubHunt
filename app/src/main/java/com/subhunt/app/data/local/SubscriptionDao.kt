package com.subhunt.app.data.local

import androidx.room.*
import com.subhunt.app.domain.model.BillingCycle
import com.subhunt.app.domain.model.Subscription
import com.subhunt.app.domain.model.SubscriptionCategory
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Entity(
    tableName = "subscriptions",
    indices = [Index(value = ["isActive", "nextBillingDate"])]
)
data class SubscriptionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val cost: Double,
    val billingCycle: String,
    val category: String,
    val startDate: String,
    val nextBillingDate: String,
    val color: Long,
    val notes: String,
    val isActive: Boolean,
    val reminderDaysBefore: Int
) {
    fun toDomain() = Subscription(
        id = id,
        name = name,
        cost = cost,
        billingCycle = BillingCycle.valueOf(billingCycle),
        category = SubscriptionCategory.valueOf(category),
        startDate = LocalDate.parse(startDate),
        nextBillingDate = LocalDate.parse(nextBillingDate),
        color = color,
        notes = notes,
        isActive = isActive,
        reminderDaysBefore = reminderDaysBefore
    )

    companion object {
        fun fromDomain(sub: Subscription) = SubscriptionEntity(
            id = sub.id,
            name = sub.name,
            cost = sub.cost,
            billingCycle = sub.billingCycle.name,
            category = sub.category.name,
            startDate = sub.startDate.toString(),
            nextBillingDate = sub.nextBillingDate.toString(),
            color = sub.color,
            notes = sub.notes,
            isActive = sub.isActive,
            reminderDaysBefore = sub.reminderDaysBefore
        )
    }
}

@Dao
interface SubscriptionDao {
    @Query("SELECT * FROM subscriptions WHERE isActive = 1 ORDER BY nextBillingDate ASC")
    fun getActiveSubscriptions(): Flow<List<SubscriptionEntity>>

    @Query("SELECT * FROM subscriptions WHERE id = :id")
    suspend fun getById(id: Long): SubscriptionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(subscription: SubscriptionEntity): Long

    @Update
    suspend fun update(subscription: SubscriptionEntity)

    @Delete
    suspend fun delete(subscription: SubscriptionEntity)

    @Query("SELECT * FROM subscriptions WHERE isActive = 1")
    suspend fun getAllSubscriptionsList(): List<SubscriptionEntity>
}
