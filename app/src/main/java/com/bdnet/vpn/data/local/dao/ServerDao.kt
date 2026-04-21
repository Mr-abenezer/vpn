package com.bdnet.vpn.data.local.dao

import androidx.room.*
import com.bdnet.vpn.data.local.entity.ServerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ServerDao {

    @Query("SELECT * FROM servers ORDER BY is_custom DESC, last_used IS NULL, last_used DESC, created_at DESC")
    fun getAllServers(): Flow<List<ServerEntity>>

    @Query("SELECT * FROM servers WHERE is_custom = 0 ORDER BY last_used IS NULL, last_used DESC")
    fun getOfficialServers(): Flow<List<ServerEntity>>

    @Query("SELECT * FROM servers WHERE is_custom = 1 ORDER BY created_at DESC")
    fun getCustomServers(): Flow<List<ServerEntity>>

    @Query("SELECT * FROM servers WHERE id = :id")
    suspend fun getServerById(id: Long): ServerEntity?

    @Query("SELECT * FROM servers WHERE id = :id")
    fun getServerByIdFlow(id: Long): Flow<ServerEntity?>

    @Query("SELECT * FROM servers WHERE country_code = :countryCode")
    fun getServersByCountry(countryCode: String): Flow<List<ServerEntity>>

    @Query("SELECT * FROM servers WHERE carrier = :carrier")
    fun getServersByCarrier(carrier: String): Flow<List<ServerEntity>>

    @Query("SELECT * FROM servers WHERE protocol = :protocol")
    fun getServersByProtocol(protocol: String): Flow<List<ServerEntity>>

    @Query("SELECT * FROM servers WHERE name LIKE '%' || :query || '%' OR host LIKE '%' || :query || '%'")
    fun searchServers(query: String): Flow<List<ServerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServer(server: ServerEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServers(servers: List<ServerEntity>)

    @Update
    suspend fun updateServer(server: ServerEntity)

    @Delete
    suspend fun deleteServer(server: ServerEntity)

    @Query("DELETE FROM servers WHERE is_custom = 1")
    suspend fun deleteAllCustomServers()

    @Query("DELETE FROM servers WHERE is_custom = 0")
    suspend fun deleteAllOfficialServers()

    @Query("UPDATE servers SET last_used = :timestamp WHERE id = :id")
    suspend fun updateLastUsed(id: Long, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE servers SET ping = :ping WHERE id = :id")
    suspend fun updatePing(id: Long, ping: Long)

    @Query("SELECT COUNT(*) FROM servers")
    fun getServerCount(): Flow<Int>

    @Query("SELECT * FROM servers ORDER BY last_used DESC LIMIT 1")
    suspend fun getLastUsedServer(): ServerEntity?
}
