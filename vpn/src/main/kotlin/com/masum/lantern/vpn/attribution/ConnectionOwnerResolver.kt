package com.masum.lantern.vpn.attribution

import android.content.Context
import android.net.ConnectivityManager
import android.os.Process
import android.util.LruCache
import com.masum.lantern.domain.model.AppInfo
import com.masum.lantern.vpn.parser.ParsedDnsQuery
import java.net.InetSocketAddress
import java.net.InetAddress
import java.nio.ByteBuffer
import android.system.OsConstants

class ConnectionOwnerResolver(context: Context) {

    private val cm = context.getSystemService(ConnectivityManager::class.java)
    private val pm = context.packageManager
    private val cache = LruCache<Int, AppInfo>(512)

    fun getUid(query: ParsedDnsQuery): Int = try {
        cm.getConnectionOwnerUid(
            OsConstants.IPPROTO_UDP,
            InetSocketAddress(intToAddress(query.sourceIp), query.sourcePort),
            InetSocketAddress(intToAddress(query.destIp), 53),
        )
    } catch (_: Exception) {
        Process.INVALID_UID
    }

    fun resolveAppInfo(uid: Int): AppInfo {
        if (uid == Process.INVALID_UID || uid < 0) return AppInfo.UNKNOWN
        cache.get(uid)?.let { return it }
        val packages = try {
            pm.getPackagesForUid(uid)
        } catch (e: Exception) {
            null
        } ?: return AppInfo.UNKNOWN
        val packageName = packages.firstOrNull() ?: return AppInfo.UNKNOWN
        val label = try {
            pm.getApplicationLabel(pm.getApplicationInfo(packageName, 0)).toString()
        } catch (_: Exception) {
            packageName
        }
        return AppInfo(uid, packageName, label).also { cache.put(uid, it) }
    }

    private fun intToAddress(ip: Int): InetAddress {
        val bytes = ByteBuffer.allocate(4).putInt(ip).array()
        return InetAddress.getByAddress(bytes)
    }
}
