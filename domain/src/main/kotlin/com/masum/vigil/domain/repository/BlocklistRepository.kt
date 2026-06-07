package com.masum.vigil.domain.repository

import com.masum.vigil.domain.model.Verdict

interface BlocklistRepository {
    fun classify(domain: String): Verdict
    fun domainCount(): Int
    fun isReady(): Boolean
}
