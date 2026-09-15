package com.subhunt.app.data.repository

import com.subhunt.app.data.local.SubscriptionDao
import com.subhunt.app.data.local.SubscriptionEntity
import com.subhunt.app.domain.model.Subscription
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

interface SubscriptionRepository {
    fun getActiveSubscriptions(): Flow<List<Subscription>>
    suspend fun getById(id: Long): Subscription?
    suspend fun insert(subscription: Subscription): Long
    suspend fun update(subscription: Subscription)
    suspend fun delete(subscription: Subscription)
}

@Singleton
class SubscriptionRepositoryImpl @Inject constructor(
    private val dao: SubscriptionDao
) : SubscriptionRepository {

    override fun getActiveSubscriptions(): Flow<List<Subscription>> =
        dao.getActiveSubscriptions().map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun getById(id: Long): Subscription? =
        dao.getById(id)?.toDomain()

    override suspend fun insert(subscription: Subscription): Long =
        dao.insert(SubscriptionEntity.fromDomain(subscription))

    override suspend fun update(subscription: Subscription) =
        dao.update(SubscriptionEntity.fromDomain(subscription))

    override suspend fun delete(subscription: Subscription) =
        dao.delete(SubscriptionEntity.fromDomain(subscription))
}
