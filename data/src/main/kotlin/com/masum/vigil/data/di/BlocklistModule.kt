package com.masum.vigil.data.di

import com.masum.vigil.data.repository.BlocklistRepositoryImpl
import com.masum.vigil.domain.repository.BlocklistRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class BlocklistModule {

    @Binds
    @Singleton
    abstract fun bindBlocklistRepository(impl: BlocklistRepositoryImpl): BlocklistRepository
}
