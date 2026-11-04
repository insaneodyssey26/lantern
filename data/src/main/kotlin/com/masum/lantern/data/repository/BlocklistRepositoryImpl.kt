package com.masum.lantern.data.repository

import com.masum.lantern.core.common.dispatcher.DispatcherProvider
import com.masum.lantern.core.database.dao.BlocklistDao
import com.masum.lantern.core.database.entity.BlocklistEntity
import com.masum.lantern.data.blocklist.BlocklistDownloader
import com.masum.lantern.data.blocklist.BlocklistLoader
import com.masum.lantern.data.blocklist.BlocklistMatcher
import com.masum.lantern.domain.model.Blocklist
import com.masum.lantern.domain.model.BlocklistCategory
import com.masum.lantern.domain.model.Verdict
import com.masum.lantern.domain.repository.BlocklistRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileInputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BlocklistRepositoryImpl @Inject constructor(
    private val matcher: BlocklistMatcher,
    private val blocklistDao: BlocklistDao,
    private val downloader: BlocklistDownloader,
    private val dispatchers: DispatcherProvider,
) : BlocklistRepository {

    private val scope = CoroutineScope(dispatchers.io)
    private val _totalRuleCount = MutableStateFlow(0)

    private val defaultBlocklists = listOf(
        BlocklistEntity(
            id = "stevenblack_base",
            name = "StevenBlack Unified",
            description = "Blocks adware, malware, and common telemetry servers without site breakage.",
            category = BlocklistCategory.ADS_AND_TRACKING.name,
            url = "https://raw.githubusercontent.com/StevenBlack/hosts/master/hosts",
            isEnabled = true,
            isCustom = false,
            ruleCount = 83000,
            lastUpdated = 0L,
            sourceUrl = "https://github.com/StevenBlack/hosts"
        ),
        BlocklistEntity(
            id = "oisd_basic",
            name = "OISD Small",
            description = "High-speed curated ad and telemetry blocklist tailored for mobile efficiency.",
            category = BlocklistCategory.ADS_AND_TRACKING.name,
            url = "https://small.oisd.nl",
            isEnabled = false,
            isCustom = false,
            ruleCount = 45000,
            lastUpdated = 0L,
            sourceUrl = "https://oisd.nl"
        ),
        BlocklistEntity(
            id = "hagezi_threat",
            name = "HaGeZi Threat Intelligence",
            description = "Protects against phishing, command-and-control servers, fake stores, and scam infrastructure.",
            category = BlocklistCategory.MALWARE.name,
            url = "https://raw.githubusercontent.com/hagezi/dns-blocklists/main/adblock/tif.mini.txt",
            isEnabled = true,
            isCustom = false,
            ruleCount = 60000,
            lastUpdated = 0L,
            sourceUrl = "https://github.com/hagezi/dns-blocklists"
        ),
        BlocklistEntity(
            id = "adguard_dns",
            name = "AdGuard DNS Filter",
            description = "Official AdGuard rules optimized for mobile ad blocking, analytics, and telemetry.",
            category = BlocklistCategory.ADS_AND_TRACKING.name,
            url = "https://adguardteam.github.io/HostlistsRegistry/assets/filter_1.txt",
            isEnabled = false,
            isCustom = false,
            ruleCount = 188000,
            lastUpdated = 0L,
            sourceUrl = "https://github.com/AdguardTeam/AdguardFilters"
        ),
        BlocklistEntity(
            id = "adguard_telemetry",
            name = "OEM & Device Diagnostics",
            description = "Blocks aggressive analytics and background trackers from smartphone OEMs and smart appliances.",
            category = BlocklistCategory.TELEMETRY.name,
            url = "https://raw.githubusercontent.com/StevenBlack/hosts/master/data/adaway.org/hosts",
            isEnabled = false,
            isCustom = false,
            ruleCount = 12000,
            lastUpdated = 0L,
            sourceUrl = "https://adaway.org"
        )
    )

    init {
        scope.launch {
            blocklistDao.insertDefaults(defaultBlocklists)
            for (defaultList in defaultBlocklists) {
                val source = defaultList.sourceUrl
                if (source != null) {
                    blocklistDao.updatePresetUrl(defaultList.id, defaultList.url, source)
                }
            }
            matcher.load()
            _totalRuleCount.value = matcher.domainCount()
        }
    }

    override fun classify(domain: String): Verdict = matcher.classify(domain)

    override fun domainCount(): Int = matcher.domainCount()

    override fun isReady(): Boolean = matcher.isReady()

    override fun getLastUpdatedTimestamp(): Long = matcher.getLastUpdatedTimestamp()

    override fun observeTotalRuleCount(): StateFlow<Int> = _totalRuleCount.asStateFlow()

    override fun observeBlocklists(): Flow<List<Blocklist>> {
        return blocklistDao.observeAll().map { entities ->
            entities.map { entity ->
                Blocklist(
                    id = entity.id,
                    name = entity.name,
                    description = entity.description,
                    category = try {
                        BlocklistCategory.valueOf(entity.category)
                    } catch (e: Exception) {
                        BlocklistCategory.CUSTOM
                    },
                    url = entity.url,
                    isEnabled = entity.isEnabled,
                    isCustom = entity.isCustom,
                    ruleCount = entity.ruleCount,
                    lastUpdated = entity.lastUpdated,
                    etag = entity.etag,
                    sourceUrl = entity.sourceUrl
                )
            }
        }
    }

    override suspend fun toggleBlocklist(id: String, isEnabled: Boolean) = withContext(dispatchers.io) {
        blocklistDao.updateEnabled(id, isEnabled)
        val entity = blocklistDao.getById(id)
        if (isEnabled && entity != null) {
            val localFile = downloader.getLocalFile(id)
            if (!localFile.exists() || localFile.length() == 0L) {
                runCatching { syncBlocklistInternal(entity) }
            }
        }
        matcher.load()
        _totalRuleCount.value = matcher.domainCount()
    }

    override suspend fun applyRecommendedPreset() = withContext(dispatchers.io) {
        val all = blocklistDao.getAll()
        for (item in all) {
            val shouldEnable = item.id == "stevenblack_base" || item.id == "hagezi_threat"
            if (item.isEnabled != shouldEnable) {
                blocklistDao.updateEnabled(item.id, shouldEnable)
                if (shouldEnable) {
                    val localFile = downloader.getLocalFile(item.id)
                    if (!localFile.exists() || localFile.length() == 0L) {
                        runCatching { syncBlocklistInternal(item) }
                    }
                }
            }
        }
        matcher.load()
        _totalRuleCount.value = matcher.domainCount()
    }

    override suspend fun addCustomBlocklist(name: String, url: String): Result<Unit> = withContext(dispatchers.io) {
        runCatching {
            val id = "custom_" + UUID.randomUUID().toString().take(8)
            val newEntity = BlocklistEntity(
                id = id,
                name = name.trim(),
                description = "Custom community blocklist feed",
                category = BlocklistCategory.CUSTOM.name,
                url = url.trim(),
                isEnabled = true,
                isCustom = true,
                ruleCount = 0,
                lastUpdated = 0L,
                sourceUrl = url.trim()
            )
            blocklistDao.insertOrUpdate(newEntity)
            syncBlocklistInternal(newEntity)
            matcher.load()
            _totalRuleCount.value = matcher.domainCount()
        }
    }

    override suspend fun deleteCustomBlocklist(id: String): Result<Unit> = withContext(dispatchers.io) {
        runCatching {
            blocklistDao.deleteCustom(id)
            downloader.deleteLocalFile(id)
            matcher.load()
            _totalRuleCount.value = matcher.domainCount()
        }
    }

    override suspend fun syncAllBlocklists(): Result<Int> = withContext(dispatchers.io) {
        runCatching {
            val enabled = blocklistDao.getEnabled()
            var updatedCount = 0
            for (item in enabled) {
                val modified = syncBlocklistInternal(item)
                if (modified) updatedCount++
            }
            if (updatedCount > 0) {
                matcher.load()
                _totalRuleCount.value = matcher.domainCount()
                matcher.setLastUpdatedTimestamp(System.currentTimeMillis())
            }
            updatedCount
        }
    }

    override suspend fun syncBlocklist(id: String): Result<Boolean> = withContext(dispatchers.io) {
        runCatching {
            val entity = blocklistDao.getById(id) ?: throw IllegalArgumentException("Blocklist not found")
            val modified = syncBlocklistInternal(entity)
            if (modified) {
                matcher.load()
                _totalRuleCount.value = matcher.domainCount()
                matcher.setLastUpdatedTimestamp(System.currentTimeMillis())
            }
            modified
        }
    }

    override suspend fun updateBlocklist(): Result<Boolean> {
        return syncAllBlocklists().map { it > 0 }
    }

    private suspend fun syncBlocklistInternal(entity: BlocklistEntity): Boolean {
        val download = downloader.downloadToFile(
            id = entity.id,
            url = entity.url,
            savedEtag = entity.etag,
            savedLastModified = entity.lastModifiedHeader
        )

        var count = 0
        if (download.file.exists()) {
            FileInputStream(download.file).use { input ->
                input.bufferedReader().useLines { lines ->
                    lines.forEach { line ->
                        if (BlocklistLoader.parseDomain(line) != null) {
                            count++
                        }
                    }
                }
            }
        }

        blocklistDao.updateMetadata(
            id = entity.id,
            ruleCount = count,
            lastUpdated = System.currentTimeMillis(),
            etag = download.etag,
            lastModifiedHeader = download.lastModified
        )

        return download.isModified
    }
}
