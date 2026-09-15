package com.subhunt.app.domain.usecase

import com.subhunt.app.domain.model.*
import com.subhunt.app.data.repository.SubscriptionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSubscriptionsUseCase @Inject constructor(
    private val repository: SubscriptionRepository
) {
    operator fun invoke(): Flow<List<Subscription>> =
        repository.getActiveSubscriptions()
}

class AddSubscriptionUseCase @Inject constructor(
    private val repository: SubscriptionRepository
) {
    suspend operator fun invoke(subscription: Subscription): Long =
        repository.insert(subscription)
}

class UpdateSubscriptionUseCase @Inject constructor(
    private val repository: SubscriptionRepository
) {
    suspend operator fun invoke(subscription: Subscription) =
        repository.update(subscription)
}

class DeleteSubscriptionUseCase @Inject constructor(
    private val repository: SubscriptionRepository
) {
    suspend operator fun invoke(subscription: Subscription) =
        repository.delete(subscription)
}

class GetSubscriptionByIdUseCase @Inject constructor(
    private val repository: SubscriptionRepository
) {
    suspend operator fun invoke(id: Long): Subscription? =
        repository.getById(id)
}
