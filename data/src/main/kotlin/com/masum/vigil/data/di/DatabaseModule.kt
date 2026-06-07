package com.masum.vigil.data.di

import android.content.Context
import androidx.room.Room
import com.masum.vigil.core.database.VigilDatabase
import com.masum.vigil.core.database.dao.DnsQueryDao
import com.masum.vigil.core.database.dao.SessionDao
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
    fun provideVigilDatabase(@ApplicationContext context: Context): VigilDatabase =
        Room.databaseBuilder(context, VigilDatabase::class.java, "vigil.db").build()

    @Provides
    fun provideDnsQueryDao(db: VigilDatabase): DnsQueryDao = db.dnsQueryDao()

    @Provides
    fun provideSessionDao(db: VigilDatabase): SessionDao = db.sessionDao()
}
