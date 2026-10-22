package com.masum.lantern.vpn.session

import com.masum.lantern.domain.model.DnsQuery
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MonitoringSessionBus @Inject constructor() {

    private val _queries = MutableSharedFlow<DnsQuery>(
        replay = 100,
        extraBufferCapacity = 512,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    val queries: SharedFlow<DnsQuery> = _queries.asSharedFlow()

    fun tryEmit(query: DnsQuery) = _queries.tryEmit(query)

    fun resetReplayCache() = _queries.resetReplayCache()
}
