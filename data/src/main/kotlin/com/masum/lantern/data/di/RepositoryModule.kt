package com.masum.lantern.data.di

import com.masum.lantern.data.repository.AppInfoRepositoryImpl
import com.masum.lantern.data.repository.DnsLogRepositoryImpl
import com.masum.lantern.data.repository.MonitoringRepositoryImpl
import com.masum.lantern.data.repository.SettingsRepositoryImpl
import com.masum.lantern.data.repository.UpdateRepositoryImpl
import com.masum.lantern.domain.repository.AppInfoRepository
import com.masum.lantern.domain.repository.DnsLogRepository
import com.masum.lantern.domain.repository.MonitoringRepository
import com.masum.lantern.domain.repository.SettingsRepository
import com.masum.lantern.domain.repository.UpdateRepository
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

    @Binds
    @Singleton
    abstract fun bindUpdateRepository(impl: UpdateRepositoryImpl): UpdateRepository
}
