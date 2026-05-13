package com.example.nammasantheledger.di

import android.content.Context
import androidx.room.Room
import com.example.nammasantheledger.data.local.NammaSantheDatabase
import com.example.nammasantheledger.data.local.dao.CustomerDao
import com.example.nammasantheledger.data.local.dao.DailySummaryDao
import com.example.nammasantheledger.data.local.dao.TransactionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module providing database and DAO instances.
 *
 * Design decision: Using fallbackToDestructiveMigration since v3 is a
 * complete schema rewrite. In production with real users, you'd write
 * proper migration scripts to preserve data.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): NammaSantheDatabase {
        return Room.databaseBuilder(
            context,
            NammaSantheDatabase::class.java,
            "namma_santhe_db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideCustomerDao(database: NammaSantheDatabase): CustomerDao {
        return database.customerDao()
    }

    @Provides
    @Singleton
    fun provideTransactionDao(database: NammaSantheDatabase): TransactionDao {
        return database.transactionDao()
    }

    @Provides
    @Singleton
    fun provideDailySummaryDao(database: NammaSantheDatabase): DailySummaryDao {
        return database.dailySummaryDao()
    }
}
