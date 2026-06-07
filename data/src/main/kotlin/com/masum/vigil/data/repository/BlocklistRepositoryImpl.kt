package com.masum.vigil.data.repository

import com.masum.vigil.data.blocklist.BlocklistMatcher
import com.masum.vigil.domain.model.Verdict
import com.masum.vigil.domain.repository.BlocklistRepository
import javax.inject.Inject

class BlocklistRepositoryImpl @Inject constructor(
    private val matcher: BlocklistMatcher,
) : BlocklistRepository {
    override fun classify(domain: String): Verdict = matcher.classify(domain)
    override fun domainCount(): Int = matcher.domainCount()
    override fun isReady(): Boolean = matcher.isReady()
}
