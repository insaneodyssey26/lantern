package com.masum.lantern.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.masum.lantern.core.database.dao.DnsQueryDao
import com.masum.lantern.core.database.dao.SessionDao
import com.masum.lantern.core.database.entity.DnsQueryEntity
import com.masum.lantern.core.database.entity.SessionEntity

@Database(
    entities = [SessionEntity::class, DnsQueryEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class LanternDatabase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao
    abstract fun dnsQueryDao(): DnsQueryDao
}
