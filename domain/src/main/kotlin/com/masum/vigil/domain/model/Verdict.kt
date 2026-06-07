package com.masum.vigil.domain.model

sealed class Verdict {
    data class Tracker(val category: String = "") : Verdict()
    data object Clean : Verdict()
    data object Unknown : Verdict()
}
