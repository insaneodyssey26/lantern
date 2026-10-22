package com.masum.lantern.domain.repository

import com.masum.lantern.domain.model.Verdict

interface BlocklistRepository {
    fun classify(domain: String): Verdict
    fun domainCount(): Int
    fun isReady(): Boolean
}
