package com.bdnet.vpn.data.model

import com.bdnet.vpn.data.local.entity.ServerEntity

data class Server(
    val id: Long = 0,
    val name: String,
    val host: String,
    val port: Int,
    val protocol: String,
    val payload: String? = null,
    val uuid: String? = null,
    val path: String? = null,
    val sni: String? = null,
    val network: String? = null,
    val security: String? = null,
    val encryption: String? = null,
    val countryCode: String,
    val carrier: String? = null,
    val isCustom: Boolean = false,
    val isPremium: Boolean = false,
    val ping: Long? = null,
    val lastUsed: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toEntity(): ServerEntity = ServerEntity(
        id = id,
        name = name,
        host = host,
        port = port,
        protocol = protocol,
        payload = payload,
        uuid = uuid,
        path = path,
        sni = sni,
        network = network,
        security = security,
        encryption = encryption,
        countryCode = countryCode,
        carrier = carrier,
        isCustom = isCustom,
        isPremium = isPremium,
        ping = ping,
        lastUsed = lastUsed,
        createdAt = createdAt
    )

    companion object {
        fun fromEntity(entity: ServerEntity): Server = Server(
            id = entity.id,
            name = entity.name,
            host = entity.host,
            port = entity.port,
            protocol = entity.protocol,
            payload = entity.payload,
            uuid = entity.uuid,
            path = entity.path,
            sni = entity.sni,
            network = entity.network,
            security = entity.security,
            encryption = entity.encryption,
            countryCode = entity.countryCode,
            carrier = entity.carrier,
            isCustom = entity.isCustom,
            isPremium = entity.isPremium,
            ping = entity.ping,
            lastUsed = entity.lastUsed,
            createdAt = entity.createdAt
        )
    }
}
