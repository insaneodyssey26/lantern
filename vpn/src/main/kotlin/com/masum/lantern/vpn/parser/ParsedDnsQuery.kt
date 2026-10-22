package com.masum.lantern.vpn.parser

data class ParsedDnsQuery(
    val transactionId: Int,
    val domain: String,
    val queryType: Int,
    val sourceIp: Int,
    val destIp: Int,
    val sourcePort: Int,
)
