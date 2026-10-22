package com.masum.lantern.domain.usecase

import com.masum.lantern.domain.model.Verdict
import com.masum.lantern.domain.repository.BlocklistRepository
import javax.inject.Inject

class ClassifyDomainUseCase @Inject constructor(
    private val repository: BlocklistRepository,
) {
    operator fun invoke(domain: String): Verdict = repository.classify(domain)
}
