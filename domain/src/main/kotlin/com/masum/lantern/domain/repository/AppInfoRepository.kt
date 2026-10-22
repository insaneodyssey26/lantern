package com.masum.lantern.domain.repository

import com.masum.lantern.domain.model.AppInfo

interface AppInfoRepository {
    suspend fun getAppInfoByUid(uid: Int): AppInfo
    suspend fun getAppInfoByPackage(packageName: String): AppInfo
    suspend fun getInstalledApps(): List<AppInfo>
}
