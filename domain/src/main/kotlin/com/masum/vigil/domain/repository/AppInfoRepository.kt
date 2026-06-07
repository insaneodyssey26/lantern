package com.masum.vigil.domain.repository

import com.masum.vigil.domain.model.AppInfo

interface AppInfoRepository {
    suspend fun getAppInfoByUid(uid: Int): AppInfo
    suspend fun getAppInfoByPackage(packageName: String): AppInfo
}
