package com.masum.lantern.data.di

import android.content.Context
import androidx.room.Room
import com.masum.lantern.core.database.LanternDatabase
import com.masum.lantern.core.database.dao.DnsQueryDao
import com.masum.lantern.core.database.dao.SessionDao
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
    fun provideLanternDatabase(@ApplicationContext context: Context): LanternDatabase =
        Room.databaseBuilder(context, LanternDatabase::class.java, "lantern.db").build()

    @Provides
    fun provideDnsQueryDao(db: LanternDatabase): DnsQueryDao = db.dnsQueryDao()

    @Provides
    fun provideSessionDao(db: LanternDatabase): SessionDao = db.sessionDao()
}
