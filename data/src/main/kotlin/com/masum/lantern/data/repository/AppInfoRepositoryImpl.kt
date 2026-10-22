package com.masum.lantern.data.repository

import android.content.Context
import android.content.pm.PackageManager
import android.util.LruCache
import com.masum.lantern.core.common.dispatcher.DispatcherProvider
import com.masum.lantern.domain.model.AppInfo
import com.masum.lantern.domain.repository.AppInfoRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppInfoRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dispatchers: DispatcherProvider,
) : AppInfoRepository {

    private val uidCache = LruCache<Int, AppInfo>(256)
    private val packageCache = LruCache<String, AppInfo>(256)

    override suspend fun getAppInfoByUid(uid: Int): AppInfo = withContext(dispatchers.io) {
        uidCache.get(uid) ?: run {
            val pm = context.packageManager
            val packageName = pm.getPackagesForUid(uid)?.firstOrNull()
                ?: return@withContext AppInfo.UNKNOWN.copy(uid = uid)
            resolve(packageName, uid, pm).also { uidCache.put(uid, it) }
        }
    }

    override suspend fun getAppInfoByPackage(packageName: String): AppInfo = withContext(dispatchers.io) {
        packageCache.get(packageName) ?: run {
            resolve(packageName, -1, context.packageManager).also { packageCache.put(packageName, it) }
        }
    }

    override suspend fun getInstalledApps(): List<AppInfo> = withContext(dispatchers.io) {
        val pm = context.packageManager
        val mainIntent = android.content.Intent(android.content.Intent.ACTION_MAIN, null).apply {
            addCategory(android.content.Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos = pm.queryIntentActivities(mainIntent, 0)
        val seen = mutableSetOf<String>()
        val result = mutableListOf<AppInfo>()

        for (info in resolveInfos) {
            val pkg = info.activityInfo.packageName
            if (pkg != context.packageName && seen.add(pkg)) {
                val appInfo = resolve(pkg, info.activityInfo.applicationInfo.uid, pm)
                packageCache.put(pkg, appInfo)
                result.add(appInfo)
            }
        }
        result.sortedBy { it.appName.lowercase() }
    }

    private fun resolve(packageName: String, uid: Int, pm: PackageManager): AppInfo =
        runCatching {
            val info = pm.getApplicationInfo(packageName, 0)
            AppInfo(
                uid = if (uid != -1) uid else info.uid,
                packageName = packageName,
                appName = pm.getApplicationLabel(info).toString(),
            )
        }.getOrElse {
            AppInfo(uid = uid, packageName = packageName, appName = packageName)
        }
}
