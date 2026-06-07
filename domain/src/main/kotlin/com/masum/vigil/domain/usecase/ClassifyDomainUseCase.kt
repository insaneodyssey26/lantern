package com.masum.vigil.domain.usecase

import com.masum.vigil.domain.model.Verdict
import com.masum.vigil.domain.repository.BlocklistRepository
import javax.inject.Inject

class ClassifyDomainUseCase @Inject constructor(
    private val repository: BlocklistRepository,
) {
    operator fun invoke(domain: String): Verdict = repository.classify(domain)
}
