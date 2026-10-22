package com.masum.lantern.data.repository

import com.masum.lantern.data.blocklist.BlocklistMatcher
import com.masum.lantern.domain.model.Verdict
import com.masum.lantern.domain.repository.BlocklistRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

class BlocklistRepositoryImpl @Inject constructor(
    private val matcher: BlocklistMatcher,
) : BlocklistRepository {

    init {
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            if (!matcher.isReady()) matcher.load()
        }
    }

    override fun classify(domain: String): Verdict = matcher.classify(domain)
    override fun domainCount(): Int = matcher.domainCount()
    override fun isReady(): Boolean = matcher.isReady()
}
