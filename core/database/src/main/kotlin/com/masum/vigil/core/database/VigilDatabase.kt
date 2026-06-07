package com.masum.vigil.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.masum.vigil.core.database.dao.DnsQueryDao
import com.masum.vigil.core.database.dao.SessionDao
import com.masum.vigil.core.database.entity.DnsQueryEntity
import com.masum.vigil.core.database.entity.SessionEntity

@Database(
    entities = [SessionEntity::class, DnsQueryEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class VigilDatabase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao
    abstract fun dnsQueryDao(): DnsQueryDao
}
