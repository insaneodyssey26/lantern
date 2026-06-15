package com.masum.vigil.data.di

import com.masum.vigil.data.repository.AppInfoRepositoryImpl
import com.masum.vigil.data.repository.DnsLogRepositoryImpl
import com.masum.vigil.data.repository.MonitoringRepositoryImpl
import com.masum.vigil.data.repository.SettingsRepositoryImpl
import com.masum.vigil.domain.repository.AppInfoRepository
import com.masum.vigil.domain.repository.DnsLogRepository
import com.masum.vigil.domain.repository.MonitoringRepository
import com.masum.vigil.domain.repository.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindDnsLogRepository(impl: DnsLogRepositoryImpl): DnsLogRepository

    @Binds
    @Singleton
    abstract fun bindMonitoringRepository(impl: MonitoringRepositoryImpl): MonitoringRepository

    @Binds
    @Singleton
    abstract fun bindAppInfoRepository(impl: AppInfoRepositoryImpl): AppInfoRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository
}
