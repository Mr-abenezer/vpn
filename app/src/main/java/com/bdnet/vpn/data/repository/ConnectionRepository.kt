package com.bdnet.vpn.data.repository

import com.bdnet.vpn.data.local.dao.ConnectionHistoryDao
import com.bdnet.vpn.data.local.entity.ConnectionHistoryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ConnectionRepository(private val historyDao: ConnectionHistoryDao) {

    val allHistory: Flow<List<ConnectionHistory>> = historyDao.getAllHistory().map { entities ->
        entities.map { ConnectionHistory.fromEntity(it) }
    }

    val recentHistory: Flow<List<ConnectionHistory>> = historyDao.getRecentHistory(50).map { entities ->
        entities.map { ConnectionHistory.fromEntity(it) }
    }

    fun getHistoryByServer(serverId: Long): Flow<List<ConnectionHistory>> =
        historyDao.getHistoryByServer(serverId).map { entities ->
            entities.map { ConnectionHistory.fromEntity(it) }
        }

    suspend fun addHistory(history: ConnectionHistory): Long =
        historyDao.insertHistory(history.toEntity())

    suspend fun updateHistory(history: ConnectionHistory) =
        historyDao.updateHistory(history.toEntity())

    suspend fun endConnection(historyId: Long, bytesIn: Long, bytesOut: Long, duration: Long) {
        val history = historyDao.getHistoryById(historyId)
        history?.let {
            val updated = it.copy(
                endTime = System.currentTimeMillis(),
                bytesIn = bytesIn,
                bytesOut = bytesOut,
                duration = duration
            )
            historyDao.updateHistory(updated)
        }
    }

    suspend fun deleteAllHistory() = historyDao.deleteAllHistory()

    suspend fun deleteOldHistory(daysToKeep: Int = 30) {
        val cutoffTime = System.currentTimeMillis() - (daysToKeep * 24 * 60 * 60 * 1000L)
        historyDao.deleteOldHistory(cutoffTime)
    }

    suspend fun getTotalStats(): ConnectionStats {
        val bytesIn = historyDao.getTotalBytesIn() ?: 0
        val bytesOut = historyDao.getTotalBytesOut() ?: 0
        val duration = historyDao.getTotalDuration() ?: 0
        return ConnectionStats(bytesIn, bytesOut, duration)
    }
}

data class ConnectionHistory(
    val id: Long = 0,
    val serverId: Long? = null,
    val serverName: String? = null,
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long? = null,
    val bytesIn: Long = 0,
    val bytesOut: Long = 0,
    val duration: Long = 0,
    val protocol: String? = null
) {
    fun toEntity(): ConnectionHistoryEntity = ConnectionHistoryEntity(
        id = id,
        serverId = serverId,
        serverName = serverName,
        startTime = startTime,
        endTime = endTime,
        bytesIn = bytesIn,
        bytesOut = bytesOut,
        duration = duration,
        protocol = protocol
    )

    companion object {
        fun fromEntity(entity: ConnectionHistoryEntity): ConnectionHistory = ConnectionHistory(
            id = entity.id,
            serverId = entity.serverId,
            serverName = entity.serverName,
            startTime = entity.startTime,
            endTime = entity.endTime,
            bytesIn = entity.bytesIn,
            bytesOut = entity.bytesOut,
            duration = entity.duration,
            protocol = entity.protocol
        )
    }
}

data class ConnectionStats(
    val totalBytesIn: Long,
    val totalBytesOut: Long,
    val totalDuration: Long
) {
    val totalBytes: Long get() = totalBytesIn + totalBytesOut
}
