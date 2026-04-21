package com.bdnet.vpn.data.repository

import com.bdnet.vpn.data.local.dao.ServerDao
import com.bdnet.vpn.data.local.entity.ServerEntity
import com.bdnet.vpn.data.model.Server
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ServerRepository(private val serverDao: ServerDao) {

    val allServers: Flow<List<Server>> = serverDao.getAllServers().map { entities ->
        entities.map { Server.fromEntity(it) }
    }

    val officialServers: Flow<List<Server>> = serverDao.getOfficialServers().map { entities ->
        entities.map { Server.fromEntity(it) }
    }

    val customServers: Flow<List<Server>> = serverDao.getCustomServers().map { entities ->
        entities.map { Server.fromEntity(it) }
    }

    fun getServerById(id: Long): Flow<Server?> = serverDao.getServerByIdFlow(id).map { it?.let { Server.fromEntity(it) } }

    fun getServersByCountry(countryCode: String): Flow<List<Server>> =
        serverDao.getServersByCountry(countryCode).map { entities ->
            entities.map { Server.fromEntity(it) }
        }

    fun getServersByCarrier(carrier: String): Flow<List<Server>> =
        serverDao.getServersByCarrier(carrier).map { entities ->
            entities.map { Server.fromEntity(it) }
        }

    fun searchServers(query: String): Flow<List<Server>> =
        serverDao.searchServers(query).map { entities ->
            entities.map { Server.fromEntity(it) }
        }

    suspend fun insertServer(server: Server): Long = serverDao.insertServer(server.toEntity())

    suspend fun insertServers(servers: List<Server>) = serverDao.insertServers(servers.map { it.toEntity() })

    suspend fun updateServer(server: Server) = serverDao.updateServer(server.toEntity())

    suspend fun deleteServer(server: Server) = serverDao.deleteServer(server.toEntity())

    suspend fun deleteAllCustomServers() = serverDao.deleteAllCustomServers()

    suspend fun updateLastUsed(id: Long) = serverDao.updateLastUsed(id)

    suspend fun getLastUsedServer(): Server? = serverDao.getLastUsedServer()?.let { Server.fromEntity(it) }

    suspend fun syncServers(remoteServers: List<Server>) {
        // Delete old official servers and insert new ones
        serverDao.deleteAllOfficialServers()
        serverDao.insertServers(remoteServers.map { it.toEntity() })
    }
}
