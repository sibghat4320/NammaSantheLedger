package com.example.nammasantheledger.di

import com.example.nammasantheledger.data.repository.CustomerRepositoryImpl
import com.example.nammasantheledger.data.repository.TransactionRepositoryImpl
import com.example.nammasantheledger.domain.repository.CustomerRepository
import com.example.nammasantheledger.domain.repository.TransactionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module binding repository interfaces to their implementations.
 * Uses @Binds for zero-cost abstraction binding.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindCustomerRepository(
        impl: CustomerRepositoryImpl
    ): CustomerRepository

    @Binds
    @Singleton
    abstract fun bindTransactionRepository(
        impl: TransactionRepositoryImpl
    ): TransactionRepository
}
