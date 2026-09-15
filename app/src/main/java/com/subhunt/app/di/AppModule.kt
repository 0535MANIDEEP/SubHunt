package com.subhunt.app.di

import android.content.Context
import androidx.room.Room
import com.subhunt.app.data.local.SubHuntDatabase
import com.subhunt.app.data.local.SubscriptionDao
import com.subhunt.app.data.local.UserPreferences
import com.subhunt.app.data.repository.SubscriptionRepository
import com.subhunt.app.data.repository.SubscriptionRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SubHuntDatabase {
        return Room.databaseBuilder(
            context,
            SubHuntDatabase::class.java,
            "subhunt.db"
        ).addMigrations(SubHuntDatabase.MIGRATION_1_2).build()
    }

    @Provides
    fun provideSubscriptionDao(database: SubHuntDatabase): SubscriptionDao {
        return database.subscriptionDao()
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindSubscriptionRepository(
        impl: SubscriptionRepositoryImpl
    ): SubscriptionRepository
}
